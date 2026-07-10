**JWT vs OAuth 2.0**

Many people confuse JWT and OAuth 2.0, but they solve different problems.

- JWT is a token format, a compact and self-contained way to represent claims between two parties
- OAuth 2.0 is an authorization framework, a set of rules for how a client gets permission to access a resource on behalf of a user

A system can use OAuth 2.0 as the authorization flow and JWT as the format of the access token it issues. They are complementary, not competing.

**Difference Between 401 and 403**

```
| Status | Meaning                                                   |
|--------|-----------------------------------------------------------|
| 401    | User is not authenticated, no valid identity was provided |
| 403    | User is authenticated but lacks permission for this resource |
```

**Step-by-Step: JWT Token-Based Authentication in Spring Boot**

We generate two tokens.

- Access token, used to call the protected APIs, short lived
- Refresh token, used to silently regenerate a new access token so the user stays logged in without re-entering credentials

Access token expiry is kept short at 15 minutes. Refresh token expiry is kept much longer, for example 7 days, since it is only used to mint new access tokens and is stored more carefully on the client.

**1. Add Dependencies (Spring Boot 3.x)**

These dependencies include Spring Security, Web, and the JJWT library for token generation and validation.

`pom.xml`

```xml
<dependencies>
    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Spring Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- JJWT Library -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.11.5</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.11.5</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.11.5</version>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

**2. JWT Utility to Generate and Validate Both Tokens**

This utility class creates and validates access and refresh tokens, extracts the username, and checks expiration. The expiry values now match the 15 minute access token and 7 day refresh token described above, and a refresh token generator has been added since the original snippet described two tokens but only implemented one.

`JwtUtil.java`

```java
package com.example.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final String SECRET_KEY = "thisisaverysecuresecretkey1234567890"; // minimum 256-bit

    private static final long ACCESS_TOKEN_EXPIRATION = 1000L * 60 * 15;          // 15 minutes
    private static final long REFRESH_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24 * 7; // 7 days

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // Generate short lived access token
    public String generateAccessToken(String username) {
        return buildToken(username, ACCESS_TOKEN_EXPIRATION);
    }

    // Generate long lived refresh token
    public String generateRefreshToken(String username) {
        return buildToken(username, REFRESH_TOKEN_EXPIRATION);
    }

    private String buildToken(String username, long expirationMillis) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Extract username from either token type
    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // Validate token against a given username
    public boolean validateToken(String token, String username) {
        final String extracted = extractUsername(token);
        return extracted.equals(username) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        Date expiration = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
        return expiration.before(new Date());
    }
}
```

**3. Security Configuration**

This sets up stateless JWT authentication for protected resources. The login and refresh endpoints are permitted without authentication since a user has no access token yet when calling either of them.

`SecurityConfig.java`

```java
package com.example.security.config;

import com.example.security.jwt.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/login", "/auth/refresh").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
```

**4. JWT Filter (for incoming requests)**

This filter intercepts each request, extracts the access token, validates it, and sets the authentication context.

`JwtFilter.java`

```java
package com.example.security.jwt;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        String username = null;
        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(token);
            } catch (Exception e) {
                // Invalid or expired token, request proceeds unauthenticated
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtUtil.validateToken(token, username)) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
```

**5. Authentication Controller**

Used to test login, access token issuance, and refresh token exchange. The original snippet only had a login endpoint returning a single token, so a refresh endpoint has been added to complete the two-token flow.

`AuthController.java`

```java
package com.example.security.controller;

import com.example.security.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        if ("admin".equals(request.username()) && "admin".equals(request.password())) {
            String accessToken = jwtUtil.generateAccessToken(request.username());
            String refreshToken = jwtUtil.generateRefreshToken(request.username());
            return ResponseEntity.ok(new TokenResponse(accessToken, refreshToken));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody RefreshRequest request) {
        try {
            String username = jwtUtil.extractUsername(request.refreshToken());
            if (jwtUtil.validateToken(request.refreshToken(), username)) {
                String newAccessToken = jwtUtil.generateAccessToken(username);
                String newRefreshToken = jwtUtil.generateRefreshToken(username);
                return ResponseEntity.ok(new TokenResponse(newAccessToken, newRefreshToken));
            }
        } catch (Exception e) {
            // Falls through to unauthorized response below
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    public record LoginRequest(String username, String password) {}
    public record RefreshRequest(String refreshToken) {}
    public record TokenResponse(String accessToken, String refreshToken) {}
}
```

**6. Sample Protected Endpoint**

`ApiController.java`

```java
package com.example.security.controller;

import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api")
public class ApiController {

    @GetMapping("/hello")
    public String hello(Principal principal) {
        return "Hello, " + principal.getName();
    }
}
```

**Interview Questions and Answers**

**Q: If OAuth 2.0 and JWT solve different problems, why are they so often mentioned together?**

A: OAuth 2.0 defines the authorization flow, how a client obtains permission and a token from an authorization server. JWT is frequently chosen as the format for the access token that OAuth 2.0 issues, because it is self-contained and can be validated without a database lookup. So in practice they are often used together, OAuth 2.0 as the protocol and JWT as the token format, but you could use OAuth 2.0 with opaque tokens instead, or use JWT outside of any OAuth 2.0 flow entirely.

**Q: Why do we need both an access token and a refresh token instead of just one long lived token?**

A: A single long lived token increases the damage if it is stolen, since it stays valid for a long time. Splitting into a short lived access token and a longer lived refresh token limits the exposure window for a leaked access token to minutes, while the refresh token, which is used less frequently and can be stored more securely, keeps the user logged in without repeated password entry.

**Q: Where should the refresh token be stored on the client, and why does that matter?**

A: The refresh token should be stored in an HttpOnly, Secure cookie rather than local storage, since JavaScript cannot read an HttpOnly cookie, which reduces the risk of theft through a cross site scripting attack. Storing it in local storage would expose it to any injected script running on the page.

**Q: What happens if a refresh token is stolen? How do you mitigate that?**

A: A stolen refresh token lets an attacker keep generating new access tokens indefinitely. Mitigations include refresh token rotation, where each use of a refresh token issues a new one and invalidates the old one, combined with server side tracking so a reused, already-rotated refresh token is immediately flagged as a replay attempt and the whole token family is revoked.

**Q: Why does the security filter chain need to permit `/auth/login` and `/auth/refresh` without authentication?**

A: Both endpoints are called before the client has a valid access token, login because no token exists yet, and refresh because the access token has already expired. If these routes required authentication, the user would be stuck with no way to obtain a new token.

**Q: What is the practical difference between returning 401 versus 403 from a protected endpoint, and why does it matter to get this right?**

A: 401 tells the client the request carried no valid credentials at all, so the client's correct response is to try authenticating again, for example triggering a refresh token flow. 403 tells the client the identity was accepted but the action is not permitted, so retrying with a new token would not help. Returning the wrong code can send a client into a pointless refresh-and-retry loop when the real issue is a permissions problem.