package fi.riista.feature.account.certificate;

import fi.riista.util.JCEUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;

@Component
public class HuntingCardQRCodeKeyHolder {

    private static final Logger LOG = LoggerFactory.getLogger(HuntingCardQRCodeKeyHolder.class);

    @Value("${hunting.card.signature.private.key}")
    private String privateKeyText;

    private PrivateKey privateKey;

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void decodePrivateKey() {
        if (StringUtils.isBlank(privateKeyText)) {
            return;
        }
        try {
            this.privateKey = JCEUtil.loadEllipticCurvePkcs8PrivateKey(this.privateKeyText);
            this.privateKeyText = null;
        } catch (Exception e) {
            LOG.error("Could not load private key", e);
        }
    }
}
