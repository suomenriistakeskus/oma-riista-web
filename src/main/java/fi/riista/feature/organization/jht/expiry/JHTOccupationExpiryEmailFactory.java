package fi.riista.feature.organization.jht.expiry;

import com.github.jknack.handlebars.Handlebars;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Component
public class JHTOccupationExpiryEmailFactory {

    @Resource
    private Handlebars handlebars;

    @Resource
    private MessageSource messageSource;

    @Nonnull
    public JHTOccupationExpiryEmail buildEmail(final JHTOccupationExpiryDTO dto,
                                               final Map<Long, Set<String>> rhyEmailMapping) {
        final Set<String> rhyEmails = rhyEmailMapping.getOrDefault(dto.getRhyId(), Collections.emptySet());

        return JHTOccupationExpiryEmail.create(handlebars, messageSource, dto, rhyEmails);
    }
}
