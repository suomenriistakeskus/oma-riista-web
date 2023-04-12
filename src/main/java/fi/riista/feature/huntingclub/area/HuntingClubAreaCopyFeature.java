package fi.riista.feature.huntingclub.area;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.area.PersonalArea;
import fi.riista.feature.account.area.PersonalAreaRepository;
import fi.riista.feature.gis.metsahallitus.MetsahallitusMaterialYear;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.huntingclub.copy.CopyClubGroupService;
import fi.riista.feature.huntingclub.copy.CopyClubPOIsService;
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
    private PersonalAreaRepository personalAreaRepository;

    @Resource
    private CopyClubGroupService copyClubGroupService;

    @Resource
    private CopyClubPOIsService copyClubPOIsService;

    @Resource
    private GISZoneRepository gisZoneRepository;

    @Resource
    private MessageSource messageSource;

    @Resource
    private SecureRandom secureRandom;

    @Resource
    private MetsahallitusMaterialYear metsahallitusMaterialYear;

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
                HuntingClubArea.calculateMetsahallitusYear(dto.getHuntingYear(),
                        metsahallitusMaterialYear.getLatestHirviYear()),
                null);

        area.generateAndStoreExternalId(secureRandom);

        Optional.ofNullable(originalArea.getZone())
                .map(originalZone -> gisZoneRepository.copyZone(originalZone, new GISZone()))
                .ifPresent(area::setZone);

        huntingClubAreaRepository.saveAndFlush(area);
        huntingClubAreaRepository.calculateZoneChanges(
                area.getId());

        if (dto.isCopyGroups()) {
            copyClubGroupService.copyGroupsHavingArea(originalArea, area);
        }
        if(dto.isCopyPOIs()) {
            copyClubPOIsService.copyPOIsHavingArea(originalArea, area);
        }
        return area;
    }

    @Transactional
    public HuntingClubAreaDTO importFromPersonalArea(final long clubAreaId, final long personalAreaId) {
        final HuntingClubArea clubArea = requireEntityService.requireHuntingClubArea(
                clubAreaId, EntityPermission.UPDATE);

        // No separate authorization for personal area since club contact person might not be the author of
        // the personal area

        personalAreaRepository.findById(personalAreaId)
                .map(PersonalArea::getZone)
                .map(personalAreaZone -> gisZoneRepository.copyZone(personalAreaZone,
                        Optional.ofNullable(clubArea.getZone()).orElseGet(GISZone::new)))
                .ifPresent(clubArea::setZone);

        return huntingClubAreaDTOTransformer.apply(clubArea);
    }

    private String suffix(Locale locale) {
        return " " + messageSource.getMessage("copy.suffix.caps", null, locale);
    }

}
