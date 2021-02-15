package fi.riista.feature.permit.application.mammal.period;

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

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WOLVERINE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MammalPermitApplicationSpeciesPeriodFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private MammalPermitApplicationSpeciesPeriodFeature feature;

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
        application = model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.MAMMAL);
        model().newMammalPermitApplication(application);
        application.setStatus(HarvestPermitApplication.Status.DRAFT);
        application.setPermitHolder(PermitHolder.createHolderForPerson(applicant));
        application.setContactPerson(applicant);
        user = createNewUser("applicant", applicant);
        persistInNewTransaction();
    }


    @Test(expected = AccessDeniedException.class)
    public void test_unauthorized() {

        onSavedAndAuthenticated(createNewUser(), () -> {
            feature.getPermitPeriodInformation(application.getId());
        });
    }

    @Test
    public void testSmoke_getPermitPeriod_initialValues() {
        final GameSpecies wolverineSpecies = model().newGameSpecies(OFFICIAL_CODE_WOLVERINE);
        model().newHarvestPermitApplicationSpeciesAmount(application, wolverineSpecies);

        onSavedAndAuthenticated(user, () -> {

            final MammalPermitApplicationSpeciesPeriodInformationDTO permitPeriodInformation =
                    feature.getPermitPeriodInformation(application.getId());

            assertTrue(permitPeriodInformation.isExtendedPeriodNotApplicable());
            assertNull(permitPeriodInformation.getValidityYears());
            assertThat(permitPeriodInformation.getSpeciesPeriods(), hasSize(1));
            final MammalPermitApplicationSpeciesPeriodDTO speciesPeriodDTO =
                    permitPeriodInformation.getSpeciesPeriods().get(0);
            assertEquals(OFFICIAL_CODE_WOLVERINE, speciesPeriodDTO.getGameSpeciesCode());
            assertNull(speciesPeriodDTO.getBeginDate());
            assertNull(speciesPeriodDTO.getEndDate());
            assertEquals(Integer.valueOf(21), speciesPeriodDTO.getMaxPeriod());
        });
    }


    @Test
    public void testSmoke_getPermitPeriod() {
        final GameSpecies wolverineSpecies = model().newGameSpecies(OFFICIAL_CODE_WOLVERINE);
        final HarvestPermitApplicationSpeciesAmount spa =
                model().newHarvestPermitApplicationSpeciesAmount(application, wolverineSpecies);
        final LocalDate beginDate = new LocalDate(2019, 9, 1);
        final LocalDate endDate = new LocalDate(2019, 9, 10);
        spa.setBeginDate(beginDate);
        spa.setEndDate(endDate);

        onSavedAndAuthenticated(user, () -> {

            final MammalPermitApplicationSpeciesPeriodInformationDTO permitPeriodInformation =
                    feature.getPermitPeriodInformation(application.getId());

            assertTrue(permitPeriodInformation.isExtendedPeriodNotApplicable());
            assertNull(permitPeriodInformation.getValidityYears());
            assertThat(permitPeriodInformation.getSpeciesPeriods(), hasSize(1));
            final MammalPermitApplicationSpeciesPeriodDTO speciesPeriodDTO =
                    permitPeriodInformation.getSpeciesPeriods().get(0);
            assertEquals(OFFICIAL_CODE_WOLVERINE, speciesPeriodDTO.getGameSpeciesCode());
            assertEquals(beginDate, speciesPeriodDTO.getBeginDate());
            assertEquals(endDate, speciesPeriodDTO.getEndDate());
            assertEquals(Integer.valueOf(21), speciesPeriodDTO.getMaxPeriod());
        });
    }

    @Test
    public void testSmoke_updatePermitPeriod_initialValues() {
        final GameSpecies wolverineSpecies = model().newGameSpecies(OFFICIAL_CODE_WOLVERINE);
        model().newHarvestPermitApplicationSpeciesAmount(application, wolverineSpecies);

        onSavedAndAuthenticated(user, () -> {
            final MammalPermitApplicationSpeciesPeriodDTO wolverineDTO = new MammalPermitApplicationSpeciesPeriodDTO();
            final LocalDate beginDate = new LocalDate(2019, 9, 1);
            final LocalDate endDate = new LocalDate(2019, 9, 10);
            wolverineDTO.setBeginDate(beginDate);
            wolverineDTO.setEndDate(endDate);
            wolverineDTO.setGameSpeciesCode(OFFICIAL_CODE_WOLVERINE);
            wolverineDTO.setAdditionalPeriodInfo("Additional information");

            final MammalPermitApplicationSpeciesPeriodInformationDTO dto =
                    new MammalPermitApplicationSpeciesPeriodInformationDTO(
                            ImmutableList.of(wolverineDTO),
                            1,
                            null,
                            null,
                            null);

            feature.saveSpeciesPeriods(application.getId(), dto);
        });
    }

}
