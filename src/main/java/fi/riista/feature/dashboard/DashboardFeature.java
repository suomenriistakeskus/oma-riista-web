package fi.riista.feature.dashboard;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Ordering;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.account.user.QSystemUser;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.announcement.AnnouncementSenderType;
import fi.riista.feature.gamediary.srva.SrvaEventStateEnum;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.huntingclub.QHuntingClub;
import fi.riista.feature.huntingclub.area.QHuntingClubArea;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.occupation.QOccupation;
import fi.riista.feature.organization.person.QPerson;
import fi.riista.feature.permit.application.QHarvestPermitApplication;
import fi.riista.feature.permit.area.QHarvestPermitArea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Component
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
public class DashboardFeature {

    private JdbcTemplate jdbcTemplate;

    @Resource
    private JPQLQueryFactory queryFactory;

    @Resource
    private DashboardMooselikeHuntingService dashboardMooselikeHuntingService;

    @Autowired
    public void setDataSource(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Transactional(readOnly = true)
    public DashboardUsersDTO getMetricsUsers() {
        final DashboardUsersDTO dto = new DashboardUsersDTO();
        countUsers(dto);
        countModerators(dto);
        countCoordinators(dto);
        return dto;
    }

    private void countUsers(DashboardUsersDTO dto) {
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

    private void countModerators(DashboardUsersDTO dto) {
        // Moderaattorit jotka ovat rekisteröityneet
        dto.setCountModeratorWithPassword(jdbcTemplate.queryForObject(
                "SELECT COUNT(password) FROM system_user WHERE role = 'ROLE_MODERATOR'"
                , Long.class));

        // Kaikki moderaattorit
        dto.setCountAllModerators(jdbcTemplate.queryForObject(
                "SELECT COUNT(user_id) FROM system_user WHERE role = 'ROLE_MODERATOR'"
                , Long.class));
    }

    private void countCoordinators(DashboardUsersDTO dto) {
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

    @Transactional(readOnly = true)
    public DashboardPdfDTO getMetricsPdf() {
        final DashboardPdfDTO dto = new DashboardPdfDTO();
        countPdfDownloads(dto);
        return dto;
    }

    private void countPdfDownloads(DashboardPdfDTO dto) {
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

    @Transactional(readOnly = true)
    public ImmutableMap<String, Object> getMobileLogins() {
        final List<DashboardMobileLoginPlatformDTO> platforms = jdbcTemplate.query("SELECT to_char(login_time, 'YYYY-MM') AS login_time," +
                " sum(android) AS android," +
                " sum(ios) AS ios," +
                " sum(wp) AS wp" +
                " FROM (SELECT login_time::DATE AS login_time," +
                " sum(CASE WHEN platform = 'android' THEN 1 ELSE 0 END) AS android, " +
                " sum(CASE WHEN platform = 'ios' THEN 1 ELSE 0 END) AS ios, " +
                " sum(CASE WHEN platform = 'wp' THEN 1 ELSE 0 END) AS wp" +
                " FROM mobile_login_event" +
                " WHERE login_time > NOW() - INTERVAL '12 MONTH'" +
                " GROUP BY 1) d GROUP BY 1 ORDER BY 1", (resultSet, i) -> new DashboardMobileLoginPlatformDTO(
                resultSet.getString("login_time"),
                resultSet.getLong("android"),
                resultSet.getLong("ios"),
                resultSet.getLong("wp")
        ));

        final List<DashboardMobileLoginVersionDTO> versions = jdbcTemplate.query("SELECT" +
                " platform || '-' || software_version AS version," +
                " count(*) AS lkm" +
                " FROM mobile_login_event" +
                " WHERE login_time > NOW() - INTERVAL '3 MONTH'" +
                " GROUP BY 1 ORDER BY 1", (resultSet, i) -> new DashboardMobileLoginVersionDTO(
                resultSet.getString("version"),
                resultSet.getLong("lkm")
        ));

        return ImmutableMap.<String, Object>builder()
                .put("platform", platforms)
                .put("version", versions)
                .build();
    }

    @Transactional(readOnly = true)
    public DashboardClubsDTO getMetricsClubs() {
        final DashboardClubsDTO dto = new DashboardClubsDTO();
        countClubs(dto);
        return dto;
    }

    private void countClubs(DashboardClubsDTO dto) {
        final QHuntingClub CLUB = QHuntingClub.huntingClub;
        dto.setAllClubs(queryFactory.from(CLUB).fetchCount());
        dto.setUserCreatedClubs(queryFactory.from(CLUB).where(CLUB.userCreated()).fetchCount());

        final QHuntingClubArea CLUB_AREA = QHuntingClubArea.huntingClubArea;
        dto.setClubAreas(queryFactory.from(CLUB_AREA).where(CLUB_AREA.active.isTrue()).fetchCount());

        final QHarvestPermitArea PERMIT_AREA = QHarvestPermitArea.harvestPermitArea;
        dto.setClubPermitAreas(queryFactory.from(PERMIT_AREA).fetchCount());

        final QHarvestPermitApplication PERMIT_APPLICATION = QHarvestPermitApplication.harvestPermitApplication;
        dto.setPermitApplications(queryFactory.from(PERMIT_APPLICATION)
                .where(PERMIT_APPLICATION.status.eq(HarvestPermitApplication.Status.ACTIVE))
                .fetchCount());
    }

    @Transactional(readOnly = true)
    public DashboardMooseHuntingDTO getMetricsMooseHunting() {
        final DashboardMooseHuntingDTO dto = new DashboardMooseHuntingDTO();
        dto.setMooselikeHuntingMetrics(dashboardMooselikeHuntingService.computeMooselikeHuntingMetrics());
        return dto;
    }

    @Transactional(readOnly = true)
    public DashboardHarvestsObservationsDTO getMetricsHarvestsObservations() {
        final DashboardHarvestsObservationsDTO dto = new DashboardHarvestsObservationsDTO();
        countHarvests(dto);
        countObservations(dto);
        countHarvestReports(dto);
        return dto;
    }

    private void countHarvests(DashboardHarvestsObservationsDTO dto) {
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
                        " OR from_mobile = TRUE AND geolocation_source = 'MANUAL'", Long.class));

        // Saaliskirjauksia webissä
        dto.setCountHarvestFromWeb(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM harvest" +
                        " WHERE deletion_time IS NULL" +
                        " AND geolocation_source = 'MANUAL'" +
                        " AND (from_mobile = FALSE OR from_mobile IS NULL)", Long.class));
    }

    private void countObservations(DashboardHarvestsObservationsDTO dto) {
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
                        " AND from_mobile = TRUE", Long.class));

        // Havaintoja webissä
        dto.setCountObservationFromWeb(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM game_observation" +
                        " WHERE deletion_time IS NULL" +
                        " AND from_mobile = FALSE", Long.class));
    }

    private void countHarvestReports(DashboardHarvestsObservationsDTO dto) {
        // Saalisilmoituksia yhteensä
        dto.setCountHarvestReport(jdbcTemplate.queryForObject(
                "SELECT COALESCE(COUNT(harvest_id),0) FROM harvest WHERE harvest_report_state IS NOT NULL;", Long.class));

        // Saalisilmoituksia yhteensä hyväksytty
        dto.setCountHarvestReportApproved(jdbcTemplate.queryForObject(
                "SELECT COALESCE(COUNT(harvest_id),0) FROM harvest WHERE harvest_report_state = 'APPROVED';", Long.class));
    }

    @Transactional(readOnly = true)
    public DashboardSrvaDTO getMetricsSrva() {
        final DashboardSrvaDTO dto = new DashboardSrvaDTO();
        countSrvaEvents(dto);
        return dto;
    }

    private void countSrvaEvents(DashboardSrvaDTO dto) {
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
                "SELECT COUNT(*) FROM srva_event WHERE from_mobile = TRUE", Long.class));

        // Srva-kirjauksia webissä
        dto.setCountSrvaEventFromWeb(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM srva_event WHERE from_mobile = FALSE", Long.class));

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

    @Transactional(readOnly = true)
    public List<DashboardHarvestReportDTO> getHarvestReportMetrics(Date begin, Date end) {
        final String sql = "-- kiintiömetsästys ja muu ei-luvanvarainen: rka päätellään saaliskirjauksen RHY:stä\n" +
                "(SELECT rka.name_finnish AS rka,\n" +
                "        g.name_finnish AS species,\n" +
                "        cast(NULL AS TEXT ) AS permit_type,\n" +
                "        cast(NULL AS TEXT ) AS permit_type_code,\n" +
                "        (SELECT TRUE) AS season,\n" +
                "        (SELECT FALSE) AS permit,\n" +
                "        coalesce(sum(CASE WHEN h.moderator_override IS FALSE THEN 1 END), 0) AS user_count,\n" +
                "        coalesce(sum(CASE WHEN h.moderator_override IS TRUE THEN 1 END), 0) AS moderator_count,\n" +
                "        count(DISTINCT h.harvest_id) AS reports_total\n" +
                " FROM harvest h\n" +
                "   JOIN organisation rhy ON rhy.organisation_id = h.rhy_id\n" +
                "   JOIN organisation rka ON rka.organisation_id = rhy.parent_organisation_id\n" +
                "   JOIN game_species g ON g.game_species_id=h.game_species_id\n" +
                " WHERE h.harvest_report_state = 'APPROVED'\n" +
                "       AND h.harvest_report_date BETWEEN ? AND ?\n" +
                "       AND h.harvest_permit_id IS NULL\n" +
                " GROUP BY rka.name_finnish,g.name_finnish)\n" +
                "\n" +
                "UNION ALL\n" +
                "\n" +
                "-- poikkeuslupa: rka päätellään luvan RHY:stä\n" +
                "(SELECT rka.name_finnish AS rka,\n" +
                "        cast(NULL AS TEXT ) AS species,\n" +
                "        hp.permit_type AS permit_type,\n" +
                "        hp.permit_type_code AS permit_type_code,\n" +
                "        (SELECT FALSE) AS season,\n" +
                "        (SELECT TRUE) AS permit,\n" +
                "        coalesce(sum(CASE WHEN hp.harvest_report_moderator_override IS FALSE THEN 1 END), 0) AS user_count,\n" +
                "        coalesce(sum(CASE WHEN hp.harvest_report_moderator_override IS TRUE THEN 1 END), 0) AS moderator_count,\n" +
                "        count(DISTINCT hp.harvest_permit_id) AS reports_total\n" +
                " FROM harvest_permit hp\n" +
                "   JOIN organisation rhy ON rhy.organisation_id = hp.rhy_id\n" +
                "   JOIN organisation rka ON rka.organisation_id = rhy.parent_organisation_id\n" +
                " WHERE hp.harvest_report_state = 'APPROVED'\n" +
                "       AND hp.harvest_report_date BETWEEN ? AND ?\n" +
                "       AND hp.harvests_as_list=TRUE\n" +
                " GROUP BY rka.name_finnish,hp.permit_type,hp.permit_type_code)\n" +
                "\n" +
                "UNION ALL\n" +
                "\n" +
                "--  ei-poikkeuslupa: rka päätellään saaliiskirjauksen RHY:stä\n" +
                "(SELECT rka.name_finnish AS rka,\n" +
                "        cast(NULL AS TEXT ) AS species,\n" +
                "        hp.permit_type AS permit_type,\n" +
                "        hp.permit_type_code AS permit_type_code,\n" +
                "        (SELECT FALSE) AS season,\n" +
                "        (SELECT TRUE) AS permit,\n" +
                "        coalesce(sum(CASE WHEN h.moderator_override IS FALSE THEN 1 END), 0) AS user_count,\n" +
                "        coalesce(sum(CASE WHEN h.moderator_override IS TRUE THEN 1 END), 0) AS moderator_count,\n" +
                "        count(DISTINCT h.harvest_id) AS reports_total\n" +
                " FROM harvest_permit hp\n" +
                "   JOIN harvest h ON hp.harvest_permit_id=h.harvest_permit_id\n" +
                "   JOIN organisation rhy ON rhy.organisation_id = h.rhy_id\n" +
                "   JOIN organisation rka ON rka.organisation_id = rhy.parent_organisation_id\n" +
                " WHERE h.harvest_report_state = 'APPROVED'\n" +
                "       AND h.harvest_report_date BETWEEN ? AND ?\n" +
                "       AND hp.harvests_as_list=FALSE\n" +
                " GROUP BY rka.name_finnish,hp.permit_type,hp.permit_type_code)\n";

        final Object[] args = {begin, end, begin, end, begin, end};
        final List<DashboardHarvestReportDTO> dtos = jdbcTemplate.query(sql, args, createRowMapper());
        Collections.sort(dtos, cmp());
        return dtos;
    }

    private static Comparator<? super DashboardHarvestReportDTO> cmp() {
        return (o1, o2) -> ComparisonChain.start()
                .compare(o1.getRka(), o2.getRka())
                .compare(o1.getSpecies(), o2.getSpecies(), Ordering.natural().nullsFirst())
                .compare(o1.getPermitType(), o2.getPermitType())
                .result();
    }

    private static RowMapper<DashboardHarvestReportDTO> createRowMapper() {
        return (rs, rowNum) -> {
            DashboardHarvestReportDTO dto = new DashboardHarvestReportDTO();
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
    public List<DashboardRhyEditDTO> getRhyEditMetrics(Date begin, Date end) {
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
        return jdbcTemplate.query(sql, args, createRowMapper2());
    }

    private static RowMapper<DashboardRhyEditDTO> createRowMapper2() {
        return (rs, rowNum) -> {
            DashboardRhyEditDTO dto = new DashboardRhyEditDTO(
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

    @Transactional(readOnly = true)
    public DashboardAnnouncementsDTO getAnnouncementMetrics() {
        final DashboardAnnouncementsDTO dto = new DashboardAnnouncementsDTO();

        jdbcTemplate.query("SELECT sender_type, count(*) FROM announcement GROUP BY sender_type",
                new Object[]{}, (RowCallbackHandler) resultSet -> {
                    final AnnouncementSenderType senderType = AnnouncementSenderType.valueOf(resultSet.getString(1));
                    final long count = resultSet.getLong(2);

                    switch (senderType) {
                        case RIISTAKESKUS:
                            dto.setSenderTypeModerator(count);
                            break;
                        case TOIMINNANOHJAAJA:
                            dto.setSenderTypeCoordinator(count);
                            break;
                        case SEURAN_YHDYSHENKILO:
                            dto.setSenderTypeClub(count);
                            break;
                        default:
                            break;
                    }
                });

        dto.setTotal(dto.getSenderTypeClub() + dto.getSenderTypeCoordinator() + dto.getSenderTypeModerator());

        return dto;
    }
}
