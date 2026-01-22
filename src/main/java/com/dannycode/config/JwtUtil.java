// package com.dannycode.config;

// import java.util.Date;

// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.stereotype.Component;

// @Component
// public class JwtUtils {

//     @Value("${jwt.secret}")
//     private String jwtSecret;
//     private static final long jwtExpiration = 86400000; // 1 day in milliseconds

//     public String generateToken(String email) {
//        return Jwts.builder()
//                 .setSubject(email)
//                 .setIssuedAt(new Date())
//                 .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
//                 .signWith(getKey(),SignatureAlgorithm.HS512, jwtSecret)
//                 .compact();

//     }
//     public String extractEmail(String token) {
//         return Jwts.parser()
//                 .setSigningKey(jwtSecret)
//                 .parseClaimsJws(token)
//                 .getBody()
//                 .getSubject();
//     }

//     public boolean validateToken(String token) {
//         try {
//             Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
//             return true;
//         } catch (Exception e) {
        
//         return false;
        
//         }
//     }

// }







package com.dannycode.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    // Inject secret from application.properties
    @Value("${jwt.secret}")
    private String secret;

    // expiration pattern
    private static final long jwtExpirationMs = 86400000; // 1 day

    // Internal: convert secret string to Key
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Generate JWT
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Extract username/email from token
    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // Validate token
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
