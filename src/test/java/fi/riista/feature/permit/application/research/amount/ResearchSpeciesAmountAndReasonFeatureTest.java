package fi.riista.feature.permit.application.research.amount;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountRepository;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.feature.permit.application.derogation.reasons.DerogationPermitApplicationReason;
import fi.riista.feature.permit.application.derogation.reasons.DerogationPermitApplicationReasonRepository;
import fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_LYNX;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_PARTRIDGE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_RABBIT;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WOLVERINE;
import static fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType.REASON_RESEARCH;
import static fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType.REASON_RESEARCH_41A;
import static fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType.REASON_RESEARCH_41C;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class ResearchSpeciesAmountAndReasonFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private ResearchSpeciesAmountAndReasonFeature feature;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    @Resource
    private DerogationPermitApplicationReasonRepository derogationPermitApplicationReasonRepository;

    private Person applicant;
    private Riistanhoitoyhdistys rhy;
    private HarvestPermitApplication application;
    private GameSpecies species;

    @Before
    public void setup() {
        applicant = model().newPerson();
        rhy = model().newRiistanhoitoyhdistys();
        application = model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.RESEARCH);
        model().newResearchPermitApplication(application);
        application.setStatus(HarvestPermitApplication.Status.DRAFT);
        application.setPermitHolder(PermitHolder.createHolderForPerson(applicant));
        application.setContactPerson(applicant);
        species = model().newGameSpecies(OFFICIAL_CODE_WOLVERINE);
    }

    @Test(expected = AccessDeniedException.class)
    public void test_unauthorized() {
        onSavedAndAuthenticated(createNewUser(), () -> {
            feature.getSpeciesAmounts(application.getId());
        });
    }

    @Test
    public void testGetSpeciesAmounts() {
        final HarvestPermitApplicationSpeciesAmount speciesAmount =
                model().newHarvestPermitApplicationSpeciesAmount(application, species);

        onSavedAndAuthenticated(createNewUser("applicant", applicant), () -> {
            final List<ResearchSpeciesAmountDTO> speciesAmountsList = feature.getSpeciesAmounts(application.getId());

            assertThat(speciesAmountsList, hasSize(1));

            final ResearchSpeciesAmountDTO speciesAmountDto = speciesAmountsList.get(0);
            assertThat(speciesAmountDto.getGameSpeciesCode(), is(equalTo(OFFICIAL_CODE_WOLVERINE)));
            assertThat((double) speciesAmountDto.getAmount(), is(closeTo(speciesAmount.getSpecimenAmount(), 0.01)));
        });
    }

    @Test
    public void testSaveSpeciesAmountsAndDerogationReasons() {
        onSavedAndAuthenticated(createNewUser("applicant", applicant), () -> {
            final ResearchSpeciesAmountDTO speciesAmountDTO = new ResearchSpeciesAmountDTO();
            speciesAmountDTO.setAmount(5);
            speciesAmountDTO.setGameSpeciesCode(OFFICIAL_CODE_WOLVERINE);
            feature.saveSpeciesAmountsAndDerogationReasons(application.getId(), ImmutableList.of(speciesAmountDTO));
        });

        runInTransaction(() -> {
            final List<HarvestPermitApplicationSpeciesAmount> speciesAmountList = harvestPermitApplicationSpeciesAmountRepository.findAll();
            assertThat(speciesAmountList, hasSize(1));

            final HarvestPermitApplicationSpeciesAmount spa = speciesAmountList.get(0);
            assertThat(spa.getGameSpecies().getOfficialCode(), is(equalTo(OFFICIAL_CODE_WOLVERINE)));
            assertThat((double)spa.getSpecimenAmount(), is(closeTo(5.0, 0.01)));

            final List<DerogationPermitApplicationReason> reasons =
                    derogationPermitApplicationReasonRepository.findByHarvestPermitApplication(application);
            assertThat(reasons, hasSize(1));
            final DerogationPermitApplicationReason reason = reasons.get(0);
            assertThat(reason.getReasonType(), is(equalTo(REASON_RESEARCH_41A)));

        });
    }

    @Test
    public void testSaveSpeciesAmountsAndDerogationReasons_update() {
        final GameSpecies firstSpecies = model().newGameSpecies(OFFICIAL_CODE_LYNX);
        final GameSpecies secondSpecies = model().newGameSpecies(OFFICIAL_CODE_PARTRIDGE);
        final GameSpecies thirdSpecies = model().newGameSpecies(OFFICIAL_CODE_RABBIT);

        onSavedAndAuthenticated(createNewUser("applicant", applicant), () -> {
            final ResearchSpeciesAmountDTO firstDTO = new ResearchSpeciesAmountDTO();
            firstDTO.setAmount(5);
            firstDTO.setGameSpeciesCode(firstSpecies.getOfficialCode());

            final ResearchSpeciesAmountDTO secondDTO = new ResearchSpeciesAmountDTO();
            secondDTO.setAmount(6);
            secondDTO.setGameSpeciesCode(secondSpecies.getOfficialCode());

            final ResearchSpeciesAmountDTO thirdDTO = new ResearchSpeciesAmountDTO();
            thirdDTO.setAmount(7);
            thirdDTO.setGameSpeciesCode(thirdSpecies.getOfficialCode());
            feature.saveSpeciesAmountsAndDerogationReasons(application.getId(), ImmutableList.of(firstDTO, secondDTO, thirdDTO));
        });

        runInTransaction(() -> {
            final List<HarvestPermitApplicationSpeciesAmount> all = harvestPermitApplicationSpeciesAmountRepository.findAll();
            assertThat(all, hasSize(3));

            final HarvestPermitApplicationSpeciesAmount spa1 = all.get(0);
            assertThat((double) spa1.getSpecimenAmount(), is(closeTo(5.0, 0.01)));

            final HarvestPermitApplicationSpeciesAmount spa2 = all.get(1);
            assertThat((double) spa2.getSpecimenAmount(), is(closeTo(6.0, 0.01)));

            final HarvestPermitApplicationSpeciesAmount spa3 = all.get(2);
            assertThat((double) spa3.getSpecimenAmount(), is(closeTo(7.0, 0.01)));

            final ImmutableSet<Integer> speciesCodes = ImmutableSet.of(
                    spa1.getGameSpecies().getOfficialCode(),
                    spa2.getGameSpecies().getOfficialCode(),
                    spa3.getGameSpecies().getOfficialCode());

            assertThat(speciesCodes, containsInAnyOrder(OFFICIAL_CODE_LYNX, OFFICIAL_CODE_PARTRIDGE, OFFICIAL_CODE_RABBIT));

            final List<DerogationPermitApplicationReason> reasons =
                    derogationPermitApplicationReasonRepository.findByHarvestPermitApplication(application);
            assertThat(reasons, hasSize(3));
            final List<PermitDecisionDerogationReasonType> reasonTypes =
                    reasons.stream().map(DerogationPermitApplicationReason::getReasonType).collect(Collectors.toList());
            assertThat(reasonTypes, containsInAnyOrder(REASON_RESEARCH_41A, REASON_RESEARCH, REASON_RESEARCH_41C));
        });
    }
}
