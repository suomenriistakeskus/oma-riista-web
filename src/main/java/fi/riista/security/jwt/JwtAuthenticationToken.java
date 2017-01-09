package fi.riista.security.jwt;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

public final class JwtAuthenticationToken implements Authentication {
    private static final long serialVersionUID = 1L;

    public static JwtAuthenticationToken createUnauthenticated(final String jwtToken,
                                                               final HttpServletRequest request) {
        final String principal = "jwtToken-" + UUID.randomUUID();
        final WebAuthenticationDetails details = new WebAuthenticationDetails(request);

        return new JwtAuthenticationToken(false, principal, jwtToken, AuthorityUtils.NO_AUTHORITIES, details);
    }

    public static JwtAuthenticationToken createAuthenticated(final UserDetails userDetails,
                                                             final WebAuthenticationDetails details) {
        return new JwtAuthenticationToken(true, userDetails, null, userDetails.getAuthorities(), details);
    }

    private final Object principal;
    private final String credentials;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean authenticated;
    private final WebAuthenticationDetails details;

    private JwtAuthenticationToken(final boolean authenticated,
                                   final Object principal,
                                   final String credentials,
                                   final Collection<? extends GrantedAuthority> authorities,
                                   final WebAuthenticationDetails details) {
        this.authenticated = authenticated;
        this.principal = Objects.requireNonNull(principal);
        this.credentials = credentials;
        this.authorities = Objects.requireNonNull(authorities);
        this.details = details;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    @Override
    public String getName() {
        if (this.getPrincipal() instanceof UserDetails) {
            return ((UserDetails) this.getPrincipal()).getUsername();
        }

        if (getPrincipal() instanceof Principal) {
            return ((Principal) getPrincipal()).getName();
        }

        return this.getPrincipal().toString();
    }

    @Override
    public WebAuthenticationDetails getDetails() {
        return this.details;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(final boolean isAuthenticated) {
        if (isAuthenticated) {
            throw new IllegalArgumentException("Cannot set this token to authenticated");
        }
    }
}
