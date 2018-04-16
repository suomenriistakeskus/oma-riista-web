package fi.riista.feature.harvestpermit.area;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.huntingclub.area.HuntingClubAreaRepository;
import fi.riista.security.EntityPermission;
import fi.riista.util.Locales;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Component
public class HarvestPermitAreaPartnersFeature {
    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private HarvestPermitAreaPartnerRepository harvestPermitAreaPartnerRepository;

    @Resource
    private HuntingClubAreaRepository huntingClubAreaRepository;

    @Resource
    private GISZoneRepository gisZoneRepository;

    @Resource
    private HarvestPermitAreaPartnerDTOTransformer dtoTransformer;

    @Resource
    private EnumLocaliser localiser;

    private HarvestPermitArea requirePermitArea(final long harvestPermitAreaId, final EntityPermission permission) {
        return requireEntityService.requireHarvestPermitArea(harvestPermitAreaId, permission);
    }

    @Transactional(readOnly = true)
    public List<HarvestPermitAreaPartnerDTO> list(final long harvestPermitAreaId, final Locale locale) {
        final HarvestPermitArea harvestPermitArea = requirePermitArea(harvestPermitAreaId, EntityPermission.READ);
        return list(locale, harvestPermitArea);
    }

    @Transactional(readOnly = true)
    public HarvestPermitAreaPartnersExcelView listExcel(final long harvestPermitAreaId, final Locale locale) {
        final HarvestPermitArea harvestPermitArea = requirePermitArea(harvestPermitAreaId, EntityPermission.READ);
        final String externalId = harvestPermitArea.getExternalId();
        final List<HarvestPermitAreaPartnerDTO> partners = list(locale, harvestPermitArea);
        return new HarvestPermitAreaPartnersExcelView(localiser, locale, externalId, partners);
    }

    private List<HarvestPermitAreaPartnerDTO> list(final Locale locale, final HarvestPermitArea harvestPermitArea) {
        final List<HarvestPermitAreaPartnerDTO> partners = dtoTransformer.transform(harvestPermitAreaPartnerRepository.findByHarvestPermitArea(harvestPermitArea));
        final Comparator<HarvestPermitAreaPartnerDTO> byClub = Comparator.comparing(p -> p.getClub().getNameLocalisation().getAnyTranslation(locale));
        final Comparator<HarvestPermitAreaPartnerDTO> byArea = Comparator.comparing(p -> p.getSourceArea().getName().getOrDefault(locale.getLanguage(), Locales.FI_LANG));
        final Comparator<HarvestPermitAreaPartnerDTO> byExternalId = Comparator.comparing(p -> p.getSourceArea().getExternalId());
        partners.sort(byClub.thenComparing(byArea).thenComparing(byExternalId));
        return partners;
    }

    @Transactional
    public HarvestPermitAreaPartnerDTO add(final long harvestPermitAreaId, final String externalId) {
        final HarvestPermitArea harvestPermitArea = requirePermitArea(harvestPermitAreaId, EntityPermission.UPDATE);
        harvestPermitArea.assertStatus(HarvestPermitArea.StatusCode.INCOMPLETE);

        return huntingClubAreaRepository.findByExternalId(externalId)
                // Make sure area is defined
                .filter(area -> area.getZone() != null
                        && area.getZone().getGeom() != null
                        && !area.getZone().getGeom().isEmpty())
                .map(clubArea -> {
                    final HarvestPermitAreaPartner result = harvestPermitArea.findPartner(clubArea)
                            .map(partner -> updatePartner(clubArea, partner))
                            .orElseGet(() -> createPartner(harvestPermitArea, clubArea));

                    return dtoTransformer.apply(result);
                })
                .orElseThrow(() -> new NotFoundException("Could not find club area by externalId"));
    }

    @Transactional
    public HarvestPermitAreaPartnerDTO updateGeometry(final long partnerId) {
        final HarvestPermitAreaPartner partner = harvestPermitAreaPartnerRepository.getOne(partnerId);

        final HarvestPermitArea harvestPermitArea = partner.getHarvestPermitArea();
        activeUserService.checkHasPermission(harvestPermitArea, EntityPermission.UPDATE);
        harvestPermitArea.assertStatus(HarvestPermitArea.StatusCode.INCOMPLETE);

        updatePartner(partner.getSourceArea(), partner);
        return dtoTransformer.apply(partner);
    }

    private HarvestPermitAreaPartner updatePartner(final HuntingClubArea clubArea,
                                                   final HarvestPermitAreaPartner partner) {
        gisZoneRepository.copyZone(clubArea.getZone(), partner.getZone());
        partner.forceRevisionUpdate();
        return partner;
    }

    private HarvestPermitAreaPartner createPartner(final HarvestPermitArea harvestPermitArea,
                                                   final HuntingClubArea clubArea) {
        final GISZone zoneCopy = gisZoneRepository.copyZone(clubArea.getZone(), new GISZone());

        return harvestPermitAreaPartnerRepository.save(
                new HarvestPermitAreaPartner(harvestPermitArea, clubArea, zoneCopy));
    }

    @Transactional
    public void remove(final long harvestPermitAreaId, final long partnerId) {
        final HarvestPermitArea harvestPermitArea = requirePermitArea(harvestPermitAreaId, EntityPermission.UPDATE);
        harvestPermitArea.assertStatus(HarvestPermitArea.StatusCode.INCOMPLETE);

        final HarvestPermitAreaPartner partner = harvestPermitAreaPartnerRepository.getOne(partnerId);

        if (!partner.getHarvestPermitArea().equals(harvestPermitArea)) {
            throw new IllegalStateException("Wrong permitArea parent");
        }

        final GISZone zone = partner.getZone();
        harvestPermitAreaPartnerRepository.delete(partner);

        zone.setMetsahallitusHirvi(Collections.emptySet());
        gisZoneRepository.removeZonePalstaAndFeatures(zone);
        gisZoneRepository.delete(zone);
    }
}
