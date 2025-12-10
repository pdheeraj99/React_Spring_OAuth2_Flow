import { useState, useEffect } from 'react'
import './App.css'

// Backend URL - mana Spring Boot server
const BACKEND_URL = 'http://localhost:8080';

function App() {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [photos, setPhotos] = useState(null);
    const [photosLoading, setPhotosLoading] = useState(false);
    const [error, setError] = useState(null);

    // Check login status on page load
    useEffect(() => {
        checkLoginStatus();
    }, []);

    const checkLoginStatus = async () => {
        try {
            const response = await fetch(`${BACKEND_URL}/api/user-status`, {
                credentials: 'include' // 🔥 IMPORTANT: Send cookies for session
            });
            const data = await response.json();

            if (data.authenticated) {
                setUser(data);
            }
        } catch (err) {
            console.error('Error checking login status:', err);
        } finally {
            setLoading(false);
        }
    };

    // Redirect to Google Login
    const handleLogin = () => {
        // Full page redirect to backend OAuth endpoint
        window.location.href = `${BACKEND_URL}/oauth2/authorization/google`;
    };

    // Logout
    const handleLogout = async () => {
        window.location.href = `${BACKEND_URL}/logout`;
    };

    // Fetch photos from Resource Server (via Backend proxy)
    const fetchPhotos = async () => {
        setPhotosLoading(true);
        setError(null);
        try {
            const response = await fetch(`${BACKEND_URL}/api/photos`, {
                credentials: 'include' // 🔥 Include session cookie
            });

            if (response.ok) {
                const data = await response.text();
                setPhotos(data);
            } else {
                setError('Failed to fetch photos. Status: ' + response.status);
            }
        } catch (err) {
            setError('Error: ' + err.message);
        } finally {
            setPhotosLoading(false);
        }
    };

    // Loading state
    if (loading) {
        return (
            <div className="app-container">
                <div className="loading-spinner">
                    <div className="spinner"></div>
                    <p>Loading...</p>
                </div>
            </div>
        );
    }

    return (
        <div className="app-container">
            {/* Header */}
            <header className="header">
                <div className="logo">
                    <span className="logo-icon">📸</span>
                    <span className="logo-text">PhotoVault Pro</span>
                </div>
                {user && (
                    <div className="user-menu">
                        <img src={user.picture} alt="Profile" className="user-avatar" />
                        <span className="user-name">{user.name}</span>
                        <button onClick={handleLogout} className="btn btn-logout">
                            Logout
                        </button>
                    </div>
                )}
            </header>

            <main className="main-content">
                {!user ? (
                    // ===== LOGIN PAGE =====
                    <div className="login-container">
                        <div className="login-card">
                            <div className="login-header">
                                <span className="app-icon">🔐</span>
                                <h1>Welcome to PhotoVault Pro</h1>
                                <p className="subtitle">Your secure photo storage powered by OAuth 2.0</p>
                            </div>

                            <div className="features">
                                <div className="feature">
                                    <span>🛡️</span>
                                    <span>Enterprise Security</span>
                                </div>
                                <div className="feature">
                                    <span>🔑</span>
                                    <span>OAuth 2.0 + JWT</span>
                                </div>
                                <div className="feature">
                                    <span>☁️</span>
                                    <span>Cloud Protected</span>
                                </div>
                            </div>

                            <button onClick={handleLogin} className="btn btn-google">
                                <svg className="google-icon" viewBox="0 0 24 24">
                                    <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" />
                                    <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" />
                                    <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" />
                                    <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" />
                                </svg>
                                Sign in with Google
                            </button>

                            <p className="security-note">
                                🔒 Secure authentication via Google OAuth 2.0
                            </p>
                        </div>

                        {/* Architecture Info (40LPA Knowledge) */}
                        <div className="architecture-card">
                            <h3>🏗️ BFF Pattern Architecture</h3>
                            <div className="flow-diagram">
                                <div className="flow-step">
                                    <span className="step-icon">🌐</span>
                                    <span>React UI</span>
                                </div>
                                <span className="arrow">→</span>
                                <div className="flow-step">
                                    <span className="step-icon">⚙️</span>
                                    <span>Backend (8080)</span>
                                </div>
                                <span className="arrow">→</span>
                                <div className="flow-step">
                                    <span className="step-icon">🔐</span>
                                    <span>Resource Server (8081)</span>
                                </div>
                            </div>
                            <p className="flow-note">
                                JWT tokens stay on backend - Never exposed to browser! 🛡️
                            </p>
                        </div>
                    </div>
                ) : (
                    // ===== DASHBOARD =====
                    <div className="dashboard">
                        <div className="welcome-section">
                            <h1>Welcome back, {user.name?.split(' ')[0]}! 👋</h1>
                            <p className="user-email">{user.email}</p>
                        </div>

                        <div className="cards-container">
                            {/* User Card */}
                            <div className="card user-card">
                                <div className="card-header">
                                    <span className="card-icon">👤</span>
                                    <h2>Your Profile</h2>
                                </div>
                                <div className="card-content">
                                    <img src={user.picture} alt="Profile" className="profile-image" />
                                    <div className="profile-details">
                                        <p><strong>Name:</strong> {user.name}</p>
                                        <p><strong>Email:</strong> {user.email}</p>
                                        <p className="auth-badge">✅ Authenticated via Google</p>
                                    </div>
                                </div>
                            </div>

                            {/* Photos Card */}
                            <div className="card photos-card">
                                <div className="card-header">
                                    <span className="card-icon">📸</span>
                                    <h2>Secret Photos</h2>
                                </div>
                                <div className="card-content">
                                    <p className="card-description">
                                        Click below to fetch your protected photos from the Resource Server.
                                        This demonstrates the complete OAuth 2.0 BFF flow!
                                    </p>

                                    <button
                                        onClick={fetchPhotos}
                                        className="btn btn-primary"
                                        disabled={photosLoading}
                                    >
                                        {photosLoading ? (
                                            <>
                                                <span className="btn-spinner"></span>
                                                Fetching...
                                            </>
                                        ) : (
                                            <>🚀 Get My Secret Photos</>
                                        )}
                                    </button>

                                    {error && (
                                        <div className="error-box">
                                            <span>❌</span> {error}
                                        </div>
                                    )}

                                    {photos && (
                                        <div className="photos-result">
                                            <div className="success-badge">✅ Resource Server Response</div>
                                            <div
                                                className="photos-content"
                                                dangerouslySetInnerHTML={{ __html: photos }}
                                            />
                                        </div>
                                    )}
                                </div>
                            </div>

                            {/* Flow Explanation Card */}
                            <div className="card flow-card">
                                <div className="card-header">
                                    <span className="card-icon">🔄</span>
                                    <h2>What Just Happened?</h2>
                                </div>
                                <div className="card-content">
                                    <ol className="flow-steps">
                                        <li>
                                            <span className="step-num">1</span>
                                            <span>React sent request to <code>/api/photos</code> with session cookie</span>
                                        </li>
                                        <li>
                                            <span className="step-num">2</span>
                                            <span>Backend extracted JWT (ID Token) from session</span>
                                        </li>
                                        <li>
                                            <span className="step-num">3</span>
                                            <span>Backend called Resource Server with <code>Authorization: Bearer JWT</code></span>
                                        </li>
                                        <li>
                                            <span className="step-num">4</span>
                                            <span>Resource Server validated JWT using Google's public keys</span>
                                        </li>
                                        <li>
                                            <span className="step-num">5</span>
                                            <span>Protected data returned! 🎉</span>
                                        </li>
                                    </ol>
                                </div>
                            </div>
                        </div>
                    </div>
                )}
            </main>

            {/* Footer */}
            <footer className="footer">
                <p>Built with ❤️ for 40LPA OAuth Mastery | Spring Security + React + BFF Pattern</p>
            </footer>
        </div>
    );
}

export default App
