package pl.com.betacom.itemsapp.infrastructure.adapter.in.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pl.com.betacom.itemsapp.application.port.out.TokenProvider;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenProvider tokenProvider;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!HttpMethod.POST.matches(request.getMethod())) {
            return false;
        }
        String path = request.getServletPath();
        return "/login".equals(path) || "/register".equals(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }
        tokenProvider.parseUserId(header.substring(BEARER_PREFIX.length()))
                .ifPresentOrElse(
                        userId -> continueWithUser(userId, request, response, filterChain),
                        () -> reject(response));
    }

    @SneakyThrows
    private void continueWithUser(UUID userId,
                                  HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) {
        SecurityContextHolder.getContext().setAuthentication(createAuthentication(userId));
        filterChain.doFilter(request, response);
    }

    @SneakyThrows
    private void reject(HttpServletResponse response) {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @NonNull
    private static UsernamePasswordAuthenticationToken createAuthentication(UUID userId) {
        return new UsernamePasswordAuthenticationToken(userId, null, List.of());
    }
}
