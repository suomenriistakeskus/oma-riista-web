package fi.riista.security.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger LOG = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String AUTHORIZATION_PREFIX = "Bearer ";

    private final AuthenticationEntryPoint entryPoint;
    private final AuthenticationManager authenticationManager;

    public JwtAuthenticationFilter(final AuthenticationManager authenticationManager,
                                   final AuthenticationEntryPoint entryPoint) {
        this.authenticationManager = authenticationManager;
        this.entryPoint = entryPoint;
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain chain) throws ServletException, IOException {
        final JwtAuthenticationToken token = parseToken(request);

        if (token != null && authenticationIsRequired()) {
            try {
                final Authentication auth = authenticationManager.authenticate(token);
                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (final AuthenticationException e) {
                LOG.warn("JWT authentication has failed", e);
                SecurityContextHolder.clearContext();
                entryPoint.commence(request, response, e);
                return;
            }

            try {
                chain.doFilter(request, response);
            } finally {
                // Always use temporary JWT authentication
                SecurityContextHolder.clearContext();
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    private static JwtAuthenticationToken parseToken(final HttpServletRequest request) {
        final String header = request.getHeader(HEADER_AUTHORIZATION);

        if (header == null || !header.startsWith(AUTHORIZATION_PREFIX)) {
            return null;
        }

        final String jwtToken = header.substring(AUTHORIZATION_PREFIX.length());

        return JwtAuthenticationToken.createUnauthenticated(jwtToken, request);
    }

    private static boolean authenticationIsRequired() {
        final Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();

        return existingAuth == null || !existingAuth.isAuthenticated() ||
                existingAuth instanceof AnonymousAuthenticationToken;
    }
}
