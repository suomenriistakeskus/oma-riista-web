package fi.riista.feature.gamediary;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.gamediary.fixture.HarvestDTOBuilderFactory;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.todo.GameDiaryTodoFeature;
import fi.riista.feature.gamediary.todo.GameDiaryTodoHarvestDTO;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.organization.person.PersonWithHunterNumberDTO;
import fi.riista.util.F;
import org.joda.time.LocalDate;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class GameDiaryTodoFeatureTest extends EmbeddedDatabaseTest implements HarvestDTOBuilderFactory {

    @Resource
    private GameDiaryTodoFeature gameDiaryTodoFeature;

    @Resource
    private GameDiaryFeature gameDiaryFeature;

    @Test
    public void testHarvestWhichRequiresReportNotListedToContactPerson() {
        withPerson(contactPerson -> withPerson(author -> {
            final HarvestPermit permit = model().newHarvestPermit(contactPerson);
            final GameSpecies speciesRequiredByPermit = model().newGameSpecies();
            model().newHarvestReportFields(speciesRequiredByPermit, true);

            final Harvest harvest = model().newHarvest(permit);
            harvest.setHarvestReportRequired(true);
            harvest.setSpecies(speciesRequiredByPermit);
            harvest.setAuthor(author);
            harvest.setActualShooter(author);

            onSavedAndAuthenticated(createUser(contactPerson), () -> {
                final GameDiaryTodoHarvestDTO required = gameDiaryTodoFeature.listAllHarvestsRequiringAction(null);
                assertEquals(0, required.getRejectedFromPermit().size());
                assertEquals(0, required.getPendingApprovalToPermit().size());
                assertEquals(0, required.getReportRequired().size());
            });
        }));
    }

    @Test
    public void testHarvestWhichRequiresReportIsListedToAuthor() {
        withPerson(author -> withPerson(contactPerson -> {
            final HarvestPermit permit = model().newHarvestPermit(contactPerson);
            final GameSpecies speciesRequiredByPermit = model().newGameSpecies();
            model().newHarvestReportFields(speciesRequiredByPermit, true);

            final Harvest harvest = model().newHarvest(permit);
            harvest.setHarvestReportRequired(true);
            harvest.setSpecies(speciesRequiredByPermit);
            harvest.setAuthor(author);
            harvest.setActualShooter(author);

            onSavedAndAuthenticated(createUser(author), () -> {
                final GameDiaryTodoHarvestDTO required = gameDiaryTodoFeature.listAllHarvestsRequiringAction(null);
                assertEquals(0, required.getRejectedFromPermit().size());
                assertEquals(0, required.getPendingApprovalToPermit().size());
                assertEquals(1, required.getReportRequired().size());
            });
        }));
    }

    @Test
    public void testListAllHarvestsRequiringHarvestReportFromActiveUser() {
        withRhy(rhy -> withPerson(shooter -> {
            model().newHarvestSeason(new LocalDate(2014, 9, 1));

            final GameSpecies speciesRequiredByPermit = model().newGameSpecies();
            model().newHarvestReportFields(speciesRequiredByPermit, true);

            final GameSpecies species = model().newGameSpecies();

            persistAndAuthenticateWithNewUser(true);

            final HarvestDTO dto = create(speciesRequiredByPermit, 1).build();
            final PersonWithHunterNumberDTO shooterDto = PersonWithHunterNumberDTO.create(shooter);
            dto.setActorInfo(shooterDto);
            final HarvestDTO requiredBySeason = gameDiaryFeature.createHarvest(dto);
            final HarvestDTO requiredByPermit = gameDiaryFeature.createHarvest(create(speciesRequiredByPermit, 1).build());
            gameDiaryFeature.createHarvest(create(species, 1).build());

            final List<HarvestDTO> required = gameDiaryTodoFeature.listAllHarvestsRequiringAction(null).getReportRequired();
            assertEquals(2, required.size());
            assertThat(
                    F.getNonNullIds(required), containsInAnyOrder(requiredByPermit.getId(), requiredBySeason.getId()));
        }));
    }

}
