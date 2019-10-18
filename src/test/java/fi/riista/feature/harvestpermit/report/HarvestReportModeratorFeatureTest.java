package fi.riista.feature.harvestpermit.report;

import fi.riista.feature.error.RevisionConflictException;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestRepository;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenValidationException;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.annotation.Resource;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static fi.riista.feature.harvestpermit.report.HarvestReportState.APPROVED;
import static fi.riista.feature.harvestpermit.report.HarvestReportState.REJECTED;
import static fi.riista.feature.harvestpermit.report.HarvestReportState.SENT_FOR_APPROVAL;
import static org.junit.Assert.assertEquals;

public class HarvestReportModeratorFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private HarvestReportModeratorFeature feature;

    @Resource
    private HarvestRepository harvestRepository;

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void testStateChangeOk() {
        withBearWithPermitHarvestReport(h -> {
            final HarvestReportState toState = APPROVED;
            feature.changeHarvestReportState(createDto(h, toState, null));

            runInTransaction(() -> {
                final Harvest harvest = harvestRepository.getOne(h.getId());
                assertEquals(toState, harvest.getHarvestReportState());
                assertEquals(1, harvest.getChangeHistory().size());
                assertEquals(toState, harvest.getChangeHistory().get(0).getHarvestReportState());
            });
        });

    }

    @Test
    public void testReasonNotGivenWhenRequired() {
        withBearWithPermitHarvestReport(h -> {
            thrown.expect(IllegalArgumentException.class);
            thrown.expectMessage("Reason is required for moderator/admin");
            feature.changeHarvestReportState(createDto(h, REJECTED, null));
        });
    }

    @Test
    public void testRevisionConflict() {
        withBearWithPermitHarvestReport(h -> {

            final HarvestReportStateChangeDTO dto = createDto(h, APPROVED, null);
            dto.setRev(dto.getRev() - 1);

            thrown.expect(RevisionConflictException.class);
            feature.changeHarvestReportState(dto);
        });
    }


    @Test
    public void testPermitHarvestReportApproved() {
        withBearWithPermitHarvestReport((h, p) -> {
            runInTransaction(() -> {
                final Harvest harvest = harvestRepository.getOne(h.getId());
                final HarvestPermit permit = harvest.getHarvestPermit();
                permit.setHarvestReportAuthor(harvest.getAuthor());
                permit.setHarvestReportDate(DateUtil.now());
                permit.setHarvestReportModeratorOverride(false);
                permit.setHarvestReportState(APPROVED);
            });
            thrown.expect(IllegalArgumentException.class);
            thrown.expectMessage("Permit harvest report is approved");
            feature.changeHarvestReportState(createDto(h, APPROVED, null));
        });
    }

    @Test
    public void testHarvestNotHarvestReport() {
        withBearWithPermitHarvestReport(h -> {
            runInTransaction(() -> {
                final Harvest harvest = harvestRepository.getOne(h.getId());
                harvest.setHarvestReportState(null);
                harvest.setHarvestReportAuthor(null);
                harvest.setHarvestReportDate(null);
                persistInCurrentlyOpenTransaction();

                thrown.expect(IllegalArgumentException.class);
                thrown.expectMessage("Harvest report is not done");
                feature.changeHarvestReportState(createDto(harvest, APPROVED, null));
            });
        });
    }

    @Test
    public void testHarvestFieldsIncomplete() {
        withBearWithPermitHarvestReport(h -> {
            runInTransaction(() -> {
                final Harvest harvest = harvestRepository.getOne(h.getId());
                harvest.getSortedSpecimens().get(0).setWeight(null);
                harvest.getSortedSpecimens().get(0).setGender(null);

                thrown.expect(HarvestSpecimenValidationException.class);
                feature.changeHarvestReportState(createDto(harvest, APPROVED, null));
            });
        });
    }

    private void withBearWithPermitHarvestReport(final Consumer<Harvest> consumer) {
        withBearWithPermitHarvestReport((h, p) -> consumer.accept(h));
    }

    private void withBearWithPermitHarvestReport(final BiConsumer<Harvest, HarvestPermit> consumer) {
        final GameSpecies bear = model().newGameSpecies(GameSpecies.OFFICIAL_CODE_BEAR);
        final HarvestPermit permit = model().newHarvestPermit();
        model().newHarvestPermitSpeciesAmount(permit, bear);

        final Harvest harvest = model().newHarvest(bear);
        harvest.setHarvestPermit(permit);
        harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.ACCEPTED);
        harvest.setRhy(permit.getRhy());
        harvest.setHarvestReportState(SENT_FOR_APPROVAL);
        harvest.setHarvestReportAuthor(harvest.getAuthor());
        harvest.setHarvestReportDate(DateUtil.now());

        model().newHarvestSpecimen(harvest, GameAge.YOUNG, GameGender.MALE, 1.0);

        onSavedAndAuthenticated(createNewModerator(), () -> {
            consumer.accept(harvest, permit);
        });
    }

    private static HarvestReportStateChangeDTO createDto(final Harvest harvest, final HarvestReportState toState, final String reason) {
        final HarvestReportStateChangeDTO dto = new HarvestReportStateChangeDTO();
        dto.setHarvestId(harvest.getId());
        dto.setRev(harvest.getConsistencyVersion());
        dto.setTo(toState);
        dto.setReason(reason);
        return dto;
    }
}
