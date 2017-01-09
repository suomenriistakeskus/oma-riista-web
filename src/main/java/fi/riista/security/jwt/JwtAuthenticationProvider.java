package fi.riista.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.SigningKeyResolver;
import io.jsonwebtoken.UnsupportedJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class JwtAuthenticationProvider implements AuthenticationProvider {
    private static final Logger LOG = LoggerFactory.getLogger(JwtAuthenticationProvider.class);

    public static final String AUD_LOGIN = "login";
    public static final SignatureAlgorithm JWT_SIGNATURE_ALG = SignatureAlgorithm.HS512;

    private final UserDetailsService userDetailsService;
    private final SigningKeyResolver signingKeyResolver;

    public JwtAuthenticationProvider(final byte[] jwtSecret,
                                     final UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
        this.signingKeyResolver = new MandatoryAlgorithmJwtKeyProvider(JWT_SIGNATURE_ALG, jwtSecret);
    }

    @Override
    public Authentication authenticate(final Authentication authentication) {
        final JwtAuthenticationToken authRequest = (JwtAuthenticationToken) authentication;
        final Jws<Claims> claimsJws = parserAndVerify(authRequest);

        if (claimsJws.getBody().getExpiration() == null) {
            throw new BadCredentialsException("Only temporary JWT supported");
        }

        final String username = claimsJws.getBody().getSubject();
        final UserDetails userDetails;

        try {
            userDetails = userDetailsService.loadUserByUsername(username);
        } catch (final UsernameNotFoundException notFound) {
            throw new BadCredentialsException("Bad credentials");
        }

        if (!userDetails.isAccountNonLocked()) {
            throw new LockedException("User account is locked");
        }

        if (!userDetails.isEnabled()) {
            throw new DisabledException("User is disabled");
        }

        if (!userDetails.isAccountNonExpired()) {
            throw new AccountExpiredException("User account has expired");
        }

        if (!userDetails.isCredentialsNonExpired()) {
            throw new CredentialsExpiredException("User credentials have expired");
        }

        LOG.info("Successful JWT authentication for username={}", userDetails.getUsername());

        return JwtAuthenticationToken.createAuthenticated(userDetails, authRequest.getDetails());
    }

    private Jws<Claims> parserAndVerify(final JwtAuthenticationToken jwtToken) {
        try {
            return Jwts.parser()
                    .setSigningKeyResolver(this.signingKeyResolver)
                    .requireAudience(AUD_LOGIN)
                    .parseClaimsJws(jwtToken.getCredentials());

        } catch (final ExpiredJwtException e) {
            throw new BadCredentialsException("Expired JWT", e);

        } catch (final UnsupportedJwtException | MalformedJwtException |
                SignatureException | IllegalArgumentException e) {
            throw new BadCredentialsException("Invalid JWT", e);

        } catch (final RuntimeException e) {
            throw new BadCredentialsException("Unknown JWT exception", e);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
