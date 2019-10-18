package fi.riista.integration.common.export;

import com.google.common.collect.Lists;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.harvestpermit.QHarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.QHuntingClub;
import fi.riista.feature.huntingclub.permit.statistics.ClubHuntingSummaryBasicInfoDTO;
import fi.riista.feature.huntingclub.permit.statistics.ClubHuntingSummaryBasicInfoService;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import fi.riista.feature.permit.PermitNumberUtil;
import fi.riista.integration.common.export.huntingsummaries.CSUM_ClubHuntingSummary;
import fi.riista.integration.common.export.huntingsummaries.CSUM_GeoLocation;
import fi.riista.integration.common.export.huntingsummaries.CSUM_HuntingSummaries;
import fi.riista.util.JaxbUtils;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static fi.riista.integration.common.export.RvrConstants.RVR_PERMIT_TYPE_CODES;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Component
public class CommonHuntingSummaryExportFeature {

    private static final QHarvestPermit PERMIT = QHarvestPermit.harvestPermit;
    private static final QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;
    private static final QGameSpecies SPECIES = QGameSpecies.gameSpecies;
    private static final QHarvestPermitSpeciesAmount SPA = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;
    private static final QHuntingClub CLUB = QHuntingClub.huntingClub;

    private static final int PAGE_SIZE = 4096;

    private static final class PermitInfo {
        private final String permitNumber;
        private final String rhyCode;
        private final Integer rhyLatitude;
        private final Integer rhyLongitude;

        public PermitInfo(final String permitNumber, final String rhyCode,
                          final Integer rhyLatitude, final Integer rhyLongitude) {
            this.permitNumber = permitNumber;
            this.rhyCode = rhyCode;
            this.rhyLatitude = rhyLatitude;
            this.rhyLongitude = rhyLongitude;
        }

        public String getPermitNumber() {
            return permitNumber;
        }

        public String getRhyCode() {
            return rhyCode;
        }

        public Integer getRhyLatitude() {
            return rhyLatitude;
        }

        public Integer getRhyLongitude() {
            return rhyLongitude;
        }
    }

    private static final class PartnerInfo {
        private final String officialCode;
        private final String clubName;
        private final Integer latitude;
        private final Integer longitude;

        public PartnerInfo(final String officialCode, final String clubName,
                           final Integer latitude, final Integer longitude) {
            this.officialCode = officialCode;
            this.clubName = clubName;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public String getOfficialCode() {
            return officialCode;
        }

        public String getClubName() {
            return clubName;
        }

        public Integer getLatitude() {
            return latitude;
        }

        public Integer getLongitude() {
            return longitude;
        }
    }

    @Resource
    private JPQLQueryFactory queryFactory;

    @Resource(name = "commonMooseHuntingSummaryExportMarshaller")
    private Jaxb2Marshaller jaxbMarshaller;

    @Resource
    private ClubHuntingSummaryBasicInfoService summaryService;

    @Transactional(readOnly = true)
    @PreAuthorize("hasPrivilege('EXPORT_RVR_COMMON') or hasPrivilege('EXPORT_LUKE_COMMON')")
    public String exportHuntingSummariesAsXml(final int year) {
        return JaxbUtils.marshalToString(exportHuntingSummaries(year), jaxbMarshaller);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasPrivilege('EXPORT_RVR_COMMON') or hasPrivilege('EXPORT_LUKE_COMMON')")
    public CSUM_HuntingSummaries exportHuntingSummaries(final int year) {


        final Map<Long, PermitInfo> permits = findPermits(year);
        final Map<Long, PartnerInfo> clubs = findPartnerInfos(permits.keySet());
        final CSUM_HuntingSummaries summaries = new CSUM_HuntingSummaries();

        findSpeciesCodesPresentInPermits(permits.keySet())
                .stream()
                .flatMap(speciesCode ->
                        summaryService.getHuntingSummaries(permits.keySet(), speciesCode).streamSummaries())
                .filter(dto -> dto.isHuntingFinished())
                .map(dto -> mapToPojo(dto, permits, clubs))
                .forEach(summaries::withHuntingSummary);

        return summaries;
    }

    private Set<Integer> findSpeciesCodesPresentInPermits(Set<Long> permitIds) {
        return Lists.partition(Lists.newArrayList(permitIds), PAGE_SIZE)
                .stream()
                .flatMap(partition -> queryFactory
                        .select(SPECIES.officialCode)
                        .from(SPA)
                        .innerJoin(SPA.gameSpecies, SPECIES)
                        .where(SPA.harvestPermit.id.in(partition))
                        .distinct()
                        .fetch()
                        .stream())
                .collect(toSet());

    }

    private static CSUM_ClubHuntingSummary mapToPojo(final ClubHuntingSummaryBasicInfoDTO dto,
                                                     final Map<Long, PermitInfo> permits,
                                                     final Map<Long, PartnerInfo> huntingClubMap) {

        final PermitInfo permitInfo = permits.get(dto.getPermitId());
        final PartnerInfo partnerInfo = huntingClubMap.get(dto.getClubId());
        final CSUM_ClubHuntingSummary summary = new CSUM_ClubHuntingSummary();

        return summary
                .withHuntingEndDate(dto.getHuntingEndDate())
                .withPermitNumber(permitInfo.getPermitNumber())
                .withPermitYear(PermitNumberUtil.extractYear(permitInfo.getPermitNumber()))
                .withClubOfficialCode(partnerInfo.getOfficialCode())
                .withClubNameFinnish(partnerInfo.getClubName())
                .withGameSpeciesCode(dto.getGameSpeciesCode())
                .withRhyOfficialCode(permitInfo.getRhyCode())
                .withGeoLocation(createGeoLocationNullable(summary, partnerInfo, permitInfo))
                .withTotalLandAreaSize(dto.getTotalHuntingArea())
                .withAreaLandEffectiveSize(dto.getEffectiveHuntingArea())
                .withRemainingPopulationInTotalLandArea(dto.getRemainingPopulationInTotalArea())
                .withRemainingPopulationInEffectiveLandArea(dto.getRemainingPopulationInEffectiveArea());
    }

    private static CSUM_GeoLocation createGeoLocationNullable(final CSUM_ClubHuntingSummary summary,
                                                              final PartnerInfo partnerInfo,
                                                              final PermitInfo permitInfo) {
        final Integer latitude = Optional.ofNullable(partnerInfo.getLatitude()).orElse(permitInfo.getRhyLatitude());
        final Integer longitude = Optional.ofNullable(partnerInfo.getLongitude()).orElse(permitInfo.getRhyLongitude());

        if (latitude != null && longitude != null) {
            return new CSUM_GeoLocation()
                    .withLatitude(latitude)
                    .withLongitude(longitude);
        }
        return null;
    }

    private Map<Long, PermitInfo> findPermits(final int year) {
        return queryFactory
                .select(
                        PERMIT.id,
                        PERMIT.permitNumber,
                        RHY.officialCode,
                        RHY.geoLocation.latitude,
                        RHY.geoLocation.longitude)
                .from(PERMIT)
                .innerJoin(PERMIT.rhy, RHY)
                .where(PERMIT.permitTypeCode.in(RVR_PERMIT_TYPE_CODES)
                        .and(PERMIT.permitYear.eq(year)))
                .fetch()
                .stream()
                .collect(toMap(
                        keyTuple -> keyTuple.get(PERMIT.id),
                        valueTuple -> new PermitInfo(
                                valueTuple.get(PERMIT.permitNumber),
                                valueTuple.get(RHY.officialCode),
                                valueTuple.get(RHY.geoLocation.latitude),
                                valueTuple.get(RHY.geoLocation.longitude))));
    }

    private Map<Long, PartnerInfo> findPartnerInfos(Set<Long> permitIds) {
        return Lists.partition(Lists.newArrayList(permitIds), PAGE_SIZE)
                .stream()
                .flatMap(partition -> queryFactory
                        .select(
                                CLUB.id,
                                CLUB.officialCode,
                                CLUB.nameFinnish,
                                CLUB.geoLocation.latitude,
                                CLUB.geoLocation.longitude)
                        .from(PERMIT)
                        .where(PERMIT.id.in(partition))
                        .innerJoin(PERMIT.permitPartners, CLUB)
                        .distinct()
                        .fetch()
                        .stream())
                .collect(Collectors.toMap(
                        keyTuple -> keyTuple.get(CLUB.id),
                        valueTuple -> new PartnerInfo(
                                valueTuple.get(CLUB.officialCode),
                                valueTuple.get(CLUB.nameFinnish),
                                valueTuple.get(CLUB.geoLocation.latitude),
                                valueTuple.get(CLUB.geoLocation.longitude))));

    }
}
