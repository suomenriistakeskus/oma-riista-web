package fi.riista.feature.metrics;

import com.google.common.base.Joiner;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.account.user.QSystemUser;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.GameDiaryEntry;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.QGameDiaryEntry;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.gamediary.harvest.QHarvest;
import fi.riista.feature.gamediary.observation.QObservation;
import fi.riista.feature.gamediary.srva.SrvaEventStateEnum;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.occupation.QOccupation;
import fi.riista.feature.organization.person.QPerson;
import fi.riista.util.DateUtil;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Types;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Component
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminMetricsFeature {

    private JdbcTemplate jdbcTemplate;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Resource
    private JPQLQueryFactory queryFactory;

    @Autowired
    public void setDataSource(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(this.jdbcTemplate);
    }

    @Transactional(readOnly = true)
    public AdminMetricsDTO getBasicMetrics() {
        final AdminMetricsDTO dto = new AdminMetricsDTO();

        countUsers(dto);
        countModerators(dto);
        countCoordinators(dto);
        countHarvests(dto);
        countObservations(dto);
        countHarvestReports(dto);
        countPdfDownloads(dto);
        countSrvaEvents(dto);
        computeMooselikeHuntingMetrics(dto);

        return dto;
    }

    private void countUsers(AdminMetricsDTO dto) {
        final QSystemUser systemUser = QSystemUser.systemUser;
        final QPerson person = QPerson.person;
        final QOccupation occupation = QOccupation.occupation;

        final BooleanExpression roleUserHavingPassword = systemUser.role.eq(SystemUser.Role.ROLE_USER)
                .and(systemUser.password.isNotNull());

        final BooleanExpression occupationValidAndNotForClub = occupation.validAndNotDeleted()
                .and(occupation.occupationType.notIn(OccupationType.clubValues()));

        // Rekisteröityneet henkilöt (ei välttämättä roolia)
        dto.setCountNormalUser(queryFactory.select(systemUser.person)
                .from(systemUser)
                .where(roleUserHavingPassword.and(systemUser.person.isNotNull()))
                .distinct()
                .fetchCount());

        // Kaikki henkilöt
        dto.setCountPerson(queryFactory.from(person).fetchCount());

        // Rekisteröityneet henkilöt joilla tehtävä jossakin rk-organisaatiossa
        final BooleanExpression occupationExistsBySystemUser = JPAExpressions.selectFrom(occupation)
                .where(occupation.person.eq(systemUser.person).and(occupationValidAndNotForClub))
                .exists();
        dto.setCountNormalUserWithOccupationAndPassword(queryFactory.select(systemUser.person)
                .from(systemUser)
                .where(occupationExistsBySystemUser.and(roleUserHavingPassword))
                .distinct()
                .fetchCount());

        // Kaikki henkilöt joilla tehtävä jossakin rk-organisaatiossa
        final BooleanExpression occupationExistsByPerson = JPAExpressions.selectFrom(occupation)
                .where(occupation.person.eq(person).and(occupationValidAndNotForClub))
                .exists();
        dto.setCountAllPeopleWithOccupation(queryFactory.from(person)
                .where(occupationExistsByPerson)
                .distinct()
                .fetchCount());
    }

    private void countModerators(AdminMetricsDTO dto) {
        // Moderaattorit jotka ovat rekisteröityneet
        dto.setCountModeratorWithPassword(jdbcTemplate.queryForObject(
                "SELECT COUNT(password) FROM system_user WHERE role = 'ROLE_MODERATOR'"
                , Long.class));

        // Kaikki moderaattorit
        dto.setCountAllModerators(jdbcTemplate.queryForObject(
                "SELECT COUNT(user_id) FROM system_user WHERE role = 'ROLE_MODERATOR'"
                , Long.class));
    }

    private void countCoordinators(AdminMetricsDTO dto) {
        // Rekisteröityneet toiminnanohjaajat
        dto.setCountRHYToiminnanohjaajaWithPassword(jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT person_id) FROM system_user" +
                        " WHERE role = 'ROLE_USER' AND password IS NOT NULL" +
                        " AND EXISTS (SELECT occupation_id FROM occupation o" +
                        " WHERE o.person_id = system_user.person_id" +
                        " AND now() BETWEEN COALESCE(begin_date, now()) AND COALESCE(end_date, now())" +
                        " AND occupation_type = 'TOIMINNANOHJAAJA')", Long.class));

        // Kaikki toiminnanohjaajat
        dto.setCountAllRHYToiminnanohjaaja(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM person" +
                        " WHERE EXISTS (SELECT occupation_id FROM occupation o" +
                        " WHERE o.person_id = person.person_id" +
                        " AND now() BETWEEN COALESCE(begin_date, now()) AND COALESCE(end_date, now())" +
                        " AND occupation_type = 'TOIMINNANOHJAAJA')", Long.class));
    }

    private void countHarvests(AdminMetricsDTO dto) {
        // Saaliskirjauksia yhteensä
        dto.setCountHarvest(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM harvest WHERE deletion_time IS NULL", Long.class));

        // Saalisyksilöitä yhteensä
        dto.setCountHarvestSpecimens(jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(amount), 0) FROM harvest WHERE deletion_time IS NULL", Long.class));

        // Saaliskirjauksia mobiililla
        dto.setCountHarvestFromMobile(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM harvest" +
                        " WHERE deletion_time IS NULL" +
                        " AND geolocation_source = 'GPS_DEVICE'" +
                        " OR from_mobile = true AND geolocation_source = 'MANUAL'", Long.class));

        // Saaliskirjauksia webissä
        dto.setCountHarvestFromWeb(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM harvest" +
                        " WHERE deletion_time IS NULL" +
                        " AND geolocation_source = 'MANUAL'" +
                        " AND (from_mobile = false OR from_mobile IS null)", Long.class));
    }

    private void countObservations(AdminMetricsDTO dto) {
        // Havaintoja yhteensä
        dto.setCountObservation(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM game_observation WHERE deletion_time IS NULL", Long.class));

        // Havaintoyksilöitä yhteensä
        dto.setCountObservationSpecimens(jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(amount), 0) FROM game_observation WHERE deletion_time IS NULL", Long.class));

        // Havaintoja mobiililla
        dto.setCountObservationFromMobile(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM game_observation" +
                        " WHERE deletion_time IS NULL" +
                        " AND from_mobile = true", Long.class));

        // Havaintoja webissä
        dto.setCountObservationFromWeb(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM game_observation" +
                        " WHERE deletion_time IS NULL" +
                        " AND from_mobile = false", Long.class));
    }

    private void countHarvestReports(AdminMetricsDTO dto) {
        // Saalisilmoituksia yhteensä
        dto.setCountHarvestReport(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM harvest_report WHERE deletion_time IS NULL;", Long.class));

        // Saalisilmoituksia yhteensä hyväksytty
        dto.setCountHarvestReportApproved(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM harvest_report WHERE deletion_time IS NULL AND state = 'APPROVED';", Long.class));
    }

    private void countPdfDownloads(AdminMetricsDTO dto) {
        // Metsästyskortti
        // Metsästyskortin ladanneiden käyttäjien määrä
        dto.setCountPdfCertificateUsers(jdbcTemplate.queryForObject(
                "SELECT count(DISTINCT user_id) FROM account_activity_message WHERE activity_type = 'PDF_HUNTER_CARD';", Long.class));
        // Metsästyskortin latauksia
        dto.setCountPdfCertificateDownloads(jdbcTemplate.queryForObject(
                "SELECT count(activity_type) FROM account_activity_message WHERE activity_type = 'PDF_HUNTER_CARD';", Long.class));

        // Ulkomaan todistus
        // Ulkomaan todistuksen ladanneiden käyttäjien määrä
        dto.setCountForeignPdfCertificateUsers(jdbcTemplate.queryForObject(
                "SELECT count(DISTINCT user_id) FROM account_activity_message WHERE activity_type = 'PDF_FOREIGN_CERTIFICATE';", Long.class));
        // Ulkomaan todistuksen latauksia
        dto.setCountForeignPdfCertificateDownloads(jdbcTemplate.queryForObject(
                "SELECT count(activity_type) FROM account_activity_message WHERE activity_type = 'PDF_FOREIGN_CERTIFICATE';", Long.class));

        // Riistanhoitomaksun tilisiirtolomake
        // Riistanhoitomaksun tililomakkeen ladanneiden käyttäjien määrä
        dto.setCountHunterPaymentPdfUsers(jdbcTemplate.queryForObject(
                "SELECT count(DISTINCT user_id) FROM account_activity_message WHERE activity_type = 'PDF_HUNTER_PAYMENT';", Long.class));
        // Riistanhoitomaksun tililomakkeen latauksia
        dto.setCountHunterPaymentPdfDownloads(jdbcTemplate.queryForObject(
                "SELECT count(activity_type) FROM account_activity_message WHERE activity_type = 'PDF_HUNTER_PAYMENT';", Long.class));
    }

    private void countSrvaEvents(AdminMetricsDTO dto) {
        // Srva-kirjauksia yhteensä
        dto.setCountSrvaEvent(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM srva_event", Long.class));

        // Srva yksilöitä yhteensä
        dto.setCountSrvaEventSpecimen(jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(total_specimen_amount),0) FROM srva_event", Long.class));

        // Srva yksilöitä hyväksytty
        dto.setCountSrvaEventSpecimenApproved(jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(total_specimen_amount),0) FROM srva_event WHERE state = '"
                        + SrvaEventStateEnum.APPROVED + "'", Long.class));

        // Srva yksilöitä kesken
        dto.setCountSrvaEventSpecimenUnfinished(jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(total_specimen_amount),0) FROM srva_event WHERE state = '"
                        + SrvaEventStateEnum.UNFINISHED + "'", Long.class));

        // Srva yksilöitä hylätty
        dto.setCountSrvaEventSpecimenRejected(jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(total_specimen_amount),0) FROM srva_event WHERE state = '"
                        + SrvaEventStateEnum.REJECTED + "'", Long.class));

        // Srva-kirjauksia mobiililla
        dto.setCountSrvaEventFromMobile(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM srva_event WHERE from_mobile = true", Long.class));

        // Srva-kirjauksia webissä
        dto.setCountSrvaEventFromWeb(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM srva_event WHERE from_mobile = false", Long.class));

        // Srva-kirjauksia hyväksytty
        dto.setCountSrvaEventApproved(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM srva_event WHERE state = '"
                        + SrvaEventStateEnum.APPROVED + "'", Long.class));

        // Srva-kirjauksia kesken
        dto.setCountSrvaEventUnfinished(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM srva_event WHERE state = '"
                        + SrvaEventStateEnum.UNFINISHED + "'", Long.class));

        // Srva-kirjauksia hylätty
        dto.setCountSrvaEventRejected(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM srva_event WHERE state = '"
                        + SrvaEventStateEnum.REJECTED + "'", Long.class));
    }

    private void computeMooselikeHuntingMetrics(AdminMetricsDTO dto) {
        final Interval yearInterval = DateUtil.huntingYearInterval(DateUtil.getFirstCalendarYearOfCurrentHuntingYear());

        final QHarvest harvest = QHarvest.harvest;
        final Map<Long, Integer> harvestCounts = count(harvest, harvest._super, harvest.amount.sum(), yearInterval);

        final QObservation observation = QObservation.observation;
        final Map<Long, Integer> observationCounts =
                count(observation, observation._super, observation.amount.sum(), yearInterval);

        final QGameSpecies species = QGameSpecies.gameSpecies;

        final Map<Long, AdminMooselikeHuntingMetrics> speciesIdToMetrics = new HashMap<>();

        queryFactory.select(species)
                .from(species)
                .where(species.officialCode.in(GameSpecies.MOOSE_AND_DEER_CODES_REQUIRING_PERMIT_FOR_HUNTING))
                .fetch()
                .forEach(gameSpecies -> {
                    final AdminMooselikeHuntingMetrics metrics =
                            new AdminMooselikeHuntingMetrics(gameSpecies.getNameFinnish());

                    final long speciesId = gameSpecies.getId();
                    metrics.setHarvestCount(harvestCounts.getOrDefault(speciesId, 0));
                    metrics.setObservationCount(observationCounts.getOrDefault(speciesId, 0));
                    speciesIdToMetrics.put(speciesId, metrics);
                });

        countPermitsAndMooseDataCardGroups(yearInterval, speciesIdToMetrics);

        dto.setMooselikeHuntingMetrics(speciesIdToMetrics
                .values()
                .stream()
                .sorted(comparing(AdminMooselikeHuntingMetrics::getSpeciesName))
                .collect(toList()));
    }

    private Map<Long, Integer> count(final EntityPathBase<? extends GameDiaryEntry> entity,
                                     final QGameDiaryEntry gameDiaryEntry,
                                     final NumberExpression<Integer> sumExpression,
                                     final Interval interval) {

        final Date begin = interval.getStart().toDate();
        final Date end = interval.getEnd().toDate();

        final QGameSpecies species = QGameSpecies.gameSpecies;
        final JPQLQuery<GameSpecies> mooselikeSpecies = JPAExpressions.selectFrom(species)
                .where(species.officialCode.in(GameSpecies.MOOSE_AND_DEER_CODES_REQUIRING_PERMIT_FOR_HUNTING));

        return queryFactory.select(gameDiaryEntry.species.id, sumExpression)
                .from(entity)
                .where(gameDiaryEntry.huntingDayOfGroup.isNotNull(),
                        gameDiaryEntry.species.in(mooselikeSpecies),
                        gameDiaryEntry.pointOfTime.between(begin, end))
                .groupBy(gameDiaryEntry.species.id)
                .fetch()
                .stream()
                .collect(toMap(t -> t.get(gameDiaryEntry.species.id), t -> t.get(sumExpression)));
    }

    private void countPermitsAndMooseDataCardGroups(final Interval interval,
                                                    final Map<Long, AdminMooselikeHuntingMetrics> speciesIdToMetrics) {

        final String mooselikeIds = Joiner.on(", ").join(GameSpecies.MOOSE_AND_DEER_CODES_REQUIRING_PERMIT_FOR_HUNTING);

        final String queryStr = "SELECT \n" +
                "  gs.game_species_id                      AS species_id, \n" +
                "  count(t.permitId) - sum(t.closedReport) AS open_permit_count, \n" +
                "  sum(t.closedReport)                     AS closed_permit_count, \n" +
                "  sum(t.moderatedReport)                  AS moderator_closed_permit_count, \n" +
                "  sum(u.mooseDataCardGroupCount)          AS moose_data_card_group_count \n" +
                "FROM game_species gs \n" +
                "INNER JOIN ( \n" +
                "  SELECT \n" +
                "    hp.harvest_permit_id                                                AS permitId, \n" +
                "    hpsa.game_species_id                                                AS speciesId, \n" +
                "    CASE WHEN mhr.moose_harvest_report_id IS NOT NULL THEN 1 ELSE 0 END AS closedReport, \n" +
                "    CASE WHEN mhr.moderator_override = true THEN 1 ELSE 0 END AS moderatedReport \n" +
                "  FROM harvest_permit_species_amount hpsa \n" +
                "  INNER JOIN harvest_permit hp ON hp.harvest_permit_id = hpsa.harvest_permit_id \n" +
                "  LEFT OUTER JOIN moose_harvest_report mhr ON mhr.species_amount_id = hpsa.harvest_permit_species_amount_id \n" +
                "  WHERE \n" +
                "    hp.permit_type_code = '" + HarvestPermit.MOOSELIKE_PERMIT_TYPE + "' \n" +
                "    AND ( \n" +
                "      hpsa.begin_date >= :beginDate AND hpsa.end_date <= :endDate \n" +
                "      OR hpsa.begin_date2 IS NOT NULL AND hpsa.begin_date2 >= :beginDate \n" +
                "        AND hpsa.end_date2 IS NOT NULL AND hpsa.end_date2 <= :endDate \n" +
                "    ) \n" +
                ") t ON t.speciesId = gs.game_species_id \n" +
                "LEFT OUTER JOIN ( \n" +
                "  SELECT \n" +
                "    harvest_permit_id      AS permitId, \n" +
                "    game_species_id        AS speciesId, \n" +
                "    count(organisation_id) AS mooseDataCardGroupCount \n" +
                "  FROM organisation \n" +
                "  WHERE \n" +
                "    organisation_type = '" + OrganisationType.CLUBGROUP.name() + "' \n" +
                "    AND from_moose_data_card = true \n" +
                "    AND harvest_permit_id IS NOT NULL \n" +
                "  GROUP BY harvest_permit_id, game_species_id \n" +
                ") u ON u.speciesId = gs.game_species_id AND u.permitId = t.permitId \n" +
                "WHERE gs.official_code IN (" + mooselikeIds + ") \n" +
                "GROUP BY gs.game_species_id \n" +
                "ORDER BY gs.game_species_id \n";

        final MapSqlParameterSource queryParams = new MapSqlParameterSource();
        queryParams.addValue("beginDate", interval.getStart().toDate(), Types.DATE);
        queryParams.addValue("endDate", interval.getEnd().toDate(), Types.DATE);

        namedParameterJdbcTemplate.query(queryStr, queryParams, (resultSet, i) -> {
            final AdminMooselikeHuntingMetrics metrics = speciesIdToMetrics.get(resultSet.getLong("species_id"));
            metrics.setOpenPermitCount(resultSet.getInt("open_permit_count"));
            metrics.setClosedPermitCount(resultSet.getInt("closed_permit_count"));
            metrics.setModeratorClosedPermitCount(resultSet.getInt("moderator_closed_permit_count"));

            final int mooseDataCardGroupCount = resultSet.getInt("moose_data_card_group_count");
            metrics.setMooseDataCardGroupCount(resultSet.wasNull() ? null : mooseDataCardGroupCount);

            return metrics;
        });
    }

    @Transactional(readOnly = true)
    public List<AdminHarvestReportMetricsDTO> getHarvestReportMetrics(Date begin, Date end) {
        final String sql = "-- kiintiömetsästys ja muu ei-luvanvarainen: rka päätellään saaliskirjauksen RHY:stä\n" +
                "(select rka.name_finnish as rka,\n" +
                "        g.name_finnish as species,\n" +
                "        cast(null as text ) as permit_type,\n" +
                "        cast(null as text ) as permit_type_code,\n" +
                "        (select true) as season,\n" +
                "        (select false) as permit,\n" +
                "        coalesce(sum(case when su.role = 'ROLE_USER' then 1 end), 0) as user_count,\n" +
                "        coalesce(sum(case when su.role <> 'ROLE_USER' then 1 end), 0) as moderator_count,\n" +
                "        count(hr.*) as reports_total\n" +
                " from harvest_report hr\n" +
                "   join harvest h on h.harvest_report_id=hr.harvest_report_id\n" +
                "   join organisation rhy on rhy.organisation_id = h.rhy_id\n" +
                "   join organisation rka on rka.organisation_id = rhy.parent_organisation_id\n" +
                "   join system_user su on su.user_id = hr.created_by_user_id\n" +
                "   join game_species g on g.game_species_id=h.game_species_id\n" +
                " where hr.state = 'APPROVED'\n" +
                "       and hr.creation_time between ? and ?\n" +
                "       and hr.harvest_permit_id is null\n" +
                " group by rka.name_finnish,g.name_finnish)\n" +
                "\n" +
                "union all\n" +
                "\n" +
                "-- poikkeuslupa: rka päätellään luvan RHY:stä\n" +
                "(select rka.name_finnish as rka,\n" +
                "        cast(null as text ) as species,\n" +
                "        hp.permit_type as permit_type,\n" +
                "        hp.permit_type_code as permit_type_code,\n" +
                "        (select false) as season,\n" +
                "        (select true) as permit,\n" +
                "        coalesce(sum(case when su.role = 'ROLE_USER' then 1 end), 0) as user_count,\n" +
                "        coalesce(sum(case when su.role <> 'ROLE_USER' then 1 end), 0) as moderator_count,\n" +
                "        count(hr.*) as reports_total\n" +
                " from harvest_report hr\n" +
                "   join harvest_permit hp on hp.harvest_permit_id=hr.harvest_permit_id\n" +
                "   join organisation rhy on rhy.organisation_id = hp.rhy_id\n" +
                "   join organisation rka on rka.organisation_id = rhy.parent_organisation_id\n" +
                "   join system_user su on su.user_id = hr.created_by_user_id\n" +
                " where hr.state = 'APPROVED'\n" +
                "       and hr.creation_time between ? and ?\n" +
                "       and hp.harvests_as_list=true\n" +
                " group by rka.name_finnish,hp.permit_type,hp.permit_type_code)\n" +
                "\n" +
                "union all\n" +
                "\n" +
                "--  ei-poikkeuslupa: rka päätellään saaliiskirjauksen RHY:stä\n" +
                "(select rka.name_finnish as rka,\n" +
                "        cast(null as text ) as species,\n" +
                "        hp.permit_type as permit_type,\n" +
                "        hp.permit_type_code as permit_type_code,\n" +
                "        (select false) as season,\n" +
                "        (select true) as permit,\n" +
                "        coalesce(sum(case when su.role = 'ROLE_USER' then 1 end), 0) as user_count,\n" +
                "        coalesce(sum(case when su.role <> 'ROLE_USER' then 1 end), 0) as moderator_count,\n" +
                "        count(hr.*) as reports_total\n" +
                " from harvest_report hr\n" +
                "   join harvest_permit hp on hp.harvest_permit_id=hr.harvest_permit_id\n" +
                "   join harvest h on h.harvest_report_id=hr.harvest_report_id\n" +
                "   join organisation rhy on rhy.organisation_id = h.rhy_id\n" +
                "   join organisation rka on rka.organisation_id = rhy.parent_organisation_id\n" +
                "   join system_user su on su.user_id = hr.created_by_user_id\n" +
                " where hr.state = 'APPROVED'\n" +
                "       and hr.creation_time between ? and ?\n" +
                "       and hp.harvests_as_list=false\n" +
                " group by rka.name_finnish,hp.permit_type,hp.permit_type_code)\n";

        final Object[] args = {begin, end, begin, end, begin, end};
        final List<AdminHarvestReportMetricsDTO> dtos = jdbcTemplate.query(sql, args, createRowMapper());
        Collections.sort(dtos, cmp());
        return dtos;
    }

    private static Comparator<? super AdminHarvestReportMetricsDTO> cmp() {
        return (o1, o2) -> ComparisonChain.start()
                .compare(o1.getRka(), o2.getRka())
                .compare(o1.getSpecies(), o2.getSpecies(), Ordering.natural().nullsFirst())
                .compare(o1.getPermitType(), o2.getPermitType())
                .result();
    }

    private static RowMapper<AdminHarvestReportMetricsDTO> createRowMapper() {
        return (rs, rowNum) -> {
            AdminHarvestReportMetricsDTO dto = new AdminHarvestReportMetricsDTO();
            dto.setRka(rs.getString("rka"));
            dto.setSpecies(rs.getString("species"));
            dto.setPermitType(rs.getString("permit_type"));
            dto.setPermitTypeCode(rs.getString("permit_type_code"));
            dto.setSeason(rs.getBoolean("season"));
            dto.setPermit(rs.getBoolean("permit"));
            dto.setUserCount(rs.getInt("user_count"));
            dto.setModeratorCount(rs.getInt("moderator_count"));
            dto.setReportsTotal(rs.getInt("reports_total"));
            return dto;
        };
    }

    @Transactional(readOnly = true)
    public List<AdminRhyEditMetricsDTO> getRhyEditMetrics(Date begin, Date end) {
        final String sql = "WITH tehtavat AS (\n" +
                "  SELECT\n" +
                "    rhy.organisation_id,\n" +
                "    sum(CASE WHEN creator.role = 'ROLE_USER' THEN 1 ELSE 0 END) AS created_to,\n" +
                "    sum(CASE WHEN creator.role = 'ROLE_MODERATOR' THEN 1 ELSE 0 END) AS created_mo,\n" +
                "    sum(CASE WHEN item.creation_time<>item.modification_time AND modifier.role = 'ROLE_USER' THEN 1 ELSE 0 END) AS modified_to,\n" +
                "    sum(CASE WHEN item.creation_time<>item.modification_time AND modifier.role = 'ROLE_MODERATOR' THEN 1 ELSE 0 END) AS modified_mo\n" +
                "  FROM organisation rhy\n" +
                "    JOIN occupation item ON (item.organisation_id = rhy.organisation_id)\n" +
                "    LEFT JOIN system_user modifier ON (modifier.user_id = item.modified_by_user_id)\n" +
                "    LEFT JOIN system_user creator ON (creator.user_id = item.created_by_user_id)\n" +
                "  WHERE rhy.organisation_type = 'RHY'\n" +
                "    AND (item.creation_time BETWEEN ? AND ? OR item.modification_time BETWEEN ? AND ?)\n" +
                "  GROUP BY rhy.organisation_id\n" +
                "),\n" +
                "tapahtumat AS (\n" +
                "    SELECT\n" +
                "      rhy.organisation_id,\n" +
                "      sum(CASE WHEN creator.role = 'ROLE_USER' THEN 1 ELSE 0 END) AS created_to,\n" +
                "      sum(CASE WHEN creator.role = 'ROLE_MODERATOR' THEN 1 ELSE 0 END) AS created_mo,\n" +
                "      sum(CASE WHEN item.creation_time<>item.modification_time AND modifier.role = 'ROLE_USER' THEN 1 ELSE 0 END) AS modified_to,\n" +
                "      sum(CASE WHEN item.creation_time<>item.modification_time AND modifier.role = 'ROLE_MODERATOR' THEN 1 ELSE 0 END) AS modified_mo\n" +
                "    FROM organisation rhy\n" +
                "      JOIN calendar_event item ON (item.organisation_id = rhy.organisation_id)\n" +
                "      LEFT JOIN system_user modifier ON (modifier.user_id = item.modified_by_user_id)\n" +
                "      LEFT JOIN system_user creator ON (creator.user_id = item.created_by_user_id)\n" +
                "    WHERE rhy.organisation_type = 'RHY'\n" +
                "          AND (item.creation_time BETWEEN ? AND ? OR item.modification_time BETWEEN ? AND ?)\n" +
                "    GROUP BY rhy.organisation_id\n" +
                ")\n" +
                "SELECT\n" +
                "  parent.official_code   AS alue_koodi,\n" +
                "  rhy.official_code      AS rhy_koodi,\n" +
                "  rhy.name_finnish       AS rhy_nimi,\n" +
                "  tehtavat.created_to    AS tehtavat_created_to,\n" +
                "  tehtavat.created_mo    AS tehtavat_created_mo,\n" +
                "  tehtavat.modified_to   AS tehtavat_modified_to,\n" +
                "  tehtavat.modified_mo   AS tehtavat_modified_mo,\n" +
                "  tapahtumat.created_to  AS tapahtumat_created_to,\n" +
                "  tapahtumat.created_mo  AS tapahtumat_created_mo,\n" +
                "  tapahtumat.modified_to AS tapahtumat_modified_to,\n" +
                "  tapahtumat.modified_mo AS tapahtumat_modified_mo\n" +
                "FROM organisation rhy\n" +
                "  JOIN organisation parent ON (parent.organisation_id = rhy.parent_organisation_id)\n" +
                "  LEFT JOIN tehtavat ON (tehtavat.organisation_id = rhy.organisation_id)\n" +
                "  LEFT JOIN tapahtumat ON (tapahtumat.organisation_id = tehtavat.organisation_id)\n" +
                "  WHERE rhy.organisation_type='RHY'\n" +
                "ORDER BY parent.official_code, rhy.name_finnish";

        final Object[] args = {begin, end, begin, end, begin, end, begin, end};
        final List<AdminRhyEditMetricsDTO> dtos = jdbcTemplate.query(sql, args, createRowMapper2());
        return dtos;
    }

    private static RowMapper<AdminRhyEditMetricsDTO> createRowMapper2() {
        return (rs, rowNum) -> {
            AdminRhyEditMetricsDTO dto = new AdminRhyEditMetricsDTO(
                    rs.getString("alue_koodi"),
                    rs.getString("rhy_koodi"),
                    rs.getString("rhy_nimi"));

            dto.occupations.coordinator.created = rs.getInt("tehtavat_created_to");
            dto.occupations.coordinator.modified = rs.getInt("tehtavat_modified_to");

            dto.occupations.moderator.created = rs.getInt("tehtavat_created_mo");
            dto.occupations.moderator.modified = rs.getInt("tehtavat_modified_mo");

            dto.events.coordinator.created = rs.getInt("tapahtumat_created_to");
            dto.events.coordinator.modified = rs.getInt("tapahtumat_modified_to");

            dto.events.moderator.created = rs.getInt("tapahtumat_created_mo");
            dto.events.moderator.modified = rs.getInt("tapahtumat_modified_mo");

            return dto;
        };
    }
}
