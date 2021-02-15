package fi.riista.feature.permit.application.mooselike;

import com.google.common.collect.ImmutableList;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountRepository;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_ROE_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER;
import static fi.riista.util.Collect.toMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class MooselikePermitApplicationSpeciesFeatureTest extends EmbeddedDatabaseTest {

    private HarvestPermitApplication application;

    @Resource
    private MooselikePermitApplicationSpeciesFeature feature;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository repository;
    private Riistanhoitoyhdistys rhy;
    private HarvestPermitArea area;
    private GameSpecies mooseSpecies;
    private GameSpecies roeDeerSpecies;
    private GameSpecies whiteTailedDeerSpecies;

    @Before
    public void setup() {
        rhy = model().newRiistanhoitoyhdistys();
        area = model().newHarvestPermitArea();
        mooseSpecies = model().newGameSpeciesMoose();
        roeDeerSpecies = model().newGameSpecies(OFFICIAL_CODE_ROE_DEER);
        whiteTailedDeerSpecies = model().newGameSpecies(OFFICIAL_CODE_WHITE_TAILED_DEER);
        application = model().newHarvestPermitApplication(rhy, area, HarvestPermitCategory.MOOSELIKE);
        application.setStatus(HarvestPermitApplication.Status.DRAFT);
    }

    @Test
    public void testSmoke_getAmounts() {
        model().newHarvestPermitApplicationSpeciesAmount(application, mooseSpecies, 5.0f);

        onSavedAndAuthenticated(createNewUser("applicant", application.getContactPerson()), () -> {
            final List<MooselikePermitApplicationSpeciesAmountDTO> speciesAmounts =
                    feature.getSpeciesAmounts(application.getId());
            assertEquals(1, speciesAmounts.size());
            final MooselikePermitApplicationSpeciesAmountDTO amountDTO = speciesAmounts.get(0);
            assertEquals(mooseSpecies.getOfficialCode(), amountDTO.getGameSpeciesCode());
            assertEquals(5.0, amountDTO.getAmount(), 0.01);
        });
    }

    @Test
    public void testSaveAmounts() {

        final MooselikePermitApplicationSpeciesAmountDTO amountDTO =
                new MooselikePermitApplicationSpeciesAmountDTO();
        amountDTO.setAmount(4.0f);
        amountDTO.setGameSpeciesCode(mooseSpecies.getOfficialCode());

        onSavedAndAuthenticated(createNewUser("applicant", application.getContactPerson()), () -> {
            feature.saveSpeciesAmounts(application.getId(), ImmutableList.of(amountDTO));
        });

        runInTransaction(() -> {
            final List<HarvestPermitApplicationSpeciesAmount> speciesAmounts =
                    repository.findByHarvestPermitApplication(application);
            assertEquals(1, speciesAmounts.size());
            final HarvestPermitApplicationSpeciesAmount amount = speciesAmounts.get(0);
            assertEquals(mooseSpecies, amount.getGameSpecies());
            assertEquals(4.0f, amount.getSpecimenAmount(), 0.01);
        });
    }

    @Test
    public void testSaveAmounts_multipleSpecies() {
        final MooselikePermitApplicationSpeciesAmountDTO mooseAmount =
                new MooselikePermitApplicationSpeciesAmountDTO();
        mooseAmount.setAmount(4.0f);
        mooseAmount.setGameSpeciesCode(mooseSpecies.getOfficialCode());

        final MooselikePermitApplicationSpeciesAmountDTO roeDeerAmount =
                new MooselikePermitApplicationSpeciesAmountDTO();
        roeDeerAmount.setAmount(5.0f);
        roeDeerAmount.setGameSpeciesCode(roeDeerSpecies.getOfficialCode());

        final MooselikePermitApplicationSpeciesAmountDTO whiteTailedDeerAmount =
                new MooselikePermitApplicationSpeciesAmountDTO();
        whiteTailedDeerAmount.setAmount(6.0f);
        whiteTailedDeerAmount.setGameSpeciesCode(whiteTailedDeerSpecies.getOfficialCode());

        onSavedAndAuthenticated(createNewUser("applicant", application.getContactPerson()), () -> {
            feature.saveSpeciesAmounts(
                    application.getId(),
                    ImmutableList.of(mooseAmount, roeDeerAmount, whiteTailedDeerAmount));
        });

        runInTransaction(() -> {

            final Map<GameSpecies, Float> resultMap =
                    repository.findByHarvestPermitApplication(application).stream()
                            .collect(toMap(
                                    HarvestPermitApplicationSpeciesAmount::getGameSpecies,
                                    HarvestPermitApplicationSpeciesAmount::getSpecimenAmount,
                                    HashMap::new));

            assertEquals(3, resultMap.size());
            assertEquals(4.0f, resultMap.get(mooseSpecies), 0.01);
            assertEquals(5.0f, resultMap.get(roeDeerSpecies), 0.01);
            assertEquals(6.0f, resultMap.get(whiteTailedDeerSpecies), 0.01);
        });

    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveAmounts_duplicate() {

        final MooselikePermitApplicationSpeciesAmountDTO amountDTO =
                new MooselikePermitApplicationSpeciesAmountDTO();
        amountDTO.setAmount(4.0f);
        amountDTO.setGameSpeciesCode(mooseSpecies.getOfficialCode());

        final MooselikePermitApplicationSpeciesAmountDTO amountDTO2 =
                new MooselikePermitApplicationSpeciesAmountDTO();
        amountDTO2.setAmount(5.0f);
        amountDTO2.setGameSpeciesCode(mooseSpecies.getOfficialCode());

        onSavedAndAuthenticated(createNewUser("applicant", application.getContactPerson()), () -> {

            final ImmutableList<MooselikePermitApplicationSpeciesAmountDTO> list =
                    ImmutableList.of(amountDTO, amountDTO2);
            feature.saveSpeciesAmounts(application.getId(), list);
            fail("Should have thrown an exception");
        });

    }
}
