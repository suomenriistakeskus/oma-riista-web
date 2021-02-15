package fi.riista.feature.permit.application.research.period;

import com.google.common.collect.ImmutableList;
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

public class ResearchSpeciesPeriodFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private ResearchSpeciesPeriodFeature feature;

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
        application = model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.RESEARCH);
        model().newResearchPermitApplication(application);
        application.setStatus(HarvestPermitApplication.Status.DRAFT);
        application.setPermitHolder(PermitHolder.createHolderForPerson(applicant));
        application.setContactPerson(applicant);
        user = createNewUser("applicant", applicant);
        persistInNewTransaction();
    }

    @Test(expected = AccessDeniedException.class)
    public void test_unauthorized() {
        onSavedAndAuthenticated(createNewUser(), () -> {
            feature.getSpeciesPeriods(application.getId());
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
        spa.setValidityYears(2);
        spa.setAdditionalPeriodInfo("Additional information");

        onSavedAndAuthenticated(user, () -> {
            final ResearchSpeciesPeriodInformationDTO permitPeriodInformation =
                    feature.getSpeciesPeriods(application.getId());

            assertThat(permitPeriodInformation.getValidityYears(), is(equalTo(2)));

            final List<ResearchSpeciesPeriodDTO> speciesPeriodList =
                    permitPeriodInformation.getSpeciesPeriods();
            ;
            assertThat(permitPeriodInformation.getSpeciesPeriods(), hasSize(1));

            final ResearchSpeciesPeriodDTO speciesPeriod = speciesPeriodList.get(0);
            assertThat(speciesPeriod.getGameSpeciesCode(), is(equalTo(OFFICIAL_CODE_WOLVERINE)));
            assertThat(speciesPeriod.getBeginDate(), is(equalTo(beginDate)));
            assertThat(speciesPeriod.getEndDate(), is(equalTo(endDate)));
            assertThat(speciesPeriod.getAdditionalPeriodInfo(), is(equalTo(spa.getAdditionalPeriodInfo())));
        });
    }

    @Test
    public void testSaveSpeciesPeriods() {
        final GameSpecies wolverineSpecies = model().newGameSpecies(OFFICIAL_CODE_WOLVERINE);
        model().newHarvestPermitApplicationSpeciesAmount(application, wolverineSpecies);

        onSavedAndAuthenticated(user, () -> {
            final ResearchSpeciesPeriodDTO speciesPeriodDto = new ResearchSpeciesPeriodDTO();
            speciesPeriodDto.setBeginDate(new LocalDate(2020, 11, 20));
            speciesPeriodDto.setEndDate(new LocalDate(2020, 11, 29));
            speciesPeriodDto.setGameSpeciesCode(OFFICIAL_CODE_WOLVERINE);
            speciesPeriodDto.setAdditionalPeriodInfo("Update additional information");

            final ResearchSpeciesPeriodInformationDTO dto =
                    new ResearchSpeciesPeriodInformationDTO(ImmutableList.of(speciesPeriodDto), 1);

            feature.saveSpeciesPeriods(application.getId(), dto);

            runInTransaction(() -> {
                final List<HarvestPermitApplicationSpeciesAmount> spaList =
                        harvestPermitApplicationSpeciesAmountRepository.findByHarvestPermitApplication(application);
                assertThat(spaList, hasSize(1));

                final HarvestPermitApplicationSpeciesAmount spa = spaList.get(0);
                assertThat(spa.getGameSpecies().getOfficialCode(), is(equalTo(speciesPeriodDto.getGameSpeciesCode())));
                assertThat(spa.getBeginDate(), is(equalTo(speciesPeriodDto.getBeginDate())));
                assertThat(spa.getEndDate(), is(equalTo(speciesPeriodDto.getEndDate())));
                assertThat(spa.getValidityYears(), is(equalTo(1)));
                assertThat(spa.getAdditionalPeriodInfo(), is(equalTo(speciesPeriodDto.getAdditionalPeriodInfo())));
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
        spa.setValidityYears(2);
        spa.setAdditionalPeriodInfo("Additional information");

        onSavedAndAuthenticated(user, () -> {
            final ResearchSpeciesPeriodDTO updateDto = new ResearchSpeciesPeriodDTO();
            updateDto.setBeginDate(new LocalDate(2020, 11, 20));
            updateDto.setEndDate(new LocalDate(2020, 11, 29));
            updateDto.setGameSpeciesCode(OFFICIAL_CODE_WOLVERINE);
            updateDto.setAdditionalPeriodInfo("Update additional information");

            final ResearchSpeciesPeriodInformationDTO dto =
                    new ResearchSpeciesPeriodInformationDTO(ImmutableList.of(updateDto), 1);

            feature.saveSpeciesPeriods(application.getId(), dto);

            runInTransaction(() -> {
                final List<HarvestPermitApplicationSpeciesAmount> spaList =
                        harvestPermitApplicationSpeciesAmountRepository.findByHarvestPermitApplication(application);
                assertThat(spaList, hasSize(1));

                final HarvestPermitApplicationSpeciesAmount updatedSpa = spaList.get(0);
                assertThat(updatedSpa.getGameSpecies().getOfficialCode(), is(equalTo(OFFICIAL_CODE_WOLVERINE)));
                assertThat(updatedSpa.getBeginDate(), is(equalTo(updateDto.getBeginDate())));
                assertThat(updatedSpa.getEndDate(), is(equalTo(updateDto.getEndDate())));
                assertThat(updatedSpa.getAdditionalPeriodInfo(), is(equalTo(updateDto.getAdditionalPeriodInfo())));
                assertThat(updatedSpa.getValidityYears(), is(equalTo(1)));
            });
        });
    }
}
