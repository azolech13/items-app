package pl.com.betacom.itemsapp.infrastructure.adapter.in.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
class LoginRateLimitFilter extends OncePerRequestFilter {

    private final LoginRateLimitProperties properties;
    private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !(HttpMethod.POST.matches(request.getMethod()) && "/login".equals(request.getServletPath()));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        Bucket bucket = buckets.computeIfAbsent(request.getRemoteAddr(), address -> newBucket());
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            filterChain.doFilter(request, response);
            return;
        }
        long retryAfterSeconds = Math.max(1L, (probe.getNanosToWaitForRefill() + 1_000_000_000L - 1) / 1_000_000_000L);
        log.warn("Login rate limit exceeded for {}", request.getRemoteAddr());
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader(HttpHeaders.RETRY_AFTER, Long.toString(retryAfterSeconds));
    }

    private Bucket newBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(properties.getAttempts())
                .refillIntervally(properties.getAttempts(), properties.getWindow())
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
