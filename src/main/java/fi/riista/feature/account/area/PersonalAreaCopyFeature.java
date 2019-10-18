package fi.riista.feature.account.area;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.security.EntityPermission;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.security.SecureRandom;
import java.util.Locale;

@Component
public class PersonalAreaCopyFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private PersonalAreaRepository personalAreaRepository;

    @Resource
    private GISZoneRepository gisZoneRepository;

    @Resource
    private MessageSource messageSource;

    @Resource
    private SecureRandom secureRandom;

    @Transactional
    public void copy(final long id, final Locale locale) {
        final PersonalArea originalArea = requireEntityService.requirePersonalArea(id, EntityPermission.READ);

        final PersonalArea area = new PersonalArea();
        area.generateAndStoreExternalId(secureRandom);
        area.setPerson(originalArea.getPerson());
        area.setName(originalArea.getName() + suffix(locale));

        if (originalArea.getZone() != null) {
            area.setZone(gisZoneRepository.copyZone(originalArea.getZone(), new GISZone()));
        }

        personalAreaRepository.saveAndFlush(area);
    }

    private String suffix(final Locale locale) {
        return " " + messageSource.getMessage("copy.suffix.caps", null, locale);
    }
}
