import { useState, useEffect } from 'react'
import './App.css'

// ==========================================
// PKCE UTILITY FUNCTIONS
// ==========================================

// Generate random string for code_verifier
function generateCodeVerifier(length = 64) {
  const charset = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~';
  let result = '';
  const randomValues = new Uint8Array(length);
  crypto.getRandomValues(randomValues);
  for (let i = 0; i < length; i++) {
    result += charset[randomValues[i] % charset.length];
  }
  return result;
}

// Create SHA-256 hash and base64url encode
async function generateCodeChallenge(verifier) {
  const encoder = new TextEncoder();
  const data = encoder.encode(verifier);
  const digest = await crypto.subtle.digest('SHA-256', data);
  
  // Base64URL encode
  const base64 = btoa(String.fromCharCode(...new Uint8Array(digest)));
  return base64.replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
}

// ==========================================
// MAIN APP COMPONENT
// ==========================================

function App() {
  const [step, setStep] = useState(0);
  const [codeVerifier, setCodeVerifier] = useState('');
  const [codeChallenge, setCodeChallenge] = useState('');
  const [authCode, setAuthCode] = useState('');
  const [storedVerifier, setStoredVerifier] = useState('');
  const [logs, setLogs] = useState([]);

  const addLog = (message, type = 'info') => {
    setLogs(prev => [...prev, { message, type, time: new Date().toLocaleTimeString() }]);
  };

  // Check URL for authorization code on load
  useEffect(() => {
    const urlParams = new URLSearchParams(window.location.search);
    const code = urlParams.get('code');
    if (code) {
      setAuthCode(code);
      setStoredVerifier(sessionStorage.getItem('pkce_verifier') || '');
      setStep(4);
      addLog('📥 Received authorization code from URL!', 'success');
    }
  }, []);

  // Step 1: Generate Code Verifier
  const handleStep1 = () => {
    const verifier = generateCodeVerifier();
    setCodeVerifier(verifier);
    addLog('🔑 Generated code_verifier (random 64 chars)', 'info');
    addLog(`   Verifier: ${verifier.substring(0, 30)}...`, 'code');
    setStep(1);
  };

  // Step 2: Generate Code Challenge (Hash)
  const handleStep2 = async () => {
    const challenge = await generateCodeChallenge(codeVerifier);
    setCodeChallenge(challenge);
    addLog('🔐 Created code_challenge = SHA256(code_verifier)', 'info');
    addLog(`   Challenge: ${challenge}`, 'code');
    addLog('   ⚠️ This hash is ONE-WAY! Cannot reverse it!', 'warning');
    setStep(2);
  };

  // Step 3: Store Verifier & Redirect
  const handleStep3 = () => {
    // Store verifier in sessionStorage
    sessionStorage.setItem('pkce_verifier', codeVerifier);
    addLog('💾 Stored code_verifier in sessionStorage', 'info');
    addLog('   (Only accessible in THIS browser tab!)', 'info');
    setStep(3);
  };

  // Step 4: Show what would be sent to Auth Server
  const handleShowAuthURL = () => {
    // Note: We're not actually redirecting to Google here
    // Just showing what the URL would look like
    const authUrl = `https://accounts.google.com/o/oauth2/v2/auth?
  client_id=YOUR_CLIENT_ID
  &response_type=code
  &redirect_uri=${encodeURIComponent(window.location.origin)}
  &scope=openid email profile
  &code_challenge=${codeChallenge}     ← HASH sent!
  &code_challenge_method=S256`;
    
    addLog('🌐 Authorization URL (what would be sent to Google):', 'info');
    addLog('   code_challenge is in URL (just a hash, safe!)', 'success');
    addLog('   code_verifier is NOT in URL (stays hidden!)', 'success');
  };

  // Simulate receiving auth code
  const handleSimulateCallback = () => {
    const fakeCode = '4/0AY0e-' + generateCodeVerifier(20);
    setAuthCode(fakeCode);
    setStoredVerifier(sessionStorage.getItem('pkce_verifier') || codeVerifier);
    addLog('📨 Simulated: Google redirected back with code', 'success');
    addLog(`   Authorization Code: ${fakeCode.substring(0, 25)}...`, 'code');
    setStep(4);
  };

  // Step 5: Token Exchange
  const handleStep5 = () => {
    addLog('📤 Token Exchange Request (to Google):', 'info');
    addLog('   POST https://oauth2.googleapis.com/token', 'code');
    addLog('   {', 'code');
    addLog(`     code: "${authCode.substring(0, 20)}..."`, 'code');
    addLog(`     code_verifier: "${storedVerifier.substring(0, 20)}..."  ← FROM sessionStorage!`, 'code');
    addLog('     client_id: "xxx"', 'code');
    addLog('     grant_type: "authorization_code"', 'code');
    addLog('   }', 'code');
    addLog('', 'info');
    addLog('🔍 Google verifies: SHA256(code_verifier) == stored code_challenge?', 'warning');
    addLog('   If YES ✅ → Tokens issued!', 'success');
    addLog('   If NO ❌ → Rejected!', 'error');
    setStep(5);
  };

  // Reset demo
  const handleReset = () => {
    setStep(0);
    setCodeVerifier('');
    setCodeChallenge('');
    setAuthCode('');
    setStoredVerifier('');
    setLogs([]);
    sessionStorage.removeItem('pkce_verifier');
    window.history.replaceState({}, '', window.location.pathname);
  };

  return (
    <div className="app">
      <h1>🔐 PKCE Demo</h1>
      <p className="subtitle">Proof Key for Code Exchange - Step by Step</p>

      {/* Current Values Display */}
      <div className="values-panel">
        <h3>📊 Current Values:</h3>
        <div className="value-row">
          <span className="label">code_verifier:</span>
          <span className={`value ${codeVerifier ? 'has-value' : ''}`}>
            {codeVerifier ? `${codeVerifier.substring(0, 40)}...` : '(not generated yet)'}
          </span>
          <span className="badge hidden">🔒 HIDDEN (in memory)</span>
        </div>
        <div className="value-row">
          <span className="label">code_challenge:</span>
          <span className={`value ${codeChallenge ? 'has-value' : ''}`}>
            {codeChallenge || '(not generated yet)'}
          </span>
          <span className="badge visible">👁️ VISIBLE (sent in URL)</span>
        </div>
        <div className="value-row">
          <span className="label">sessionStorage:</span>
          <span className={`value ${sessionStorage.getItem('pkce_verifier') ? 'has-value' : ''}`}>
            {sessionStorage.getItem('pkce_verifier') ? '✅ Verifier stored!' : '(empty)'}
          </span>
        </div>
      </div>

      {/* Steps */}
      <div className="steps">
        <button 
          onClick={handleStep1} 
          disabled={step > 0}
          className={step >= 1 ? 'completed' : ''}
        >
          Step 1: Generate code_verifier
        </button>

        <button 
          onClick={handleStep2} 
          disabled={step < 1 || step > 1}
          className={step >= 2 ? 'completed' : ''}
        >
          Step 2: Create code_challenge (SHA256 hash)
        </button>

        <button 
          onClick={handleStep3} 
          disabled={step < 2 || step > 2}
          className={step >= 3 ? 'completed' : ''}
        >
          Step 3: Store verifier in sessionStorage
        </button>

        <button 
          onClick={() => { handleShowAuthURL(); handleSimulateCallback(); }} 
          disabled={step < 3 || step > 3}
          className={step >= 4 ? 'completed' : ''}
        >
          Step 4: Redirect to Google (Simulated)
        </button>

        <button 
          onClick={handleStep5} 
          disabled={step < 4 || step > 4}
          className={step >= 5 ? 'completed' : ''}
        >
          Step 5: Exchange code + verifier for tokens
        </button>

        <button onClick={handleReset} className="reset">
          🔄 Reset Demo
        </button>
      </div>

      {/* Security Explanation */}
      <div className="security-box">
        <h3>🛡️ Why is PKCE Secure?</h3>
        <div className="comparison">
          <div className="what-hacker-sees">
            <h4>👀 What Hacker Can See (URL):</h4>
            <ul>
              <li>code_challenge (just a hash!)</li>
              <li>authorization_code</li>
            </ul>
          </div>
          <div className="what-hacker-needs">
            <h4>🔑 What Hacker Needs:</h4>
            <ul>
              <li>code_verifier (in browser memory!)</li>
              <li>authorization_code</li>
            </ul>
          </div>
        </div>
        <p className="key-point">
          ⭐ Hacker has the code but NOT the verifier! <br/>
          Can't reverse SHA256 hash → Can't get verifier → Can't exchange code!
        </p>
      </div>

      {/* Logs */}
      <div className="logs">
        <h3>📋 Activity Log:</h3>
        {logs.length === 0 && <p className="no-logs">Click Step 1 to start...</p>}
        {logs.map((log, i) => (
          <div key={i} className={`log-entry ${log.type}`}>
            <span className="time">[{log.time}]</span> {log.message}
          </div>
        ))}
      </div>
    </div>
  );
}

export default App
