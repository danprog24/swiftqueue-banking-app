package com.dannycode.config;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    // General API limit: 100 requests per minute per IP
    private Bucket createGeneralBucket() {
        Bandwidth limit = Bandwidth.classic(
            100,
            Refill.greedy(100, Duration.ofMinutes(1))
        );

        return Bucket.builder()
            .addLimit(limit)
            .build();
    }

    // Auth API limit: 10 requests per minute per IP
    private Bucket createAuthBucket() {
        Bandwidth limit = Bandwidth.classic(
            10,
            Refill.greedy(10, Duration.ofMinutes(1))
        );

        return Bucket.builder()
            .addLimit(limit)
            .build();
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        String ip = getClientIp(request);
        String path = request.getRequestURI();

        // Use stricter bucket for auth endpoints
        String bucketKey = isAuthEndpoint(path)
            ? "auth:" + ip
            : "api:" + ip;

        Bucket bucket = buckets.computeIfAbsent(
            bucketKey,
            k -> isAuthEndpoint(path)
                ? createAuthBucket()
                : createGeneralBucket()
        );

        if (bucket.tryConsume(1)) {

            response.setHeader(
                "X-RateLimit-Remaining",
                String.valueOf(bucket.getAvailableTokens())
            );

            filterChain.doFilter(request, response);

        } else {

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");

            response.getWriter().write(
                "{\"status\":429,\"message\":\"Too many requests. Please try again later.\"}"
            );
        }
    }

    private boolean isAuthEndpoint(String path) {
        return path.startsWith("/api/v1/auth");
    }

    private String getClientIp(HttpServletRequest request) {

        String forwardedFor = request.getHeader("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}





// package com.dannycode.config;

// import io.github.bucket4j.Bandwidth;
// import io.github.bucket4j.BucketConfiguration;
// import io.github.bucket4j.Refill;
// import io.github.bucket4j.distributed.proxy.ProxyManager;
// import jakarta.servlet.FilterChain;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import lombok.RequiredArgsConstructor;
// import org.springframework.http.HttpStatus;
// import org.springframework.stereotype.Component;
// import org.springframework.web.filter.OncePerRequestFilter;

// import java.io.IOException;
// import java.time.Duration;
// import java.util.function.Supplier;

// @Component
// @RequiredArgsConstructor
// public class RateLimitFilter extends OncePerRequestFilter {

//     private final ProxyManager<String> proxyManager;

//     @Override
//     protected void doFilterInternal(
//             HttpServletRequest request,
//             HttpServletResponse response,
//             FilterChain filterChain
//     ) throws ServletException, IOException {

//         String ip = getClientIp(request);
//         String path = request.getRequestURI();
//         boolean isAuth = isAuthEndpoint(path);

//         String bucketKey = isAuth ? "auth:" + ip : "api:" + ip;

//         Supplier<BucketConfiguration> configSupplier = isAuth
//                 ? this::authBucketConfig
//                 : this::generalBucketConfig;

//         // var bucket = proxyManager.builder().build(bucketKey, configSupplier);
//         var bucket = proxyManager.getProxy(bucketKey, configSupplier);


//         var probe = bucket.tryConsumeAndReturnRemaining(1);

//         if (probe.isConsumed()) {
//             response.setHeader("X-RateLimit-Remaining",
//                     String.valueOf(probe.getRemainingTokens()));
//             filterChain.doFilter(request, response);
//         } else {
//             long waitSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;
//             response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
//             response.setContentType("application/json");
//             response.setHeader("X-RateLimit-Retry-After", String.valueOf(waitSeconds));
//             response.getWriter().write(String.format(
//                     "{\"status\":429,\"message\":\"Too many requests. Retry after %d seconds.\"}",
//                     waitSeconds
//             ));
//         }
//     }

//     private BucketConfiguration generalBucketConfig() {
//         return BucketConfiguration.builder()
//                 .addLimit(Bandwidth.classic(
//                         100, Refill.greedy(100, Duration.ofMinutes(1))))
//                 .build();
//     }

//     private BucketConfiguration authBucketConfig() {
//         return BucketConfiguration.builder()
//                 .addLimit(Bandwidth.classic(
//                         10, Refill.greedy(10, Duration.ofMinutes(1))))
//                 .build();
//     }

//     private boolean isAuthEndpoint(String path) {
//         return path.startsWith("/api/v1/auth");
//     }

//     private String getClientIp(HttpServletRequest request) {
//         String forwardedFor = request.getHeader("X-Forwarded-For");
//         if (forwardedFor != null && !forwardedFor.isBlank()) {
//             return forwardedFor.split(",")[0].trim();
//         }
//         return request.getRemoteAddr();
//     }
// }