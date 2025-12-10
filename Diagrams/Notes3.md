<!-- "C:\Program Files\BellSoft\LibericaJDK-21\bin\java.exe" -XX:TieredStopAtLevel=1 -Dspring.output.ansi.enabled=always -Dcom.sun.management.jmxremote -Dspring.jmx.enabled=true -Dspring.liveBeansView.mbeanDomain -Dspring.application.admin.enabled=true "-Dmanagement.endpoints.jmx.exposure.include=*" "-javaagent:C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.3\lib\idea_rt.jar=62855" -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -classpath "D:\Spring Security\OAuth 2\client-app\target\classes;C:\Users\pilla\.m2\repository\org\springframework\boot\spring-boot-starter-oauth2-client\3.5.7\spring-boot-starter-oauth2-client-3.5.7.jar;C:\Users\pilla\.m2\repository\org\springframework\boot\spring-boot-starter\3.5.7\spring-boot-starter-3.5.7.jar;C:\Users\pilla\.m2\repository\org\springframework\boot\spring-boot-starter-logging\3.5.7\spring-boot-starter-logging-3.5.7.jar;C:\Users\pilla\.m2\repository\ch\qos\logback\logback-classic\1.5.20\logback-classic-1.5.20.jar;C:\Users\pilla\.m2\repository\ch\qos\logback\logback-core\1.5.20\logback-core-1.5.20.jar;C:\Users\pilla\.m2\repository\org\apache\logging\log4j\log4j-to-slf4j\2.24.3\log4j-to-slf4j-2.24.3.jar;C:\Users\pilla\.m2\repository\org\apache\logging\log4j\log4j-api\2.24.3\log4j-api-2.24.3.jar;C:\Users\pilla\.m2\repository\org\slf4j\jul-to-slf4j\2.0.17\jul-to-slf4j-2.0.17.jar;C:\Users\pilla\.m2\repository\jakarta\annotation\jakarta.annotation-api\2.1.1\jakarta.annotation-api-2.1.1.jar;C:\Users\pilla\.m2\repository\org\yaml\snakeyaml\2.4\snakeyaml-2.4.jar;C:\Users\pilla\.m2\repository\org\springframework\security\spring-security-config\6.5.6\spring-security-config-6.5.6.jar;C:\Users\pilla\.m2\repository\org\springframework\spring-beans\6.2.12\spring-beans-6.2.12.jar;C:\Users\pilla\.m2\repository\org\springframework\spring-context\6.2.12\spring-context-6.2.12.jar;C:\Users\pilla\.m2\repository\org\springframework\security\spring-security-core\6.5.6\spring-security-core-6.5.6.jar;C:\Users\pilla\.m2\repository\org\springframework\security\spring-security-crypto\6.5.6\spring-security-crypto-6.5.6.jar;C:\Users\pilla\.m2\repository\org\springframework\spring-expression\6.2.12\spring-expression-6.2.12.jar;C:\Users\pilla\.m2\repository\io\micrometer\micrometer-observation\1.15.5\micrometer-observation-1.15.5.jar;C:\Users\pilla\.m2\repository\io\micrometer\micrometer-commons\1.15.5\micrometer-commons-1.15.5.jar;C:\Users\pilla\.m2\repository\org\springframework\security\spring-security-oauth2-client\6.5.6\spring-security-oauth2-client-6.5.6.jar;C:\Users\pilla\.m2\repository\org\springframework\security\spring-security-oauth2-core\6.5.6\spring-security-oauth2-core-6.5.6.jar;C:\Users\pilla\.m2\repository\com\nimbusds\oauth2-oidc-sdk\9.43.6\oauth2-oidc-sdk-9.43.6.jar;C:\Users\pilla\.m2\repository\com\github\stephenc\jcip\jcip-annotations\1.0-1\jcip-annotations-1.0-1.jar;C:\Users\pilla\.m2\repository\com\nimbusds\content-type\2.2\content-type-2.2.jar;C:\Users\pilla\.m2\repository\com\nimbusds\lang-tag\1.7\lang-tag-1.7.jar;C:\Users\pilla\.m2\repository\org\springframework\security\spring-security-oauth2-jose\6.5.6\spring-security-oauth2-jose-6.5.6.jar;C:\Users\pilla\.m2\repository\com\nimbusds\nimbus-jose-jwt\9.37.4\nimbus-jose-jwt-9.37.4.jar;C:\Users\pilla\.m2\repository\org\springframework\boot\spring-boot-starter-security\3.5.7\spring-boot-starter-security-3.5.7.jar;C:\Users\pilla\.m2\repository\org\springframework\spring-aop\6.2.12\spring-aop-6.2.12.jar;C:\Users\pilla\.m2\repository\org\springframework\security\spring-security-web\6.5.6\spring-security-web-6.5.6.jar;C:\Users\pilla\.m2\repository\org\springframework\boot\spring-boot-starter-web\3.5.7\spring-boot-starter-web-3.5.7.jar;C:\Users\pilla\.m2\repository\org\springframework\boot\spring-boot-starter-json\3.5.7\spring-boot-starter-json-3.5.7.jar;C:\Users\pilla\.m2\repository\com\fasterxml\jackson\core\jackson-databind\2.19.2\jackson-databind-2.19.2.jar;C:\Users\pilla\.m2\repository\com\fasterxml\jackson\core\jackson-annotations\2.19.2\jackson-annotations-2.19.2.jar;C:\Users\pilla\.m2\repository\com\fasterxml\jackson\core\jackson-core\2.19.2\jackson-core-2.19.2.jar;C:\Users\pilla\.m2\repository\com\fasterxml\jackson\datatype\jackson-datatype-jdk8\2.19.2\jackson-datatype-jdk8-2.19.2.jar;C:\Users\pilla\.m2\repository\com\fasterxml\jackson\datatype\jackson-datatype-jsr310\2.19.2\jackson-datatype-jsr310-2.19.2.jar;C:\Users\pilla\.m2\repository\com\fasterxml\jackson\module\jackson-module-parameter-names\2.19.2\jackson-module-parameter-names-2.19.2.jar;C:\Users\pilla\.m2\repository\org\springframework\boot\spring-boot-starter-tomcat\3.5.7\spring-boot-starter-tomcat-3.5.7.jar;C:\Users\pilla\.m2\repository\org\apache\tomcat\embed\tomcat-embed-core\10.1.48\tomcat-embed-core-10.1.48.jar;C:\Users\pilla\.m2\repository\org\apache\tomcat\embed\tomcat-embed-el\10.1.48\tomcat-embed-el-10.1.48.jar;C:\Users\pilla\.m2\repository\org\apache\tomcat\embed\tomcat-embed-websocket\10.1.48\tomcat-embed-websocket-10.1.48.jar;C:\Users\pilla\.m2\repository\org\springframework\spring-web\6.2.12\spring-web-6.2.12.jar;C:\Users\pilla\.m2\repository\org\springframework\spring-webmvc\6.2.12\spring-webmvc-6.2.12.jar;C:\Users\pilla\.m2\repository\org\springframework\boot\spring-boot-devtools\3.5.7\spring-boot-devtools-3.5.7.jar;C:\Users\pilla\.m2\repository\org\springframework\boot\spring-boot\3.5.7\spring-boot-3.5.7.jar;C:\Users\pilla\.m2\repository\org\springframework\boot\spring-boot-autoconfigure\3.5.7\spring-boot-autoconfigure-3.5.7.jar;C:\Users\pilla\.m2\repository\org\projectlombok\lombok\1.18.42\lombok-1.18.42.jar;C:\Users\pilla\.m2\repository\org\slf4j\slf4j-api\2.0.17\slf4j-api-2.0.17.jar;C:\Users\pilla\.m2\repository\net\minidev\json-smart\2.5.2\json-smart-2.5.2.jar;C:\Users\pilla\.m2\repository\net\minidev\accessors-smart\2.5.2\accessors-smart-2.5.2.jar;C:\Users\pilla\.m2\repository\org\ow2\asm\asm\9.7.1\asm-9.7.1.jar;C:\Users\pilla\.m2\repository\org\springframework\spring-core\6.2.12\spring-core-6.2.12.jar;C:\Users\pilla\.m2\repository\org\springframework\spring-jcl\6.2.12\spring-jcl-6.2.12.jar" com.oauth.client_app.ClientAppApplication

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.5.7)

2025-11-21T15:16:58.666+05:30  INFO 12484 --- [client-app] [  restartedMain] c.oauth.client_app.ClientAppApplication  : Starting ClientAppApplication using Java 21.0.8 with PID 12484 (D:\Spring Security\OAuth 2\client-app\target\classes started by pilla in D:\Spring Security\OAuth 2)
2025-11-21T15:16:58.678+05:30  INFO 12484 --- [client-app] [  restartedMain] c.oauth.client_app.ClientAppApplication  : No active profile set, falling back to 1 default profile: "default"
2025-11-21T15:16:58.741+05:30  INFO 12484 --- [client-app] [  restartedMain] .e.DevToolsPropertyDefaultsPostProcessor : Devtools property defaults active! Set 'spring.devtools.add-properties' to 'false' to disable
2025-11-21T15:16:58.742+05:30  INFO 12484 --- [client-app] [  restartedMain] .e.DevToolsPropertyDefaultsPostProcessor : For additional web related logging consider setting the 'logging.level.web' property to 'DEBUG'
2025-11-21T15:16:59.873+05:30  INFO 12484 --- [client-app] [  restartedMain] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 8080 (http)
2025-11-21T15:16:59.890+05:30  INFO 12484 --- [client-app] [  restartedMain] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2025-11-21T15:16:59.891+05:30  INFO 12484 --- [client-app] [  restartedMain] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.48]
2025-11-21T15:16:59.939+05:30  INFO 12484 --- [client-app] [  restartedMain] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2025-11-21T15:16:59.940+05:30  INFO 12484 --- [client-app] [  restartedMain] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 1198 ms
2025-11-21T15:17:00.604+05:30 DEBUG 12484 --- [client-app] [  restartedMain] swordEncoderAuthenticationManagerBuilder : No authenticationProviders and no parentAuthenticationManager defined. Returning null.
2025-11-21T15:17:00.731+05:30 DEBUG 12484 --- [client-app] [  restartedMain] o.s.s.web.DefaultSecurityFilterChain     : Will secure any request with filters: DisableEncodeUrlFilter, WebAsyncManagerIntegrationFilter, SecurityContextHolderFilter, HeaderWriterFilter, CsrfFilter, LogoutFilter, OAuth2AuthorizationRequestRedirectFilter, OAuth2AuthorizationRequestRedirectFilter, OAuth2LoginAuthenticationFilter, DefaultResourcesFilter, DefaultLoginPageGeneratingFilter, DefaultLogoutPageGeneratingFilter, RequestCacheAwareFilter, SecurityContextHolderAwareRequestFilter, AnonymousAuthenticationFilter, OAuth2AuthorizationCodeGrantFilter, ExceptionTranslationFilter, AuthorizationFilter
2025-11-21T15:17:00.801+05:30  INFO 12484 --- [client-app] [  restartedMain] o.s.b.d.a.OptionalLiveReloadServer       : LiveReload server is running on port 35729
2025-11-21T15:17:00.843+05:30  INFO 12484 --- [client-app] [  restartedMain] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 8080 (http) with context path '/'
2025-11-21T15:17:00.850+05:30  INFO 12484 --- [client-app] [  restartedMain] c.oauth.client_app.ClientAppApplication  : Started ClientAppApplication in 2.831 seconds (process running for 4.275)
2025-11-21T15:17:22.055+05:30  INFO 12484 --- [client-app] [nio-8080-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2025-11-21T15:17:22.056+05:30  INFO 12484 --- [client-app] [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2025-11-21T15:17:22.056+05:30  INFO 12484 --- [client-app] [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 0 ms
2025-11-21T15:17:22.075+05:30 DEBUG 12484 --- [client-app] [nio-8080-exec-1] o.s.security.web.FilterChainProxy        : Securing GET /oauth2/authorization/google
2025-11-21T15:17:22.099+05:30 DEBUG 12484 --- [client-app] [nio-8080-exec-1] o.s.s.web.DefaultRedirectStrategy        : Redirecting to https://accounts.google.com/o/oauth2/v2/auth?response_type=code&client_id=450472639030-g4g6r5terpsr6i9eo5bfhmfedcf33387.apps.googleusercontent.com&scope=email%20profile&state=OIrctuFmQQBsamrioh1V-QROxjIfKVjiiZmxhzSTYAI%3D&redirect_uri=http://localhost:8080/login/oauth2/code/google
2025-11-21T15:17:29.560+05:30 DEBUG 12484 --- [client-app] [nio-8080-exec-2] o.s.security.web.FilterChainProxy        : Securing GET /login/oauth2/code/google?state=OIrctuFmQQBsamrioh1V-QROxjIfKVjiiZmxhzSTYAI%3D&code=4%2F0Ab32j93kj8NIjEXWDcmjHCkjX5-JT1efbaAomrqiwvrBiDvz7o3Z1Cq9YESvgmisP1ClzA&scope=email+profile+https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email+https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.profile+openid&authuser=1&prompt=none
2025-11-21T15:17:29.744+05:30 DEBUG 12484 --- [client-app] [nio-8080-exec-2] o.s.web.client.RestTemplate              : HTTP POST https://oauth2.googleapis.com/token
2025-11-21T15:17:29.751+05:30 DEBUG 12484 --- [client-app] [nio-8080-exec-2] o.s.web.client.RestTemplate              : Accept=[application/json, application/*+json]
2025-11-21T15:17:29.751+05:30 DEBUG 12484 --- [client-app] [nio-8080-exec-2] o.s.web.client.RestTemplate              : Writing [{grant_type=[authorization_code], code=[4/0Ab32j93kj8NIjEXWDcmjHCkjX5-JT1efbaAomrqiwvrBiDvz7o3Z1Cq9YESvgmisP1ClzA], redirect_uri=[http://localhost:8080/login/oauth2/code/google]}] as "application/x-www-form-urlencoded;charset=UTF-8"
2025-11-21T15:17:30.275+05:30 DEBUG 12484 --- [client-app] [nio-8080-exec-2] o.s.web.client.RestTemplate              : Response 200 OK
2025-11-21T15:17:30.277+05:30 DEBUG 12484 --- [client-app] [nio-8080-exec-2] o.s.web.client.RestTemplate              : Reading to [org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse] as "application/json;charset=utf-8"
2025-11-21T15:17:30.322+05:30 DEBUG 12484 --- [client-app] [nio-8080-exec-2] o.s.web.client.RestTemplate              : HTTP GET https://www.googleapis.com/oauth2/v3/userinfo
2025-11-21T15:17:30.323+05:30 DEBUG 12484 --- [client-app] [nio-8080-exec-2] o.s.web.client.RestTemplate              : Accept=[application/json, application/*+json]
2025-11-21T15:17:30.522+05:30 DEBUG 12484 --- [client-app] [nio-8080-exec-2] o.s.web.client.RestTemplate              : Response 200 OK
2025-11-21T15:17:30.522+05:30 DEBUG 12484 --- [client-app] [nio-8080-exec-2] o.s.web.client.RestTemplate              : Reading to [java.util.Map<java.lang.String, java.lang.Object>]
2025-11-21T15:17:30.558+05:30 DEBUG 12484 --- [client-app] [nio-8080-exec-2] .s.ChangeSessionIdAuthenticationStrategy : Changed session id from DCBA869D8B50425AFC088D7CC2A49E42
2025-11-21T15:17:30.560+05:30 DEBUG 12484 --- [client-app] [nio-8080-exec-2] w.c.HttpSessionSecurityContextRepository : Stored SecurityContextImpl [Authentication=OAuth2AuthenticationToken [Principal=Name: [100897790827880714898], Granted Authorities: [[OAUTH2_USER, SCOPE_https://www.googleapis.com/auth/userinfo.email, SCOPE_https://www.googleapis.com/auth/userinfo.profile, SCOPE_openid]], User Attributes: [{sub=100897790827880714898, name=My TV, given_name=My TV, picture=https://lh3.googleusercontent.com/a/ACg8ocKcwgJhdJ6l9N4l8z-XfIbTUvqKX6YcysjH3_bG9BIiEtQ3Aw=s96-c, email=mytv35049@gmail.com, email_verified=true}], Credentials=[PROTECTED], Authenticated=true, Details=WebAuthenticationDetails [RemoteIpAddress=0:0:0:0:0:0:0:1, SessionId=DCBA869D8B50425AFC088D7CC2A49E42], Granted Authorities=[OAUTH2_USER, SCOPE_https://www.googleapis.com/auth/userinfo.email, SCOPE_https://www.googleapis.com/auth/userinfo.profile, SCOPE_openid]]] to HttpSession [org.apache.catalina.session.StandardSessionFacade@9dc87f9]
2025-11-21T15:17:30.560+05:30 DEBUG 12484 --- [client-app] [nio-8080-exec-2] .s.o.c.w.OAuth2LoginAuthenticationFilter : Set SecurityContextHolder to OAuth2AuthenticationToken [Principal=Name: [100897790827880714898], Granted Authorities: [[OAUTH2_USER, SCOPE_https://www.googleapis.com/auth/userinfo.email, SCOPE_https://www.googleapis.com/auth/userinfo.profile, SCOPE_openid]], User Attributes: [{sub=100897790827880714898, name=My TV, given_name=My TV, picture=https://lh3.googleusercontent.com/a/ACg8ocKcwgJhdJ6l9N4l8z-XfIbTUvqKX6YcysjH3_bG9BIiEtQ3Aw=s96-c, email=mytv35049@gmail.com, email_verified=true}], Credentials=[PROTECTED], Authenticated=true, Details=WebAuthenticationDetails [RemoteIpAddress=0:0:0:0:0:0:0:1, SessionId=DCBA869D8B50425AFC088D7CC2A49E42], Granted Authorities=[OAUTH2_USER, SCOPE_https://www.googleapis.com/auth/userinfo.email, SCOPE_https://www.googleapis.com/auth/userinfo.profile, SCOPE_openid]]
2025-11-21T15:17:30.560+05:30 DEBUG 12484 --- [client-app] [nio-8080-exec-2] o.s.s.web.DefaultRedirectStrategy        : Redirecting to /
2025-11-21T15:17:30.581+05:30 DEBUG 12484 --- [client-app] [nio-8080-exec-5] o.s.security.web.FilterChainProxy        : Securing GET /
2025-11-21T15:17:30.584+05:30 DEBUG 12484 --- [client-app] [nio-8080-exec-5] w.c.HttpSessionSecurityContextRepository : Retrieved SecurityContextImpl [Authentication=OAuth2AuthenticationToken [Principal=Name: [100897790827880714898], Granted Authorities: [[OAUTH2_USER, SCOPE_https://www.googleapis.com/auth/userinfo.email, SCOPE_https://www.googleapis.com/auth/userinfo.profile, SCOPE_openid]], User Attributes: [{sub=100897790827880714898, name=My TV, given_name=My TV, picture=https://lh3.googleusercontent.com/a/ACg8ocKcwgJhdJ6l9N4l8z-XfIbTUvqKX6YcysjH3_bG9BIiEtQ3Aw=s96-c, email=mytv35049@gmail.com, email_verified=true}], Credentials=[PROTECTED], Authenticated=true, Details=WebAuthenticationDetails [RemoteIpAddress=0:0:0:0:0:0:0:1, SessionId=DCBA869D8B50425AFC088D7CC2A49E42], Granted Authorities=[OAUTH2_USER, SCOPE_https://www.googleapis.com/auth/userinfo.email, SCOPE_https://www.googleapis.com/auth/userinfo.profile, SCOPE_openid]]]
2025-11-21T15:17:30.585+05:30 DEBUG 12484 --- [client-app] [nio-8080-exec-5] o.s.security.web.FilterChainProxy        : Secured GET /
2025-11-21T15:17:30.711+05:30 DEBUG 12484 --- [client-app] [nio-8080-exec-4] o.s.security.web.FilterChainProxy        : Securing GET /.well-known/appspecific/com.chrome.devtools.json
2025-11-21T15:17:30.711+05:30 DEBUG 12484 --- [client-app] [nio-8080-exec-4] w.c.HttpSessionSecurityContextRepository : Retrieved SecurityContextImpl [Authentication=OAuth2AuthenticationToken [Principal=Name: [100897790827880714898], Granted Authorities: [[OAUTH2_USER, SCOPE_https://www.googleapis.com/auth/userinfo.email, SCOPE_https://www.googleapis.com/auth/userinfo.profile, SCOPE_openid]], User Attributes: [{sub=100897790827880714898, name=My TV, given_name=My TV, picture=https://lh3.googleusercontent.com/a/ACg8ocKcwgJhdJ6l9N4l8z-XfIbTUvqKX6YcysjH3_bG9BIiEtQ3Aw=s96-c, email=mytv35049@gmail.com, email_verified=true}], Credentials=[PROTECTED], Authenticated=true, Details=WebAuthenticationDetails [RemoteIpAddress=0:0:0:0:0:0:0:1, SessionId=DCBA869D8B50425AFC088D7CC2A49E42], Granted Authorities=[OAUTH2_USER, SCOPE_https://www.googleapis.com/auth/userinfo.email, SCOPE_https://www.googleapis.com/auth/userinfo.profile, SCOPE_openid]]]
2025-11-21T15:17:30.712+05:30 DEBUG 12484 --- [client-app] [nio-8080-exec-4] o.s.security.web.FilterChainProxy        : Secured GET /.well-known/appspecific/com.chrome.devtools.json
2025-11-21T15:17:30.737+05:30 DEBUG 12484 --- [client-app] [nio-8080-exec-4] o.s.security.web.FilterChainProxy        : Securing GET /error
2025-11-21T15:17:30.738+05:30 DEBUG 12484 --- [client-app] [nio-8080-exec-4] w.c.HttpSessionSecurityContextRepository : Retrieved SecurityContextImpl [Authentication=OAuth2AuthenticationToken [Principal=Name: [100897790827880714898], Granted Authorities: [[OAUTH2_USER, SCOPE_https://www.googleapis.com/auth/userinfo.email, SCOPE_https://www.googleapis.com/auth/userinfo.profile, SCOPE_openid]], User Attributes: [{sub=100897790827880714898, name=My TV, given_name=My TV, picture=https://lh3.googleusercontent.com/a/ACg8ocKcwgJhdJ6l9N4l8z-XfIbTUvqKX6YcysjH3_bG9BIiEtQ3Aw=s96-c, email=mytv35049@gmail.com, email_verified=true}], Credentials=[PROTECTED], Authenticated=true, Details=WebAuthenticationDetails [RemoteIpAddress=0:0:0:0:0:0:0:1, SessionId=DCBA869D8B50425AFC088D7CC2A49E42], Granted Authorities=[OAUTH2_USER, SCOPE_https://www.googleapis.com/auth/userinfo.email, SCOPE_https://www.googleapis.com/auth/userinfo.profile, SCOPE_openid]]]
2025-11-21T15:17:30.738+05:30 DEBUG 12484 --- [client-app] [nio-8080-exec-4] o.s.security.web.FilterChainProxy        : Secured GET /error -->




Nuvvu `spring-boot-starter-oauth2-client` add chesi, `application.yml` lo Google details ivvagane...
Spring Boot: *"Ok, veedu ippudu Local Database (User/Pass) login vadatledu, Google/Facebook lanti External Login vadutunnadu"* ani fix ayyi, patha **Form Login Filters** ni pakkana petti, **OAuth2 Filters** ni field loki dimputundi.

Nee logs lo unna **Top 5 Hero Filters** ni, avi em chestunnayo "Post-Mortem" cheddam. Ee knowledge unte repu poddunna "Custom Authentication" rayalanna neeku easy ga untundi.

Here is the breakdown of your Log Evidence:

-----

### 🕵️‍♂️ The Filter Squad (As per your Logs)

Nee log line: `Will secure any request with filters: [..., OAuth2AuthorizationRequestRedirectFilter, OAuth2LoginAuthenticationFilter, ...]`

Deenni manam **Movie Logic** lo chuddam:

#### 1\. `AnonymousAuthenticationFilter` (The Gatekeeper)

  * **Role:** "Veedu evado telidu."
  * **Log Evidence:** `Set SecurityContextHolder to anonymous SecurityContext`.
  * **Em chesindi:** First time request vachinappudu, user login avvaledu kabatti, temporary ga "Anonymous" ani tag vesindi.

#### 2\. `ExceptionTranslationFilter` (The Bouncer 🛑)

  * **Role:** "Identity Card ledu, bayataki po\!"
  * **Situation:** User `/` page adigadu, kani `Anonymous` user. So permission denied (`401`).
  * **Action:** Denied ani cheppi, next em cheyalo decide chesi **Start Authentication** process trigger chestundi.

#### 3\. `OAuth2AuthorizationRequestRedirectFilter` (The Travel Agent ✈️)

  * **Role:** "Google ki velle Ticket (URL) ready cheyadam."
  * **Log Evidence:** `Redirecting to https://accounts.google.com/o/oauth2/v2/auth?...`
  * **Em chesindi:**
      * Idi request ni intercept chestundi (`/oauth2/authorization/google`).
      * Backend lo `Client ID`, `Scope`, `Redirect URI` anni kalipi oka pedda Google URL construct chestundi.
      * Browser ki `302 Redirect` istundi.
      * *Note:* Login page chupinchedi ide.

#### 4\. `OAuth2LoginAuthenticationFilter` (The Main Hero 💪)

  * **Role:** "Code tiskuni, Token techukuni, Validate cheyadam."
  * **Log Evidence:**
      * `HTTP POST https://oauth2.googleapis.com/token` (Back-channel call).
      * `HTTP GET https://www.googleapis.com/oauth2/v3/userinfo` (User info call).
  * **Em chesindi:**
      * Google nundi Browser return vachinappudu (`/login/oauth2/code/google`), ee filter **Catch** chestundi.
      * Idi `RestTemplate` ni vaadi Google server tho matladi Code ni Token ga marchi, User details techukuntundi.
      * Finally, user ni "Authenticated" ga mark chestundi. **Idi chala crucial filter.**

#### 5\. `SecurityContextHolderFilter` (The Memory Chip 🧠)

  * (Note: Old versions lo `SecurityContextPersistenceFilter` anevaaru).
  * **Role:** "Session Management."
  * **Log Evidence:** `Stored SecurityContextImpl ... to HttpSession`.
  * **Em chesindi:**
      * `OAuth2LoginAuthenticationFilter` pani aipoyaka, vachina User Details (Principal) ni **Session** lo dachipettukuntundi.
      * Next time user refresh chesthe, malli login adagakunda, Session nundi Data tiskoni "Veedu already login ayyadu" ani cheptundi.

-----

### 🔬 Visualizing the Chain (Flow Diagram)

Nee Request Filter Chain lo ila travel avtundi:

```text
User Request (GET /)
      |
      v
[SecurityContextHolderFilter] (Checking: "Session lo emaina unda?" -> Empty)
      |
      v
[AnonymousAuthenticationFilter] (Marking: "Veedu Anonymous")
      |
      v
[ExceptionTranslationFilter] (Stopping: "Access Denied! Login Avvu")
      |
      v
[OAuth2AuthorizationRequestRedirectFilter] (Action: "Go to Google")
      |
      |  <--- (User goes to Google & Comes back with Code) ---
      |
[OAuth2LoginAuthenticationFilter] (Action: "Ah Code itu ivvu..")
      |--> Calls Google (Token Exchange)
      |--> Creates User Session
      |
      v
[AuthorizationFilter] (Checking: "Ippudu Session undi. Go inside!")
      |
      v
Your Controller (HelloController)
```

### 💡 40LPA Insight (Interview Q)

**Q: Spring Security lo form-login ni OAuth2 login ni oke app lo vadacha?**
**A:** Yes\!
Spring Boot Auto-configuration valla ippudu neeku only OAuth vachindi. Kani manam `SecurityConfig` rasi `.formLogin()` and `.oauth2Login()` renduu pedithe, User ki choice vastundi (Either Email/Pass OR Login with Google).

**Final Confirmation:**
Yes Mawa, nuvvu cheppindi 100% correct. Aa Logs lo unna `OAuth2...Filters` ye ippudu nee security guards. Patha `UsernamePasswordAuthenticationFilter` vacation ki vellipoindi.