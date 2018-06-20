package fi.riista.feature.huntingclub.permit;

import com.google.common.base.MoreObjects;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.common.entity.CreditorReference;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitLockedByDateService;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountDTO;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount_;
import fi.riista.feature.harvestpermit.HarvestPermit_;
import fi.riista.feature.harvestpermit.allocation.HarvestPermitAllocationRepository;
import fi.riista.feature.harvestpermit.allocation.MoosePermitAllocationDTO;
import fi.riista.feature.harvestpermit.endofhunting.MooseHarvestReport;
import fi.riista.feature.harvestpermit.endofhunting.MooseHarvestReportDTO;
import fi.riista.feature.harvestpermit.endofhunting.MooseHarvestReportRepository;
import fi.riista.feature.harvestpermit.list.MooselikePermitListDTO;
import fi.riista.feature.harvestpermit.list.MooselikePermitListDTOBuilder;
import fi.riista.feature.harvestpermit.season.MooselikePrice;
import fi.riista.feature.harvestpermit.season.MooselikePriceRepository;
import fi.riista.feature.huntingclub.permit.basicsummary.BasicClubHuntingSummaryRepository;
import fi.riista.feature.huntingclub.permit.summary.ClubHuntingSummaryBasicInfoDTO;
import fi.riista.feature.huntingclub.permit.summary.HuntingEndStatus;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Stream;

import static fi.riista.feature.harvestpermit.HarvestPermitAuthorization.Permission.CREATE_REMOVE_MOOSE_HARVEST_REPORT;
import static fi.riista.feature.harvestpermit.HarvestPermitAuthorization.Permission.LIST_LEADERS;
import static fi.riista.util.Collect.indexingBy;
import static fi.riista.util.NumberUtils.equal;
import static fi.riista.util.NumberUtils.sum;
import static fi.riista.util.jpa.JpaSpecs.equal;
import static fi.riista.util.jpa.JpaSpecs.fetch;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.springframework.data.jpa.domain.Specifications.where;

@Component
public class HuntingClubPermitDTOFactory {

    private static final Logger LOG = LoggerFactory.getLogger(HuntingClubPermitDTOFactory.class);

    @Resource
    private HarvestPermitSpeciesAmountRepository speciesAmountRepository;

    @Resource
    private HarvestPermitAllocationRepository allocationRepository;

    @Resource
    private BasicClubHuntingSummaryRepository basicHuntingSummaryRepository;

    @Resource
    private MooselikePriceRepository mooselikePriceRepository;

    @Resource
    private MooseHarvestReportRepository mooseHarvestReportRepository;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Resource
    private HuntingClubPermitService huntingPermitService;

    @Resource
    private HarvestPermitLockedByDateService harvestPermitLockedByDateService;

    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(final DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public MooselikePermitListDTO getPermitListingDTOWithoutAuthorization
            (final HarvestPermit permit, final int speciesCode, final Long viewedClubId) {

        final GameSpecies species = gameSpeciesService.requireByOfficialCode(speciesCode);
        final HarvestPermitSpeciesAmount speciesAmount = getOneSpeciesAmount(
                speciesAmountRepository.findByHarvestPermit(permit), permit.getPermitNumber(), species);

        final Map<String, Float> amendmentPermits = getAmendmentPermits(permit, species);

        final Map<Long, HuntingClubPermitCountDTO> harvestCounts =
                huntingPermitService.getHarvestCountsGroupedByClubId(permit, speciesCode);

        final boolean amendmentPermitsMatchHarvests = checkAmendmentPermitsMatchHarvests(amendmentPermits, harvestCounts);

        final MooseHarvestReport mooseHarvestReport = mooseHarvestReportRepository.findBySpeciesAmount(speciesAmount);

        final Map<Long, ClubHuntingSummaryBasicInfoDTO> summariesByClubId =
                huntingPermitService.getHuntingSummaryBasicInfoGroupedByClubId(permit, speciesCode);

        final boolean listLeadersButtonVisible = activeUserService.checkHasPermission(permit, LIST_LEADERS);

        final boolean huntingFinished = Optional.ofNullable(viewedClubId)
                .map(summariesByClubId::get)
                .map(ClubHuntingSummaryBasicInfoDTO::isHuntingFinished)
                .orElse(false);

        final boolean huntingFinishedByModeration = Optional.ofNullable(viewedClubId)
                .map(summariesByClubId::get)
                .map(ClubHuntingSummaryBasicInfoDTO::isHuntingFinishedByModeration)
                .orElse(false);

        final boolean viewedClubIsPartner = viewedClubInPartners(viewedClubId, permit);

        return new MooselikePermitListDTOBuilder()
                .setPermit(permit)
                .setSpa(speciesAmount)
                .setSpecies(species)
                .setAmendmentPermits(amendmentPermits)
                .setViewedClubId(viewedClubId)
                .setViewedClubIsPartner(viewedClubIsPartner)
                .setAmendmentPermitsMatchHarvests(amendmentPermitsMatchHarvests)
                .setMooseHarvestReport(mooseHarvestReport)
                .setListLeadersButtonVisible(listLeadersButtonVisible)
                .setHuntingFinished(huntingFinished)
                .setHuntingFinishedByModeration(huntingFinishedByModeration)
                .setHarvests(harvestCounts.values())
                .setAllocations(allocationRepository.getAllocationsIncludeMissingPartnerDTO(permit, species))
                .createMooselikePermitListingDTO();
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public HuntingClubPermitDTO getPermitWithoutAuthorization(final HarvestPermit permit,
                                                              final int speciesCode,
                                                              final Long viewedClubId) {
        final GameSpecies species = gameSpeciesService.requireByOfficialCode(speciesCode);
        final HarvestPermitSpeciesAmount speciesAmount = getOneSpeciesAmount(
                speciesAmountRepository.findByHarvestPermit(permit), permit.getPermitNumber(), species);

        final Map<String, Float> amendmentPermits = getAmendmentPermits(permit, species);

        final Map<Long, HuntingClubPermitHuntingDayStatisticsDTO> stats = calculateStatistics(speciesAmount);

        final HuntingClubPermitHuntingDayStatisticsDTO totalStats = calculateTotalStats(stats.values());

        final int huntingYear = speciesAmount.resolveHuntingYear();
        final MooselikePrice prices = mooselikePriceRepository.getByHuntingYearAndGameSpecies(huntingYear, species);

        final Map<Long, HuntingClubPermitCountDTO> harvestCounts =
                huntingPermitService.getHarvestCountsGroupedByClubId(permit, speciesCode);

        final Map<Long, HuntingClubPermitPaymentDTO> payments = calculatePayments(prices, harvestCounts);
        final HuntingClubPermitTotalPaymentDTO totalPayment = calculateTotalPayment(payments, prices,
                speciesAmount.getDueDate(), speciesAmount.getCreditorReference(), harvestCounts);
        final Map<Long, ClubHuntingSummaryBasicInfoDTO> summariesByClubId =
                huntingPermitService.getHuntingSummaryBasicInfoGroupedByClubId(permit, speciesCode);

        final boolean amendmentPermitsMatchHarvests =
                checkAmendmentPermitsMatchHarvests(amendmentPermits, harvestCounts);

        final boolean allPartnersFinishedHunting = didAllPartnersFinishedHunting(summariesByClubId);
        final boolean canModifyEndOfHunting = isNotLocked(permit, huntingYear) &&
                activeUserService.checkHasPermission(permit, CREATE_REMOVE_MOOSE_HARVEST_REPORT);

        final MooseHarvestReport mooseHarvestReport = mooseHarvestReportRepository.findBySpeciesAmount(speciesAmount);
        final MooseHarvestReportDTO mooseHarvestReportDTO = MooseHarvestReportDTO.create(mooseHarvestReport);
        final List<MoosePermitAllocationDTO> allocations = allocationRepository.getAllocationsIncludeMissingPartnerDTO(permit, species);

        return HuntingClubPermitDTOBuilder.builder(permit)
                .withSpeciesAmount(HarvestPermitSpeciesAmountDTO.create(speciesAmount, species))
                .withAmendmentPermits(amendmentPermits)
                .withViewedClubId(viewedClubId)
                .withAllocations(allocations)
                .withHarvestCounts(harvestCounts)
                .withTotalStatistics(totalStats)
                .withStatistics(stats)
                .withSummaryForPartnersTable(summariesByClubId)
                .withAmendmentPermitsMatchHarvests(amendmentPermitsMatchHarvests)
                .withAllPartnersFinishedHunting(allPartnersFinishedHunting)
                .withCanModifyEndOfHunting(canModifyEndOfHunting)
                .withTotalPayment(totalPayment)
                .withPayments(payments)
                .withMooseHarvestReport(mooseHarvestReportDTO)
                .build();
    }

    private boolean isNotLocked(final HarvestPermit permit, final int huntingYear) {
        return activeUserService.isModeratorOrAdmin() || !harvestPermitLockedByDateService.isPermitLockedByDateForHuntingYear(permit, huntingYear);
    }

    private static boolean viewedClubInPartners(Long viewedClubId, HarvestPermit permit) {
        return F.getUniqueIds(permit.getPermitPartners()).contains(viewedClubId);
    }

    private static boolean didAllPartnersFinishedHunting(Map<Long, ? extends HuntingEndStatus> endStatusesByClubId) {
        return endStatusesByClubId.values().stream().allMatch(HuntingEndStatus::isHuntingFinished);
    }

    private static boolean checkAmendmentPermitsMatchHarvests(final Map<String, Float> amendmentPermits,
                                                              final Map<Long, HuntingClubPermitCountDTO> harvests) {
        double totalAmendmentPermits = amendmentPermits.values().stream()
                .mapToDouble(Float::doubleValue)
                .sum();

        double usedPermits = harvests.values().stream()
                .mapToDouble(c -> c.getNumberOfNonEdibleAdults() + c.getNumberOfNonEdibleYoungs() * 0.5)
                .sum();
        return equal(totalAmendmentPermits, usedPermits);
    }

    private static HarvestPermitSpeciesAmount getOneSpeciesAmount(
            final Collection<HarvestPermitSpeciesAmount> allSpeciesAmountsForPermit,
            final String permitNumber,
            final GameSpecies species) {

        final List<HarvestPermitSpeciesAmount> filteredSpeciesAmounts = allSpeciesAmountsForPermit.stream()
                .filter(speciesAmount -> Objects.equals(speciesAmount.getGameSpecies().getId(), species.getId()))
                .sorted(Comparator.comparing(HarvestPermitSpeciesAmount::getBeginDate))
                .collect(toList());
        final int numSpeciesAmounts = filteredSpeciesAmounts.size();

        if (numSpeciesAmounts > 1) {

            final Function<HarvestPermitSpeciesAmount, String> strFn =
                    spa -> MoreObjects.toStringHelper(HarvestPermitSpeciesAmount.class)
                            .add("id", spa.getId())
                            .add("amount", spa.getAmount())
                            .add("beginDate", spa.getBeginDate())
                            .add("endDate", spa.getEndDate())
                            .add("beginDate2", spa.getBeginDate2())
                            .add("endDate2", spa.getEndDate2())
                            .add("restrictionType", spa.getRestrictionType())
                            .add("restrictionAmount", spa.getRestrictionAmount())
                            .toString();

            final String warnMsg = String.format(
                    "For permit number %s and species code %d there exists (unexpectedly) %d species-amount objects."
                            + " Picking the first one from the following list:\n    %s",
                    permitNumber,
                    species.getOfficialCode(),
                    numSpeciesAmounts,
                    filteredSpeciesAmounts.stream().map(strFn).collect(joining("\n    ")));

            LOG.warn(warnMsg);
        }

        return filteredSpeciesAmounts.iterator().next();
    }

    private SortedMap<String, Float> getAmendmentPermits(final HarvestPermit permit, final GameSpecies species) {

        final Specification<HarvestPermitSpeciesAmount> speciesAmountSpec = where(
                equal(HarvestPermitSpeciesAmount_.harvestPermit, HarvestPermit_.originalPermit, permit))
                .and(equal(HarvestPermitSpeciesAmount_.gameSpecies, species))
                .and(fetch(HarvestPermitSpeciesAmount_.harvestPermit));

        final TreeMap<String, Float> result = new TreeMap<>();

        speciesAmountRepository.findAll(speciesAmountSpec).stream()
                .collect(groupingBy(speciesAmount -> speciesAmount.getHarvestPermit().getPermitNumber()))
                .forEach((permitNumber, speciesAmounts) -> {
                    result.put(permitNumber, getOneSpeciesAmount(speciesAmounts, permitNumber, species).getAmount());
                });

        return result;
    }

    private Map<Long, HuntingClubPermitHuntingDayStatisticsDTO> calculateStatistics(
            final HarvestPermitSpeciesAmount speciesAmount) {

        // Initialize empty statistics for each partner with moderated hunting summary.
        final Map<Long, HuntingClubPermitHuntingDayStatisticsDTO> results =
                basicHuntingSummaryRepository.findModeratorOverriddenHuntingSummaries(speciesAmount).stream()
                        .collect(toMap(s -> s.getClub().getId(), summary -> {
                            final HuntingClubPermitHuntingDayStatisticsDTO dto = new HuntingClubPermitHuntingDayStatisticsDTO();
                            dto.setHuntingClubId(summary.getClub().getId());
                            return dto;
                        }));

        final String sql = "SELECT\n" +
                "  daystats.club_id, daystats.day_count, daystats.hunter_count, harveststats.harvest_count,\n" +
                "  observationstats.observation_count,\n" +
                "  daystats.latest_day_update, harveststats.latest_harvest_update, observationstats.latest_observation_update\n" +
                "-- daystats\n" +
                "FROM (SELECT\n" +
                "  days.club_id,\n" +
                "  count(days.group_hunting_day_id) AS day_count,\n" +
                "  max(days.modification_time) AS latest_day_update,\n" +
                "  coalesce(sum(days.number_of_hunters), 0) AS hunter_count\n" +
                "  FROM (SELECT\n" +
                "    clubgroup.parent_organisation_id AS club_id,\n" +
                "    group_hunting_day.modification_time,\n" +
                "    group_hunting_day.group_hunting_day_id,\n" +
                "    group_hunting_day.number_of_hunters\n" +
                getPermitsClubsHuntingDaysJoin() +
                ") AS days\n" +
                "GROUP BY days.club_id\n" +
                ") AS daystats\n" +
                "-- harveststats\n" +
                getHarvestOrObservationStats("harvest", "harvest", "harveststats") +
                "-- observationstats\n" +
                getHarvestOrObservationStats("game_observation", "observation", "observationstats") +

                "ORDER BY daystats.club_id";

        final MapSqlParameterSource queryParams = new MapSqlParameterSource();
        queryParams.addValue("permitId", speciesAmount.getHarvestPermit().getId());
        queryParams.addValue("speciesCode", speciesAmount.getGameSpecies().getOfficialCode());

        jdbcTemplate.query(sql, queryParams, (rs, rowNum) -> {
            final HuntingClubPermitHuntingDayStatisticsDTO dto = new HuntingClubPermitHuntingDayStatisticsDTO();

            dto.setHuntingClubId(rs.getLong("club_id"));

            dto.setLatestUpdate(DateUtil.toLocalDateTimeNullSafe(Stream.of(
                    rs.getTimestamp("latest_day_update"),
                    rs.getTimestamp("latest_harvest_update"),
                    rs.getTimestamp("latest_observation_update")).filter(Objects::nonNull).max(Timestamp::compareTo)
                    .orElse(null)));

            dto.setDayCount(rs.getInt("day_count"));
            dto.setHunterCount(rs.getInt("hunter_count"));
            dto.setHarvestCount(rs.getInt("harvest_count"));
            dto.setObservationCount(rs.getInt("observation_count"));
            return dto;
        }).forEach(dto -> results.putIfAbsent(dto.getHuntingClubId(), dto));

        return results;
    }

    private static String getPermitsClubsHuntingDaysJoin() {
        return " FROM organisation clubgroup\n" +
                " JOIN harvest_permit_species_amount harvest_permit_species_amount ON (\n" +
                "   clubgroup.harvest_permit_id = harvest_permit_species_amount.harvest_permit_id\n" +
                "   AND clubgroup.game_species_id = harvest_permit_species_amount.game_species_id\n" +
                "   AND harvest_permit_species_amount.harvest_permit_id = :permitId)\n" +
                " JOIN game_species game_species ON (game_species.game_species_id = harvest_permit_species_amount.game_species_id AND game_species.official_code = :speciesCode)\n" +
                " JOIN harvest_permit_partners partner ON (partner.harvest_permit_id = :permitId AND partner.organisation_id = clubgroup.parent_organisation_id)\n" +
                " LEFT JOIN group_hunting_day group_hunting_day ON (group_hunting_day.hunting_group_id = clubgroup.organisation_id)";
    }

    private static String getHarvestOrObservationStats(String table, String statsValueAlias, String statsAlias) {
        return "LEFT JOIN (SELECT\n" +
                " days.club_id AS club_id,\n" +
                " max(item.modification_time) AS latest_" + statsValueAlias + "_update,\n" +
                " coalesce(sum(item.amount), 0) AS " + statsValueAlias + "_count\n" +
                " FROM " + table + " item\n" +
                " JOIN (SELECT\n" +
                "  clubgroup.parent_organisation_id AS club_id,\n" +
                "  group_hunting_day.modification_time,\n" +
                "  group_hunting_day.group_hunting_day_id,\n" +
                "  group_hunting_day.number_of_hunters\n" +
                getPermitsClubsHuntingDaysJoin() +
                ") AS days ON (item.group_hunting_day_id = days.group_hunting_day_id)\n" +
                "  JOIN game_species game_species ON (game_species.game_species_id = item.game_species_id AND game_species.official_code = :speciesCode)\n" +
                "GROUP BY days.club_id) AS " + statsAlias + " ON (daystats.club_id = " + statsAlias + ".club_id)";
    }

    private static HuntingClubPermitHuntingDayStatisticsDTO calculateTotalStats(
            Collection<HuntingClubPermitHuntingDayStatisticsDTO> stats) {

        HuntingClubPermitHuntingDayStatisticsDTO dto = new HuntingClubPermitHuntingDayStatisticsDTO();

        dto.setLatestUpdate(stats.stream()
                .map(HuntingClubPermitHuntingDayStatisticsDTO::getLatestUpdate)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null));

        dto.setDayCount(sum(stats, HuntingClubPermitHuntingDayStatisticsDTO::getDayCount));
        dto.setHunterCount(sum(stats, HuntingClubPermitHuntingDayStatisticsDTO::getHunterCount));
        dto.setHarvestCount(sum(stats, HuntingClubPermitHuntingDayStatisticsDTO::getHarvestCount));
        dto.setObservationCount(sum(stats, HuntingClubPermitHuntingDayStatisticsDTO::getObservationCount));
        return dto;
    }

    private static Map<Long, HuntingClubPermitPaymentDTO> calculatePayments(
            final MooselikePrice prices, final Map<Long, HuntingClubPermitCountDTO> harvests) {

        return harvests.values().stream()
                .map(h -> HuntingClubPermitPaymentDTO.create(h, prices))
                .collect(indexingBy(HuntingClubPermitPaymentDTO::getHuntingClubId));
    }

    private static HuntingClubPermitTotalPaymentDTO calculateTotalPayment(
            Map<Long, HuntingClubPermitPaymentDTO> payments,
            MooselikePrice prices,
            LocalDate dueDate,
            CreditorReference creditorReference,
            Map<Long, HuntingClubPermitCountDTO> harvests) {

        final HuntingClubPermitTotalPaymentDTO total = new HuntingClubPermitTotalPaymentDTO();
        total.setYoungPayment(sum(payments.values(), HuntingClubPermitPaymentDTO::getYoungPayment));
        total.setAdultsPayment(sum(payments.values(), HuntingClubPermitPaymentDTO::getAdultsPayment));
        total.setCreditorReference(creditorReference != null ? creditorReference.getValue() : null);
        total.setIban(prices.getIban() != null ? prices.getIban().toFormattedString() : null);
        total.setBic(prices.getBic() != null ? prices.getBic().toString() : null);
        total.setRecipientName(prices.getRecipientName());

        total.setAdultPrice(prices.getAdultPrice());
        total.setYoungPrice(prices.getYoungPrice());
        total.setDueDate(dueDate);

        total.setAdultsCount(sum(harvests.values(), HuntingClubPermitCountDTO::countAdults));
        total.setAdultsNotEdibleCount(sum(harvests.values(), HuntingClubPermitCountDTO::getNumberOfNonEdibleAdults));
        total.setYoungCount(sum(harvests.values(), HuntingClubPermitCountDTO::countYoung));
        total.setYoungNotEdibleCount(sum(harvests.values(), HuntingClubPermitCountDTO::getNumberOfNonEdibleYoungs));
        return total;
    }
}
