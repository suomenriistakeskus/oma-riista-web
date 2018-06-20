package fi.riista.feature.huntingclub.area;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gis.metsahallitus.MetsahallitusProperties;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.huntingclub.copy.CopyClubGroupService;
import fi.riista.feature.huntingclub.copy.HuntingClubAreaCopyDTO;
import fi.riista.security.EntityPermission;
import fi.riista.util.Locales;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.Optional;

@Component
public class HuntingClubAreaCopyFeature {

    @Resource
    private HuntingClubAreaRepository huntingClubAreaRepository;

    @Resource
    private HuntingClubAreaDTOTransformer huntingClubAreaDTOTransformer;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private CopyClubGroupService copyClubGroupService;

    @Resource
    private GISZoneRepository gisZoneRepository;

    @Resource
    private MessageSource messageSource;

    @Resource
    private SecureRandom secureRandom;

    @Resource
    private MetsahallitusProperties metsahallitusProperties;

    @Transactional
    public HuntingClubAreaDTO copy(final HuntingClubAreaCopyDTO dto) {
        return huntingClubAreaDTOTransformer.apply(copyWithoutTransform(dto));
    }

    @Transactional
    public HuntingClubArea copyWithoutTransform(final HuntingClubAreaCopyDTO dto) {
        final HuntingClubArea originalArea = requireEntityService.requireHuntingClubArea(
                dto.getId(), EntityPermission.CREATE);
        final boolean useSuffix = originalArea.getHuntingYear() == dto.getHuntingYear();

        final HuntingClubArea area = new HuntingClubArea(
                originalArea.getClub(),
                originalArea.getNameFinnish() + (useSuffix ? suffix(Locales.FI) : ""),
                originalArea.getNameSwedish() + (useSuffix ? suffix(Locales.SV) : ""),
                dto.getHuntingYear(),
                HuntingClubArea.calculateMetsahallitusYear(dto.getHuntingYear(), metsahallitusProperties.getLatestMetsahallitusYear()),
                null);

        area.generateAndStoreExternalId(secureRandom);

        Optional.ofNullable(originalArea.getZone())
                .map(originalZone -> gisZoneRepository.copyZone(originalZone, new GISZone()))
                .ifPresent(area::setZone);

        huntingClubAreaRepository.saveAndFlush(area);

        if (dto.isCopyGroups()) {
            copyClubGroupService.copyGroupsHavingArea(originalArea, area);
        }
        return area;
    }

    private String suffix(Locale locale) {
        return " " + messageSource.getMessage("copy.suffix.caps", null, locale);
    }

}
