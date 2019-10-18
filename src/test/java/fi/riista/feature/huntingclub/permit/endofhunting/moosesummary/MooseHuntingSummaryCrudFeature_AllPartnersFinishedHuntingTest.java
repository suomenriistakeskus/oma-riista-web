package fi.riista.feature.huntingclub.permit.endofhunting.moosesummary;

import com.google.common.collect.Sets;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.endofhunting.MooselikeHuntingFinishedException;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.huntingclub.permit.endofhunting.AllPartnersFinishedHuntingMailFeature;
import fi.riista.feature.huntingclub.permit.endofhunting.AllPartnersFinishedHuntingMailService;
import fi.riista.test.EmbeddedDatabaseTest;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Set;

import static fi.riista.util.DateUtil.today;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class MooseHuntingSummaryCrudFeature_AllPartnersFinishedHuntingTest extends EmbeddedDatabaseTest
        implements HuntingGroupFixtureMixin {

    @Resource
    private MooseHuntingSummaryCrudFeature crudFeature;

    @Resource
    private AllPartnersFinishedHuntingMailFeature huntingFinishedMailFeature;

    private AllPartnersFinishedHuntingMailService originalMailService;
    private AllPartnersFinishedHuntingMailService mockMailService;

    @Before
    public void setup() {
        originalMailService = huntingFinishedMailFeature.getMailService();
        mockMailService = mock(AllPartnersFinishedHuntingMailService.class);
        huntingFinishedMailFeature.setMailService(mockMailService);
    }

    @After
    public void cleanup() {
        huntingFinishedMailFeature.setMailService(originalMailService);
    }

    @Test
    public void testMailNotSentBecauseOtherPartnerHasNoSummary() {
        doTestIsMailSent(false, false, false);
    }

    @Test
    public void testMailNotSentBecauseOtherPartnersSummaryNotFinished() {
        doTestIsMailSent(false, true, false);
    }

    @Test
    public void testMailIsSent() {
        doTestIsMailSent(true, true, true);
    }

    private void doTestIsMailSent(final boolean expectedIsMailSent,
                                  final boolean isOtherPartnerMooseHuntingFinished,
                                  final boolean otherPartnerHasMooseHuntingSummary) {

        withPerson(otherPermitContactPerson -> withMooseHuntingGroupFixture(f -> {
            model().newHarvestPermitContactPerson(f.permit, otherPermitContactPerson);

            withHuntingGroupFixture(f.speciesAmount, f2 -> {

                createHarvest(f.permit, f.group);
                createHarvest(f.permit, f2.group);

                if (otherPartnerHasMooseHuntingSummary) {
                    // Intermediary flush needed before persisting MooseHuntingSummary in order to have
                    // harvest_permit_partners table populated required for foreign key constraint.
                    persistInNewTransaction();
                    model().newMooseHuntingSummary(f.permit, f2.club, isOtherPartnerMooseHuntingFinished);
                }

                onSavedAndAuthenticated(createUser(f.clubContact), () -> {
                    final MooseHuntingSummaryDTO dto = new MooseHuntingSummaryDTO();
                    dto.setClubId(f.club.getId());
                    dto.setHarvestPermitId(f.permit.getId());
                    dto.setTotalHuntingArea(123);
                    dto.setRemainingPopulationInTotalArea(456);
                    dto.setHuntingEndDate(today());
                    dto.setHuntingFinished(true);

                    crudFeature.create(dto);

                    if (expectedIsMailSent) {
                        final Set<String> emails = Sets.newHashSet(f.clubContact.getEmail(),
                                f.permit.getOriginalContactPerson().getEmail(), otherPermitContactPerson.getEmail());

                        verify(mockMailService).sendEmailAsync(eq(emails), any());
                    } else {
                        verifyNoMoreInteractions(mockMailService);
                    }
                });
            });
        }));
    }

    private void createHarvest(final HarvestPermit permit, final HuntingClubGroup group) {
        final Harvest harvest = model().newHarvest(group.getSpecies());
        harvest.setRhy(permit.getRhy());

        final GroupHuntingDay huntingDay =
                model().newGroupHuntingDay(group, LocalDate.fromDateFields(harvest.getPointOfTime()));
        harvest.updateHuntingDayOfGroup(huntingDay, null);

        model().newHarvestSpecimen(harvest, GameAge.ADULT, GameGender.MALE);
    }

    @Test
    public void testUpdate_whenHarvestReportNotDone() {
        doTestUpdate(false);
    }

    @Test(expected = MooselikeHuntingFinishedException.class)
    public void testUpdate_whenHarvestReportDone() {
        doTestUpdate(true);
    }

    private void doTestUpdate(final boolean permitHolderFinishedHunting) {
        withMooseHuntingGroupFixture(f -> {
            f.speciesAmount.setMooselikeHuntingFinished(permitHolderFinishedHunting);

            persistInNewTransaction();
            final MooseHuntingSummary summary = model().newMooseHuntingSummary(f.permit, f.club, true);

            onSavedAndAuthenticated(createUser(f.clubContact), () -> {
                final MooseHuntingSummaryDTO dto = crudFeature.read(summary.getId());
                dto.setEffectiveHuntingArea(dto.getEffectiveHuntingArea() - 1);

                crudFeature.update(dto);
            });
        });
    }
}
