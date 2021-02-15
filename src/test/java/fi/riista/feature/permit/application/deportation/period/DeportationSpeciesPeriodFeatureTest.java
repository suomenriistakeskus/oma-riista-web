package fi.riista.feature.permit.application.deportation.period;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountRepository;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.test.EmbeddedDatabaseTest;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;

import java.util.List;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WOLVERINE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class DeportationSpeciesPeriodFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private DeportationSpeciesPeriodFeature feature;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    private Person applicant;
    private Riistanhoitoyhdistys rhy;
    private HarvestPermitApplication application;
    private SystemUser user;

    @Before
    public void setup() {
        applicant = model().newPerson();
        rhy = model().newRiistanhoitoyhdistys();
        application = model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.DEPORTATION);
        model().newDeportationPermitApplication(application);
        application.setStatus(HarvestPermitApplication.Status.DRAFT);
        application.setPermitHolder(PermitHolder.createHolderForPerson(applicant));
        application.setContactPerson(applicant);
        user = createNewUser("applicant", applicant);
        persistInNewTransaction();
    }

    @Test(expected = AccessDeniedException.class)
    public void test_unauthorized() {
        onSavedAndAuthenticated(createNewUser(), () -> {
            feature.getSpeciesPeriod(application.getId());
        });
    }

    @Test
    public void testGetSpeciesPeriods() {
        final GameSpecies wolverineSpecies = model().newGameSpecies(OFFICIAL_CODE_WOLVERINE);
        final HarvestPermitApplicationSpeciesAmount spa =
                model().newHarvestPermitApplicationSpeciesAmount(application, wolverineSpecies);
        final LocalDate beginDate = new LocalDate(2020, 11, 21);
        final LocalDate endDate = new LocalDate(2020, 11, 30);
        spa.setBeginDate(beginDate);
        spa.setEndDate(endDate);
        spa.setAdditionalPeriodInfo("Additional information");

        onSavedAndAuthenticated(user, () -> {
            final DeportationSpeciesPeriodDTO deportationSpeciesPeriodDTO =
                    feature.getSpeciesPeriod(application.getId());
            assertThat(deportationSpeciesPeriodDTO, is(notNullValue()));

            assertThat(deportationSpeciesPeriodDTO.getGameSpeciesCode(), is(equalTo(OFFICIAL_CODE_WOLVERINE)));
            assertThat(deportationSpeciesPeriodDTO.getBeginDate(), is(equalTo(beginDate)));
            assertThat(deportationSpeciesPeriodDTO.getEndDate(), is(equalTo(endDate)));
            assertThat(deportationSpeciesPeriodDTO.getAdditionalPeriodInfo(), is(equalTo(spa.getAdditionalPeriodInfo())));
            assertThat(deportationSpeciesPeriodDTO.getMaxPeriod(), is(equalTo(21)));
        });
    }

    @Test
    public void testSaveSpeciesPeriods() {
        final GameSpecies wolverineSpecies = model().newGameSpecies(OFFICIAL_CODE_WOLVERINE);
        model().newHarvestPermitApplicationSpeciesAmount(application, wolverineSpecies);

        onSavedAndAuthenticated(user, () -> {
            final DeportationSpeciesPeriodDTO speciesPeriodDTO = new DeportationSpeciesPeriodDTO();
            speciesPeriodDTO.setBeginDate(new LocalDate(2020, 11, 20));
            speciesPeriodDTO.setEndDate(new LocalDate(2020, 11, 29));
            speciesPeriodDTO.setGameSpeciesCode(OFFICIAL_CODE_WOLVERINE);
            speciesPeriodDTO.setAdditionalPeriodInfo("Update additional information");

            feature.saveSpeciesPeriod(application.getId(), speciesPeriodDTO);

            runInTransaction(() -> {
                final List<HarvestPermitApplicationSpeciesAmount> spaList =
                        harvestPermitApplicationSpeciesAmountRepository.findByHarvestPermitApplication(application);
                assertThat(spaList, hasSize(1));

                final HarvestPermitApplicationSpeciesAmount spa = spaList.get(0);
                assertThat(spa.getGameSpecies().getOfficialCode(), is(equalTo(speciesPeriodDTO.getGameSpeciesCode())));
                assertThat(spa.getBeginDate(), is(equalTo(speciesPeriodDTO.getBeginDate())));
                assertThat(spa.getEndDate(), is(equalTo(speciesPeriodDTO.getEndDate())));
                assertThat(spa.getValidityYears(), is(equalTo(1)));
                assertThat(spa.getAdditionalPeriodInfo(), is(equalTo(speciesPeriodDTO.getAdditionalPeriodInfo())));
            });
        });
    }

    @Test
    public void testSaveSpeciesPeriods_update() {
        final GameSpecies wolverineSpecies = model().newGameSpecies(OFFICIAL_CODE_WOLVERINE);
        final HarvestPermitApplicationSpeciesAmount spa = model().newHarvestPermitApplicationSpeciesAmount(application, wolverineSpecies);
        final LocalDate beginDate = new LocalDate(2020, 11, 21);
        final LocalDate endDate = new LocalDate(2020, 11, 30);
        spa.setBeginDate(beginDate);
        spa.setEndDate(endDate);
        spa.setAdditionalPeriodInfo("Additional information");

        onSavedAndAuthenticated(user, () -> {
            final DeportationSpeciesPeriodDTO updateDto = new DeportationSpeciesPeriodDTO();
            updateDto.setBeginDate(new LocalDate(2020, 11, 20));
            updateDto.setEndDate(new LocalDate(2020, 11, 29));
            updateDto.setGameSpeciesCode(OFFICIAL_CODE_WOLVERINE);
            updateDto.setAdditionalPeriodInfo("Update additional information");

            feature.saveSpeciesPeriod(application.getId(), updateDto);

            runInTransaction(() -> {
                final List<HarvestPermitApplicationSpeciesAmount> spaList =
                        harvestPermitApplicationSpeciesAmountRepository.findByHarvestPermitApplication(application);
                assertThat(spaList, hasSize(1));

                final HarvestPermitApplicationSpeciesAmount updatedSpa = spaList.get(0);
                assertThat(updatedSpa.getGameSpecies().getOfficialCode(), is(equalTo(OFFICIAL_CODE_WOLVERINE)));
                assertThat(updatedSpa.getBeginDate(), is(equalTo(updateDto.getBeginDate())));
                assertThat(updatedSpa.getEndDate(), is(equalTo(updateDto.getEndDate())));
                assertThat(updatedSpa.getValidityYears(), is(equalTo(1)));
                assertThat(updatedSpa.getAdditionalPeriodInfo(), is(equalTo(updateDto.getAdditionalPeriodInfo())));
            });
        });
    }
}
