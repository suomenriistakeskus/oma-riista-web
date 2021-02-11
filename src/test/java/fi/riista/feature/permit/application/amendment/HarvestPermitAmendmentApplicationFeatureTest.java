package fi.riista.feature.permit.application.amendment;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationRepository;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.Locales;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HarvestPermitAmendmentApplicationFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private HarvestPermitAmendmentApplicationFeature feature;

    @Resource
    private HarvestPermitApplicationRepository repository;

    private Riistanhoitoyhdistys rhy;
    private Person originalContactPerson;
    private Person otherContactPerson;
    private HarvestPermit originalPermit;
    private GameSpecies species;

    @Before
    public void setup() {
        originalContactPerson = model().newPerson();
        otherContactPerson = model().newPerson();
        species = model().newGameSpecies();
        rhy = model().newRiistanhoitoyhdistys();
        final PermitDecision decision = model().newPermitDecision(model().newHarvestPermitApplication(rhy, model().newHarvestPermitArea(), species));
        originalPermit = model().newHarvestPermit(rhy);
        originalPermit.setPermitDecision(decision);
        originalPermit.setOriginalContactPerson(originalContactPerson);
        model().newHarvestPermitContactPerson(originalPermit, otherContactPerson);

        persistInNewTransaction();
    }

    @Test
    public void testOriginalPersonCreateApplication() {
        onSavedAndAuthenticated(createNewUser("original", originalContactPerson), () -> {
            final HarvestPermitAmendmentApplicationDTO dto = feature.createAmendmentApplication(createDto(), Locales.FI);
            final HarvestPermitApplication application = repository.findById(dto.getId()).get();
            assertNotNull(application);
            assertEquals(originalContactPerson, application.getContactPerson());
        });
    }

    @Test
    public void testOtherContactPersonCreateApplication() {
        onSavedAndAuthenticated(createNewUser("other", otherContactPerson), () -> {
            final HarvestPermitAmendmentApplicationDTO dto = feature.createAmendmentApplication(createDto(), Locales.FI);
            final HarvestPermitApplication application = repository.findById(dto.getId()).get();
            assertNotNull(application);
            assertEquals(otherContactPerson, application.getContactPerson());
        });
    }

    @Test
    public void testModeratorPersonCreateApplication() {
        onSavedAndAuthenticated(createNewModerator(), () -> {
            final HarvestPermitAmendmentApplicationDTO dto = feature.createAmendmentApplication(createDto(), Locales.FI);
            final HarvestPermitApplication application = repository.findById(dto.getId()).get();
            assertNotNull(application);
            assertEquals(originalContactPerson, application.getContactPerson());
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testNonContactPersonCannotCreateApplication() {
        onSavedAndAuthenticated(createNewUser(), () -> {
            feature.createAmendmentApplication(createDto(), Locales.FI);
        });
    }

    private HarvestPermitAmendmentApplicationCreateDTO createDto() {
        final HarvestPermitAmendmentApplicationCreateDTO dto = new HarvestPermitAmendmentApplicationCreateDTO();
        dto.setGameSpeciesCode(species.getOfficialCode());
        dto.setNonEdibleHarvestId(null);
        dto.setOriginalPermitId(originalPermit.getId());
        return dto;
    }

}
