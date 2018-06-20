package fi.riista.feature.permit.area.partner;

import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.permit.area.HarvestPermitArea;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class HarvestPermitAreaPartnerService {

    @Resource
    private HarvestPermitAreaPartnerRepository harvestPermitAreaPartnerRepository;

    @Resource
    private HarvestPermitAreaPartnerDTOTransformer partnerDTOTransformer;

    @Resource
    private GISZoneRepository zoneRepository;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<HarvestPermitAreaPartnerDTO> listPartners(final HarvestPermitArea harvestPermitArea,
                                                          final Locale locale) {
        final List<HarvestPermitAreaPartner> partners = harvestPermitAreaPartnerRepository
                .findByHarvestPermitArea(harvestPermitArea);

        return partnerDTOTransformer.transformAndSort(partners, locale);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public HarvestPermitAreaPartner addPartner(final HarvestPermitArea harvestPermitArea,
                                               final HuntingClubArea clubArea) {
        harvestPermitArea.assertStatus(HarvestPermitArea.StatusCode.INCOMPLETE);

        // Club area must Zone with non-empty geometry
        if (clubArea.isGeometryEmpty()) {
            throw new NotFoundException("Specified area is empty");
        }

        if (clubArea.getHuntingYear() != harvestPermitArea.getHuntingYear()) {
            throw new HarvestPermitAreaHuntingYearException(
                    clubArea.getHuntingYear(), harvestPermitArea.getHuntingYear());
        }

        final Long zoneId = clubArea.getZone().getId();
        final Set<Integer> uniqueMetsahallitusYears = zoneRepository.getUniqueMetsahallitusYears(zoneId);

        // Any MH geometries used?
        if (!uniqueMetsahallitusYears.isEmpty()) {
            if (uniqueMetsahallitusYears.size() > 1) {
                // Multiple material years used
                throw new MetsahallitusYearMismatchException(String.format(
                        "Multiple MH material years for zoneId=%d", zoneId));
            }

            if (!uniqueMetsahallitusYears.contains(clubArea.getHuntingYear())) {
                // Material year mismatch
                throw new MetsahallitusYearMismatchException(String.format(
                        "MH year should equal huntingYear %d for zoneId=%d", clubArea.getHuntingYear(), zoneId));
            }
        }

        return harvestPermitArea.findPartner(clubArea).map(partner -> {
            // Refresh zone geometry data
            zoneRepository.copyZone(clubArea.getZone(), partner.getZone());
            partner.forceRevisionUpdate();
            return partner;

        }).orElseGet(() -> {
            final GISZone zoneCopy = zoneRepository.copyZone(clubArea.getZone(), new GISZone());
            final HarvestPermitAreaPartner partner = new HarvestPermitAreaPartner(harvestPermitArea, clubArea, zoneCopy);
            return harvestPermitAreaPartnerRepository.save(partner);
        });
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void removePartner(final HarvestPermitArea harvestPermitArea, final long partnerId) {
        harvestPermitArea.assertStatus(HarvestPermitArea.StatusCode.INCOMPLETE);

        final HarvestPermitAreaPartner partner = harvestPermitAreaPartnerRepository.getOne(partnerId);

        if (!partner.getHarvestPermitArea().equals(harvestPermitArea)) {
            throw new IllegalStateException("Wrong permitArea parent");
        }

        final GISZone zone = partner.getZone();
        harvestPermitAreaPartnerRepository.delete(partner);

        zone.setMetsahallitusHirvi(Collections.emptySet());
        zoneRepository.removeZonePalstaAndFeatures(zone);
        zoneRepository.delete(zone);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void refreshPartner(final HarvestPermitArea harvestPermitArea, final long partnerId) {
        harvestPermitArea.assertStatus(HarvestPermitArea.StatusCode.INCOMPLETE);

        final HarvestPermitAreaPartner partner = harvestPermitAreaPartnerRepository.getOne(partnerId);

        if (!partner.getHarvestPermitArea().equals(harvestPermitArea)) {
            throw new IllegalStateException("Partner is not linked to given application");
        }

        zoneRepository.copyZone(partner.getSourceArea().getZone(), partner.getZone());
        partner.forceRevisionUpdate();
    }
}
