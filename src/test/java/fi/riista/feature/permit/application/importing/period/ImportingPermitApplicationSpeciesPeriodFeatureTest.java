package fi.riista.feature.permit.application.importing.period;

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

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_CANADIAN_BEAVER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_EUROPEAN_POLECAT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ImportingPermitApplicationSpeciesPeriodFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private ImportingPermitApplicationSpeciesPeriodFeature feature;

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
        application = model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.IMPORTING);
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
    public void testGetPermitPeriod() {
        final GameSpecies beaverSpecies = model().newGameSpecies(OFFICIAL_CODE_CANADIAN_BEAVER);
        final HarvestPermitApplicationSpeciesAmount speciesAmount =
                model().newHarvestPermitApplicationSpeciesAmount(application, beaverSpecies, 5.0f, 3);

        onSavedAndAuthenticated(user, () -> {

            final ImportingPermitApplicationSpeciesPeriodInformationDTO permitPeriodInformation =
                    feature.getPermitPeriodInformation(application.getId());

            assertThat(permitPeriodInformation.getValidityYears(), equalTo(3));
            assertThat(permitPeriodInformation.getSpeciesPeriods(), hasSize(1));

            final ImportingPermitApplicationSpeciesPeriodDTO speciesPeriodDTO =
                    permitPeriodInformation.getSpeciesPeriods().get(0);
            assertThat(speciesPeriodDTO.getGameSpeciesCode(), equalTo(OFFICIAL_CODE_CANADIAN_BEAVER));
            assertThat(speciesPeriodDTO.getBeginDate(), equalTo(speciesAmount.getBeginDate()));
            assertThat(speciesPeriodDTO.getEndDate(), equalTo(speciesAmount.getEndDate()));
        });
    }

    @Test
    public void testUpdatePermitPeriod() {
        final GameSpecies polecatSpecies = model().newGameSpecies(OFFICIAL_CODE_EUROPEAN_POLECAT);
        model().newHarvestPermitApplicationSpeciesAmount(application, polecatSpecies);

        final ImportingPermitApplicationSpeciesPeriodDTO polecatDTO = createDto(polecatSpecies.getOfficialCode());

        onSavedAndAuthenticated(user, () -> {

            final ImportingPermitApplicationSpeciesPeriodInformationDTO dto =
                    new ImportingPermitApplicationSpeciesPeriodInformationDTO(
                            ImmutableList.of(polecatDTO), 4);

            feature.saveSpeciesPeriods(application.getId(), dto);
        });

        runInTransaction(()->{
            final List<HarvestPermitApplicationSpeciesAmount> all =
                    harvestPermitApplicationSpeciesAmountRepository.findAll();
            assertThat(all, hasSize(1));
            final HarvestPermitApplicationSpeciesAmount speciesAmount = all.get(0);
            assertThat(speciesAmount.getGameSpecies(), equalTo(polecatSpecies));
            assertThat(speciesAmount.getBeginDate(), equalTo(polecatDTO.getBeginDate()));
            assertThat(speciesAmount.getEndDate(), equalTo(polecatDTO.getEndDate()));
            assertThat(speciesAmount.getAdditionalPeriodInfo(), equalTo(polecatDTO.getAdditionalPeriodInfo()));
            assertThat(speciesAmount.getValidityYears(), equalTo(4));
        });
    }

    ImportingPermitApplicationSpeciesPeriodDTO createDto(final int speciesCode) {
        final ImportingPermitApplicationSpeciesPeriodDTO polecatDTO = new ImportingPermitApplicationSpeciesPeriodDTO();
        final LocalDate beginDate = new LocalDate(2020, 9, 1);
        final LocalDate endDate = new LocalDate(2020, 9, 10);
        polecatDTO.setBeginDate(beginDate);
        polecatDTO.setEndDate(endDate);
        polecatDTO.setGameSpeciesCode(speciesCode);
        polecatDTO.setAdditionalPeriodInfo("Additional information");
        return polecatDTO;
    }

}
