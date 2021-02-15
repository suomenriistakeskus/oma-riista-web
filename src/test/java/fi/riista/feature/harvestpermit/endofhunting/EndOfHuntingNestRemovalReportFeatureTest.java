package fi.riista.feature.harvestpermit.endofhunting;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.nestremoval.HarvestPermitNestLocationType;
import fi.riista.feature.harvestpermit.nestremoval.HarvestPermitNestRemovalUsage;
import fi.riista.feature.harvestpermit.nestremoval.HarvestPermitNestRemovalUsageDTO;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonWithHunterNumberDTO;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import org.joda.time.DateTime;
import org.junit.Test;

import javax.annotation.Resource;

import java.util.List;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_EUROPEAN_BEAVER;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class EndOfHuntingNestRemovalReportFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private EndOfHuntingNestRemovalReportFeature feature;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Test
    public void testGetEndOfNestRemovalPermitReport() {
        final Person author = model().newPerson();
        persistInNewTransaction();

        final HarvestPermit permit = model().newHarvestPermit();
        permit.setOriginalContactPerson(author);
        permit.setPermitTypeCode(PermitTypeCode.NEST_REMOVAL_BASED);

        final DateTime harvestReportDate = DateUtil.today().toDateTimeAtCurrentTime();
        permit.setHarvestReportDate(harvestReportDate);
        permit.setHarvestReportState(HarvestReportState.APPROVED);
        permit.setEndOfHuntingReportComments("Comments");

        permit.setHarvestReportAuthor(author);
        permit.setHarvestReportModeratorOverride(false);

        final GameSpecies species = model().newGameSpecies(OFFICIAL_CODE_EUROPEAN_BEAVER);
        final HarvestPermitSpeciesAmount spa = model().newHarvestPermitSpeciesAmount(permit, species, null, null, 2);

        final HarvestPermitNestRemovalUsage nestRemovalUsage = model().newHarvestPermitNestRemovalUsage(spa, null, null, 1, geoLocation(), HarvestPermitNestLocationType.CONSTRUCTION);

        onSavedAndAuthenticated(createUser(author), () -> {
            final EndOfHuntingNestRemovalReportDTO dto = feature.getEndOfNestRemovalPermitReport(permit.getId());
            assertThat(dto, is(notNullValue()));

            assertThat(dto.getHarvestReportDate(), is(equalTo(harvestReportDate)));
            assertThat(dto.getHarvestReportState(), is(equalTo(HarvestReportState.APPROVED)));
            assertThat(dto.getEndOfHuntingReportComments(), is(equalTo("Comments")));
            assertThat(dto.getHarvestReportAuthor().getExtendedName(), is(equalTo(PersonWithHunterNumberDTO.create(author).getExtendedName())));

            final List<HarvestPermitNestRemovalUsageDTO> usages = dto.getUsages();
            assertThat(usages, hasSize(1));

            final HarvestPermitNestRemovalUsageDTO usage = usages.get(0);
            assertThat(usage.getSpeciesCode(), is(equalTo(spa.getGameSpecies().getOfficialCode())));
            assertThat(usage.getPermitNestAmount(), is(equalTo(spa.getNestAmount())));
            assertThat(usage.getUsedNestAmount(), is(equalTo(nestRemovalUsage.getNestAmount())));
            assertThat(usage.getPermitEggAmount(), is(equalTo(spa.getEggAmount())));
            assertThat(usage.getUsedEggAmount(), is(equalTo(nestRemovalUsage.getEggAmount())));
            assertThat(usage.getPermitConstructionAmount(), is(equalTo(spa.getConstructionAmount())));
            assertThat(usage.getUsedConstructionAmount(), is(equalTo(nestRemovalUsage.getConstructionAmount())));
        });
    }

    @Test
    public void testCreateEndOfHuntingReport() {
        final Person author = model().newPerson();
        persistInNewTransaction();

        final HarvestPermit permit = model().newHarvestPermit();
        permit.setOriginalContactPerson(author);
        permit.setPermitTypeCode(PermitTypeCode.NEST_REMOVAL_BASED);

        onSavedAndAuthenticated(createUser(author), () -> {
            feature.createEndOfHuntingReport(permit.getId(), null);

            runInTransaction(() -> {
                final HarvestPermit reloaded = harvestPermitRepository.getOne(permit.getId());
                assertThat(reloaded, is(notNullValue()));

                assertThat(DateUtil.toLocalDateNullSafe(reloaded.getHarvestReportDate()), is(equalTo(DateUtil.today())));
                assertThat(reloaded.getHarvestReportState(), is(equalTo(HarvestReportState.SENT_FOR_APPROVAL)));
                assertThat(reloaded.getEndOfHuntingReportComments(), is(nullValue()));
                assertThat(reloaded.getHarvestReportAuthor(), is(equalTo(author)));
                assertThat(reloaded.getHarvestReportModeratorOverride(), is(false));
            });
        });
    }

    @Test
    public void testCreateEndOfHuntingReport_moderator() {
        final Person author = model().newPerson();

        persistInNewTransaction();

        final HarvestPermit permit = model().newHarvestPermit();
        permit.setOriginalContactPerson(author);
        permit.setPermitTypeCode(PermitTypeCode.NEST_REMOVAL_BASED);

        final SystemUser moderator = createNewModerator();

        onSavedAndAuthenticated(moderator, () -> {
            final EndOfHuntingReportModeratorCommentsDTO comments = new EndOfHuntingReportModeratorCommentsDTO();
            comments.setEndOfHuntingReportComments("Comments");

            feature.createEndOfHuntingReport(permit.getId(), comments);

            runInTransaction(() -> {
                final HarvestPermit reloaded = harvestPermitRepository.getOne(permit.getId());
                assertThat(reloaded, is(notNullValue()));

                assertThat(DateUtil.toLocalDateNullSafe(reloaded.getHarvestReportDate()), is(equalTo(DateUtil.today())));
                assertThat(reloaded.getHarvestReportState(), is(equalTo(HarvestReportState.APPROVED)));
                assertThat(reloaded.getEndOfHuntingReportComments(), is(equalTo(comments.getEndOfHuntingReportComments())));
                assertThat(reloaded.getHarvestReportAuthor(), is(equalTo(author)));
                assertThat(reloaded.getHarvestReportModeratorOverride(), is(true));
            });
        });
    }
}
