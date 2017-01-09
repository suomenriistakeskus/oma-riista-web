package fi.riista.feature.huntingclub.permit;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.common.entity.CreditorReference;
import fi.riista.feature.gamediary.GameDiaryService;
import fi.riista.feature.gamediary.GameSpeciesDTO;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermitSpecs;
import fi.riista.feature.harvestpermit.HarvestPermitAuthorization;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount_;
import fi.riista.feature.harvestpermit.HarvestPermit_;
import fi.riista.feature.harvestpermit.season.MooselikePrice;
import fi.riista.feature.harvestpermit.allocation.HarvestPermitAllocationRepository;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.feature.harvestpermit.season.MooselikePriceRepository;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.permit.basicsummary.BasicClubHuntingSummaryRepository;
import fi.riista.feature.huntingclub.permit.harvestreport.MooseHarvestReport;
import fi.riista.feature.huntingclub.permit.harvestreport.MooseHarvestReportDTO;
import fi.riista.feature.huntingclub.permit.harvestreport.MooseHarvestReportRepository;
import fi.riista.feature.huntingclub.permit.summary.ClubHuntingSummaryBasicInfoDTO;
import fi.riista.feature.huntingclub.permit.summary.HuntingEndStatus;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.EntityPermission;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import fi.riista.util.NumberUtils;
import fi.riista.util.jpa.JpaSubQuery;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Stream;

import static fi.riista.feature.harvestpermit.HarvestPermitAuthorization.HarvestPermitPermission.CREATE_REMOVE_MOOSE_HARVEST_REPORT;
import static fi.riista.feature.harvestpermit.HarvestPermitAuthorization.HarvestPermitPermission.LIST_LEADERS;
import static fi.riista.feature.harvestpermit.HarvestPermitAuthorization.HarvestPermitPermission.UPDATE_ALLOCATIONS;
import static fi.riista.util.jpa.JpaSpecs.equal;
import static fi.riista.util.jpa.JpaSpecs.fetch;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.springframework.data.jpa.domain.Specifications.where;

@Component
public class HuntingClubPermitFeature {

    private static final Logger LOG = LoggerFactory.getLogger(HuntingClubPermitFeature.class);

    @Resource
    private HarvestPermitRepository permitRepository;

    @Resource
    private HarvestPermitSpeciesAmountRepository speciesAmountRepository;

    @Resource
    private HarvestPermitAllocationRepository allocationRepository;

    @Resource
    private BasicClubHuntingSummaryRepository basicHuntingSummaryRepository;

    @Resource
    private MooseHarvestReportRepository mooseHarvestReportRepository;

    @Resource
    private HarvestPermitSpeciesAmountRepository speciesAmountRepo;

    @Resource
    private MooselikePriceRepository mooselikePriceRepository;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private RequireEntityService entityService;

    @Resource
    private GameDiaryService gameDiaryService;

    @Resource
    private HuntingClubPermitService huntingPermitService;

    @Resource
    private HarvestPermitAuthorization harvestPermitAuthorization;

    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(final DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Transactional(readOnly = true)
    public List<MooselikePermitListingDTO> listPermits(final long huntingClubId, final int huntingYear, final int speciesCode) {
        final HuntingClub club = entityService.requireHuntingClub(huntingClubId, EntityPermission.READ);

        return permitRepository.findAll(spec(club, huntingYear, speciesCode))
                .stream()
                .map(p -> HuntingClubPermitFeature.this.getPermitListingDTOWithoutAuthorization(p, speciesCode, huntingClubId))
                .collect(toList());
    }

    private static Specifications<HarvestPermit> spec(final HuntingClub club, final int huntingYear, int speciesCode) {
        return clubPredicate(club)
                .and(HarvestPermitSpecs.validWithinHuntingYear(huntingYear))
                .and(HarvestPermitSpecs.IS_MOOSELIKE_PERMIT)
                .and(HarvestPermitSpecs.withSpeciesCode(speciesCode));
    }

    private static Specifications<HarvestPermit> clubPredicate(final HuntingClub club) {
        final Specification<HarvestPermit> clubIsPermitHolder = equal(HarvestPermit_.permitHolder, club);
        final Specification<HarvestPermit> clubIsPermitPartner =
                JpaSubQuery.of(HarvestPermit_.permitPartners).exists((root, cb) -> cb.equal(root, club));

        return Specifications.where(clubIsPermitHolder).or(clubIsPermitPartner);
    }

    @Transactional(readOnly = true)
    public List<GameSpeciesDTO> listPermitSpecies(long huntingClubId) {
        final HuntingClub club = entityService.requireHuntingClub(huntingClubId, EntityPermission.READ);

        return GameSpeciesDTO.transformList(permitRepository.findAll(clubPredicate(club)).stream()
                .flatMap(p -> p.getSpeciesAmounts().stream())
                .map(HarvestPermitSpeciesAmount::getGameSpecies)
                .distinct()
                .collect(toList()));
    }

    @Transactional(readOnly = true)
    public List<MooselikeHuntingYearDTO> listHuntingYears(final long huntingClubId) {
        final HuntingClub club = entityService.requireHuntingClub(huntingClubId, EntityPermission.READ);

        return MooselikeHuntingYearDTO.create(permitRepository.findAll(clubPredicate(club)).stream()
                .map(HarvestPermit::getSpeciesAmounts)
                .flatMap(Collection::stream));
    }

    @Transactional(readOnly = true)
    public HuntingClubPermitDTO getPermit(final long huntingClubId, final long harvestPermitId, final int speciesCode) {
        final HuntingClub club = entityService.requireHuntingClub(huntingClubId, EntityPermission.READ);
        final HarvestPermit permit = entityService.requireHarvestPermit(harvestPermitId, EntityPermission.READ);

        Preconditions.checkArgument(permit.isPermitHolderOrPartner(club), "Club is not permits holder or partner");

        final HuntingClubPermitDTO dto = getPermitWithoutAuthorization(permit, speciesCode, huntingClubId);
        final HarvestPermitSpeciesAmount hpsa =
                speciesAmountRepo.getOneByHarvestPermitIdAndSpeciesCode(harvestPermitId, speciesCode);

        dto.setMooseHarvestReport(MooseHarvestReportDTO.create(mooseHarvestReportRepository.findBySpeciesAmount(hpsa)));
        return dto;
    }

    @Transactional(readOnly = true)
    public MooselikePermitListingDTO getPermitListingDTOWithoutAuthorization
            (final HarvestPermit permit, final int speciesCode, final Long viewedClubId) {

        final SystemUser activeUser = activeUserService.getActiveUser();
        final Person person = activeUser.getPerson();

        final boolean canEditAllocations = harvestPermitAuthorization.hasPermission(permit, person, UPDATE_ALLOCATIONS);

        final GameSpecies species = gameDiaryService.getGameSpeciesByOfficialCode(speciesCode);
        final HarvestPermitSpeciesAmount speciesAmount = getOneSpeciesAmount(
                speciesAmountRepository.findByHarvestPermit(permit), permit.getPermitNumber(), species);

        final Map<String, Float> amendmentPermits = getAmendmentPermits(permit, species);

        final int huntingYear = speciesAmount.resolveHuntingYear();
        final MooselikePrice prices = mooselikePriceRepository.getByHuntingYearAndGameSpecies(huntingYear, species);

        final Map<Long, HuntingClubPermitCountDTO> harvestCounts =
                huntingPermitService.getHarvestCountsGroupedByClubId(permit, speciesCode);

        final Map<Long, HuntingClubPermitPaymentDTO> payments = calculatePayments(prices, harvestCounts);
        final HuntingClubPermitTotalPaymentDTO totalPayment = calculateTotalPayment(payments, prices,
                speciesAmount.getDueDate(), speciesAmount.getCreditorReference(), harvestCounts);

        final boolean amendmentPermitsMatchHarvests = checkAmendmentPermitsMatchHarvests(amendmentPermits, harvestCounts);

        final boolean hasPermissionToCreateOrRemove =
                harvestPermitAuthorization.hasPermission(permit, person, CREATE_REMOVE_MOOSE_HARVEST_REPORT);

        final MooseHarvestReport mooseHarvestReport = mooseHarvestReportRepository.findBySpeciesAmount(speciesAmount);

        final Map<Long, ClubHuntingSummaryBasicInfoDTO> summariesByClubId =
                huntingPermitService.getHuntingSummaryBasicInfoGroupedByClubId(permit, speciesCode);

        final boolean allPartnersFinishedHunting = didAllPartnersFinishedHunting(summariesByClubId);


        final boolean listLeadersButtonVisible = harvestPermitAuthorization.hasPermission(permit, person, LIST_LEADERS);

        final boolean huntingFinished = Optional.ofNullable(viewedClubId)
                .map(summariesByClubId::get)
                .map(ClubHuntingSummaryBasicInfoDTO::isHuntingFinished)
                .orElse(false);

        final boolean huntingFinishedByModeration = Optional.ofNullable(viewedClubId)
                .map(summariesByClubId::get)
                .map(ClubHuntingSummaryBasicInfoDTO::isHuntingFinishedByModeration)
                .orElse(false);

        return new MooselikePermitListingDTOBuilder()
                .setPermit(permit)
                .setSpa(speciesAmount)
                .setSpecies(species)
                .setAmendmentPermits(amendmentPermits)
                .setViewedClubId(viewedClubId)
                .setCanEditAllocations(canEditAllocations)
                .setHasPermissionToCreateOrRemove(hasPermissionToCreateOrRemove && isNotLocked(huntingYear))
                .setAmendmentPermitsMatchHarvests(amendmentPermitsMatchHarvests)
                .setAllPartnersFinishedHunting(allPartnersFinishedHunting)
                .setMooseHarvestReport(mooseHarvestReport)
                .setListLeadersButtonVisible(listLeadersButtonVisible)
                .setHuntingFinished(huntingFinished)
                .setHuntingFinishedByModeration(huntingFinishedByModeration)
                .setHarvests(harvestCounts.values())
                .setAllocations(allocationRepository.getAllocationsIncludeMissingPartnerDTO(permit, species))
                .setTotalPayment(totalPayment)
                .createMooselikePermitListingDTO();
    }

    private static boolean viewedClubInPartners(Long viewedClubId, HarvestPermit permit) {
        return F.getUniqueIds(permit.getPermitPartners()).contains(viewedClubId);
    }

    private static boolean didAllPartnersFinishedHunting(Map<Long, ? extends HuntingEndStatus> endStatusesByClubId) {
        return endStatusesByClubId.values().stream().allMatch(HuntingEndStatus::isHuntingFinished);
    }

    @Transactional(readOnly = true)
    public HuntingClubPermitDTO getPermitWithoutAuthorization(final HarvestPermit permit,
                                                              final int speciesCode,
                                                              final Long viewedClubId) {
        final SystemUser activeUser = activeUserService.getActiveUser();
        final Person person = activeUser.getPerson();
        final boolean canEditAllocations = harvestPermitAuthorization.hasPermission(permit, person, UPDATE_ALLOCATIONS);

        final GameSpecies species = gameDiaryService.getGameSpeciesByOfficialCode(speciesCode);
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

        final boolean amendmentPermitsMatchHarvests =
                checkAmendmentPermitsMatchHarvests(amendmentPermits, harvestCounts);

        final MooseHarvestReport mooseHarvestReport = mooseHarvestReportRepository.findBySpeciesAmount(speciesAmount);
        final MooseHarvestReportDTO mooseHarvestReportDTO = MooseHarvestReportDTO.create(mooseHarvestReport);

        return HuntingClubPermitDTO.create(
                permit,
                species,
                speciesAmount,
                amendmentPermits,
                viewedClubId,
                allocationRepository.getAllocationsIncludeMissingPartnerDTO(permit, species),
                canEditAllocations,
                harvestCounts,
                totalStats,
                stats,
                huntingPermitService.getHuntingSummaryBasicInfoGroupedByClubId(permit, speciesCode),
                totalPayment,
                payments,
                amendmentPermitsMatchHarvests,
                mooseHarvestReportDTO);
    }

    private static boolean isNotLocked(final int huntingYear) {
        final LocalDate lockDate = new LocalDate(huntingYear + 1, 3, 31);
        return !DateUtil.today().isAfter(lockDate);
    }

    private static boolean checkAmendmentPermitsMatchHarvests(final Map<String, Float> amendmentPermits,
                                                              final Map<Long, HuntingClubPermitCountDTO> harvests) {
        double totalAmendmentPermits = amendmentPermits.values().stream()
                .mapToDouble(Float::doubleValue)
                .sum();

        double usedPermits = harvests.values().stream()
                .mapToDouble(c -> c.getNumberOfNonEdibleAdults() + c.getNumberOfNonEdibleYoungs() * 0.5)
                .sum();
        return NumberUtils.equal(totalAmendmentPermits, usedPermits);
    }

    private static HarvestPermitSpeciesAmount getOneSpeciesAmount(
            final Collection<HarvestPermitSpeciesAmount> allSpeciesAmountsForPermit,
            final String permitNumber,
            final GameSpecies species) {

        final List<HarvestPermitSpeciesAmount> filteredSpeciesAmounts = allSpeciesAmountsForPermit.stream()
                .filter(speciesAmount -> Objects.equals(speciesAmount.getGameSpecies().getId(), species.getId()))
                .sorted((o1, o2) -> o1.getBeginDate().compareTo(o2.getBeginDate()))
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

        dto.setDayCount(F.sum(stats, HuntingClubPermitHuntingDayStatisticsDTO::getDayCount));
        dto.setHunterCount(F.sum(stats, HuntingClubPermitHuntingDayStatisticsDTO::getHunterCount));
        dto.setHarvestCount(F.sum(stats, HuntingClubPermitHuntingDayStatisticsDTO::getHarvestCount));
        dto.setObservationCount(F.sum(stats, HuntingClubPermitHuntingDayStatisticsDTO::getObservationCount));
        return dto;
    }

    private static Map<Long, HuntingClubPermitPaymentDTO> calculatePayments(
            final MooselikePrice prices, final Map<Long, HuntingClubPermitCountDTO> harvests) {

        return harvests.values().stream()
                .map(h -> HuntingClubPermitPaymentDTO.create(h, prices))
                .collect(toMap(HuntingClubPermitPaymentDTO::getHuntingClubId, identity()));
    }

    private static HuntingClubPermitTotalPaymentDTO calculateTotalPayment(
            Map<Long, HuntingClubPermitPaymentDTO> payments,
            MooselikePrice prices,
            LocalDate dueDate,
            CreditorReference creditorReference,
            Map<Long, HuntingClubPermitCountDTO> harvests) {

        final HuntingClubPermitTotalPaymentDTO total = new HuntingClubPermitTotalPaymentDTO();
        total.setYoungPayment(F.sum(payments.values(), HuntingClubPermitPaymentDTO::getYoungPayment));
        total.setAdultsPayment(F.sum(payments.values(), HuntingClubPermitPaymentDTO::getAdultsPayment));
        total.setCreditorReference(creditorReference.getValue());
        total.setIban(prices.getIban().getValue());
        total.setBic(prices.getBic().getValue());
        total.setRecipientName(prices.getRecipientName());

        total.setAdultPrice(prices.getAdultPrice());
        total.setYoungPrice(prices.getYoungPrice());
        total.setDueDate(dueDate);

        total.setAdultsCount(F.sum(harvests.values(), HuntingClubPermitCountDTO::countAdults));
        total.setAdultsNotEdibleCount(F.sum(harvests.values(), HuntingClubPermitCountDTO::getNumberOfNonEdibleAdults));
        total.setYoungCount(F.sum(harvests.values(), HuntingClubPermitCountDTO::countYoung));
        total.setYoungNotEdibleCount(F.sum(harvests.values(), HuntingClubPermitCountDTO::getNumberOfNonEdibleYoungs));
        return total;
    }
}
