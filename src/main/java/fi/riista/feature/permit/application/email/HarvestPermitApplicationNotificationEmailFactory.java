package fi.riista.feature.permit.application.email;

import com.github.jknack.handlebars.Handlebars;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

import static java.util.Objects.requireNonNull;

@Component
public class HarvestPermitApplicationNotificationEmailFactory {

    @Resource
    private Handlebars handlebars;

    @Resource
    private MessageSource messageSource;

    @Nonnull
    public HarvestPermitApplicationNotificationEmail create(final @Nonnull HarvestPermitApplicationNotificationDTO dto) {
        return HarvestPermitApplicationNotificationEmail.create(handlebars, messageSource, requireNonNull(dto));
    }
}
