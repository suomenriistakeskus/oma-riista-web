package fi.riista.feature.permit.application.email;

import com.github.jknack.handlebars.Handlebars;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.gis.zone.GISZoneSizeDTO;
import fi.riista.feature.mail.MailService;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.util.Locales;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Locale;

@Component
public class HarvestPermitApplicationNotificationService {
    private static final Logger LOG = LoggerFactory.getLogger(HarvestPermitApplicationNotificationService.class);

    @Resource
    private Handlebars handlebars;

    @Resource
    private MessageSource messageSource;

    @Resource
    private MailService mailService;

    @Resource
    private GISZoneRepository gisZoneRepository;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = Exception.class)
    public void sendNotification(final HarvestPermitApplication application) {
        application.assertHasPermitArea();

        final GISZoneSizeDTO areaSize = gisZoneRepository.getAreaSize(application.getArea().getZone().getId());
        final String contactPersonEmail = application.getContactPerson().getEmail();
        final Locale locale = Locales.getLocaleByLanguageCode(application.getContactPerson().getLanguageCode());

        if (StringUtils.isBlank(contactPersonEmail)) {
            LOG.warn("Empty recipient list for applicationId={}", application.getId());
            return;
        }

        mailService.send(new HarvestPermitApplicationNotification(handlebars, messageSource)
                .withApplication(application)
                .withLocale(locale)
                .withAreaSize(areaSize)
                .withRecipients(Collections.singleton(contactPersonEmail))
                .createMailMessage(mailService.getDefaultFromAddress()));
    }
}
