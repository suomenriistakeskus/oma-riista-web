package fi.riista.feature.shootingtest.expiry;

import com.github.jknack.handlebars.Handlebars;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Set;

@Component
public class ShootingTestEndOfYearExpiryEmailFactory {

    @Resource
    private Handlebars handlebars;

    @Nonnull
    public ShootingTestEndOfYearExpiryEmail buildEmail(final List<ShootingTestEndOfYearExpiryDTO> dtos,
                                                       final Set<String> recipients) {

        final String subject = "Avoimet ampumakokeet / Öppna skjutprovstillfället";
        String body_fi;
        String body_sv;
        try {
            body_fi = handlebars.compile("email_shootingtest_expiry").apply(dtos);
            body_sv = handlebars.compile("email_shootingtest_expiry_sv").apply(dtos);
        } catch (IOException e) {
            throw new RuntimeException("Could not render template", e);
        }

        return new ShootingTestEndOfYearExpiryEmail(subject, body_fi + "\n<hr/>\n" + body_sv, recipients);
    }
}
