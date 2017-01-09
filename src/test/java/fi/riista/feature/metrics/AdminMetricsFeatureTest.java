package fi.riista.feature.metrics;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.report.HarvestReport;
import fi.riista.feature.harvestpermit.report.fields.HarvestReportFields;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.util.DateUtil;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

public class AdminMetricsFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private AdminMetricsFeature adminMetricsFeature;

    @Test
    public void testCountUsers() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final HuntingClub club = model().newHuntingClub(rhy);
        final HuntingClubGroup group = model().newHuntingClubGroup(club);

        // Club occupations should not be counted
        // +2 person
        model().newHuntingClubMember(club, OccupationType.SEURAN_JASEN);
        model().newHuntingClubGroupMember(group, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        // +2 person
        // +2 user
        createUser(model().newHuntingClubMember(club, OccupationType.SEURAN_JASEN));
        createUser(model().newHuntingClubGroupMember(group, OccupationType.RYHMAN_METSASTYKSENJOHTAJA));

        // +1 person
        // +1 occupation
        model().newOccupation(rhy, model().newPerson(), OccupationType.PUHEENJOHTAJA);
        // +1 person
        // +1 user
        // +1 occupation with password
        createUser(model().newOccupation(rhy, model().newPerson(), OccupationType.VARAPUHEENJOHTAJA));
        // deleted occupation not counted
        // +1 person
        // +1 user
        createUser(model().newOccupation(rhy, model().newPerson(), OccupationType.PETOYHDYSHENKILO))
                .softDelete();

        onSavedAndAuthenticated(createNewAdmin(), () -> {
            final AdminMetricsDTO dto = adminMetricsFeature.getBasicMetrics();
            assertEquals(7, dto.getCountPerson());
            assertEquals(4, dto.getCountNormalUser());
            assertEquals(1, dto.getCountNormalUserWithOccupationAndPassword());
            assertEquals(2, dto.getCountAllPeopleWithOccupation());
        });
    }

    private Occupation createUser(Occupation occ) {
        createNewUser(occ.getPerson().getFullName().replace(" ", ""), occ.getPerson());
        return occ;
    }

    @Test
    public void testHarvestReportMetrics() {
        final SystemUser user = createNewUser();
        final SystemUser admin = createNewAdmin();

        //need to persist here, otherwise user does not have id and queries will not work
        persistInNewTransaction();

        final RiistakeskuksenAlue rka1 = model().newRiistakeskuksenAlue();
        final Riistanhoitoyhdistys rhy1_1 = model().newRiistanhoitoyhdistys(rka1);
        final Riistanhoitoyhdistys rhy1_2 = model().newRiistanhoitoyhdistys(rka1);

        final RiistakeskuksenAlue rka2 = model().newRiistakeskuksenAlue();
        final Riistanhoitoyhdistys rhy2 = model().newRiistanhoitoyhdistys(rka2);

        final GameSpecies species = model().newGameSpecies();

        HarvestPermit permit = model().newHarvestPermit(rhy1_1);
        permit.setPermitTypeCode("250");

        HarvestPermit listPermit = model().newHarvestPermit(rhy2);
        listPermit.setHarvestsAsList(true);
        listPermit.setPermitTypeCode("300");

        insertApprovedHarvestReport(user, rhy1_1, species, permit);
        insertApprovedHarvestReport(admin, rhy1_1, species, permit);
        insertApprovedHarvestReport(user, rhy1_2, species);

        insertApprovedHarvestReport(user, rhy2, species, permit);
        insertApprovedHarvestReport(admin, rhy2, species, listPermit);
        insertApprovedHarvestReport(user, rhy2, species);

        authenticate(admin);

        final Date begin = getTestStartTime().toDate();
        final Date end = DateUtil.now().toDate();
        final List<AdminHarvestReportMetricsDTO> result = adminMetricsFeature.getHarvestReportMetrics(begin, end);

        assertEquals(5, result.size());

        String speciesName = species.getNameFinnish();
        assertDTO(findDto(result, rka1, null), rka1, speciesName, false, 1, 0);
        assertDTO(findDto(result, rka1, permit.getPermitTypeCode()), rka1, null, true, 1, 1);
        assertDTO(findDto(result, rka2, permit.getPermitTypeCode()), rka2, null, true, 1, 0);
        assertDTO(findDto(result, rka2, listPermit.getPermitTypeCode()), rka2, null, true, 0, 1);
        assertDTO(findDto(result, rka2, null), rka2, speciesName, false, 1, 0);
    }

    private static void assertDTO(AdminHarvestReportMetricsDTO dto, RiistakeskuksenAlue rka, String speciesName,
                                  boolean permit, int userCount, int moderatorCount) {
        assertEquals(permit, dto.isPermit());
        assertEquals(rka.getNameFinnish(), dto.getRka());
        assertEquals(speciesName, dto.getSpecies());
        assertEquals(userCount, dto.getUserCount());
        assertEquals(moderatorCount, dto.getModeratorCount());
        assertEquals(userCount + moderatorCount, dto.getReportsTotal());
    }

    private static AdminHarvestReportMetricsDTO findDto(
            List<AdminHarvestReportMetricsDTO> result, RiistakeskuksenAlue rka1, String permitTypeCode) {

        for (AdminHarvestReportMetricsDTO dto : result) {
            if (Objects.equals(dto.getPermitTypeCode(), permitTypeCode) && dto.getRka().equals(rka1.getNameFinnish())) {
                return dto;
            }
        }
        throw new RuntimeException(String.format("DTO not found %s %s %s", rka1.getNameFinnish(), permitTypeCode, result));
    }

    private void insertApprovedHarvestReport(SystemUser user, Riistanhoitoyhdistys rhy, GameSpecies species) {
        insertApprovedHarvestReport(user, rhy, species, null);
    }

    private void insertApprovedHarvestReport(SystemUser user, Riistanhoitoyhdistys rhy, GameSpecies species, HarvestPermit permit) {
        activeUserService().loginWithoutCheck(user);
        final Harvest harvest = model().newHarvest(species, rhy);
        harvest.setHarvestPermit(permit);
        HarvestReportFields fields = model().newHarvestReportFields(species, false);
        HarvestReport report = model().newHarvestReport(fields, HarvestReport.State.APPROVED, harvest);
        report.setHarvestPermit(permit);
        // needs to persist here to force entities createdBy to be the currently authenticated user
        persistInNewTransaction();
    }
}
