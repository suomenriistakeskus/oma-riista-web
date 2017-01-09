package fi.riista.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SigningKeyResolverAdapter;
import io.jsonwebtoken.UnsupportedJwtException;

public class MandatoryAlgorithmJwtKeyProvider extends SigningKeyResolverAdapter {
    private final SignatureAlgorithm requiredAlgorithm;
    private final byte[] signatureKey;

    public MandatoryAlgorithmJwtKeyProvider(final SignatureAlgorithm requiredAlgorithm, final byte[] signatureKey) {
        this.requiredAlgorithm = requiredAlgorithm;
        this.signatureKey = signatureKey;
    }

    @Override
    public byte[] resolveSigningKeyBytes(JwsHeader header, Claims claims) {
        final SignatureAlgorithm inputAlgorithm = SignatureAlgorithm.forName(header.getAlgorithm());

        if (!this.requiredAlgorithm.equals(inputAlgorithm)) {
            throw new UnsupportedJwtException("Invalid algorithm");
        }

        return signatureKey;
    }
}
