package fi.riista.feature.harvest;

import fi.riista.config.Constants;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.GameCategory;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.fixture.HarvestSpecimenType;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.Locales;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;

import static fi.riista.util.DateUtil.now;
import static fi.riista.util.DateUtil.today;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class LegalHarvestCertificatePdfFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private LegalHarvestCertificatePdfFeature feature;


    private Person harvestPerson;
    private GameSpecies bearSpecies;
    private GameSpecies lynxSpecies;
    private SystemUser moderator;
    private Riistanhoitoyhdistys rhy;

    @Before
    public void setup() {
        runInTransaction(() -> {
            bearSpecies = model().newGameSpecies(GameSpecies.OFFICIAL_CODE_BEAR, GameCategory.GAME_MAMMAL, "karhu",
                    "björn", "bear");
            lynxSpecies = model().newGameSpecies(GameSpecies.OFFICIAL_CODE_LYNX, GameCategory.GAME_MAMMAL, "ilves",
                    "lodjur", "lynx");
            harvestPerson = model().newPerson("etu", "suku", "010101-0101", "88888888");
            moderator = createNewModerator();
            rhy = model().newRiistanhoitoyhdistys();
            persistInNewTransaction();
            entityManager().flush();
        });
    }

    @Test
    public void testFinnish() {
        final Harvest harvest = createAcceptedHarvest(bearSpecies);

        persistInNewTransaction();

        onSavedAndAuthenticated(moderator, () -> {
            runInTransaction(() -> {
                final LegalHarvestCertificatePdfFeature.PdfModel pdfModel = feature.getPdfModel(harvest.getId(),
                        Locales.FI);
                final LegalHarvestCertificateDTO dto = (LegalHarvestCertificateDTO) pdfModel.getModel();
                assertEquals(bearSpecies.getNameFinnish(), dto.getSpecies());
                assertEquals(rhy.getNameFinnish(), dto.getRhy());
                assertEquals("uros", dto.getGender().toLowerCase());
            });

        });
    }

    @Test
    public void testSwedish() {
        final Harvest harvest = createAcceptedHarvest(bearSpecies);

        persistInNewTransaction();

        onSavedAndAuthenticated(moderator, () -> {
            runInTransaction(() -> {
                final LegalHarvestCertificatePdfFeature.PdfModel pdfModel = feature.getPdfModel(harvest.getId(),
                        Locales.SV);
                final LegalHarvestCertificateDTO dto = (LegalHarvestCertificateDTO) pdfModel.getModel();
                assertEquals(bearSpecies.getNameSwedish(), dto.getSpecies());
                assertEquals(rhy.getNameSwedish(), dto.getRhy());
                assertEquals("handjur", dto.getGender().toLowerCase());
            });

        });
    }

    @Test(expected = AccessDeniedException.class)
    public void test_unauthorized() {
        onSavedAndAuthenticated(createNewUser("user"), () -> {
            feature.getPdfModel(42, Locales.FI);

        });
    }

    @Test(expected = IllegalStateException.class)
    public void testLynxWithNoPermit() {
        final Harvest harvest = createAcceptedHarvest(lynxSpecies);

        persistInNewTransaction();

        onSavedAndAuthenticated(moderator, () -> {
            runInTransaction(() -> {
                feature.getPdfModel(harvest.getId(), Locales.FI);
                fail("Cannot create certificate for lynx with no permit");
            });

        });
    }

    private Harvest createAcceptedHarvest(final GameSpecies species) {
        final Harvest harvest = model().newHarvest(harvestPerson);
        harvest.setRhy(rhy);
        model().newHarvestSpecimen(harvest, HarvestSpecimenType.ADULT_MALE);
        harvest.setSpecies(species);
        harvest.setPointOfTime(today().toDateTimeAtStartOfDay(Constants.DEFAULT_TIMEZONE));
        harvest.setHarvestReportState(HarvestReportState.APPROVED);
        harvest.setHarvestReportDate(now());
        harvest.setHarvestReportAuthor(harvestPerson);
        model().newHarvestChangeHistory(harvest, HarvestReportState.APPROVED, moderator);
        return harvest;
    }

}
