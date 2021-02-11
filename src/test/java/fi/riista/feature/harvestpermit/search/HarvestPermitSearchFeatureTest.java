package fi.riista.feature.harvestpermit.search;

import com.google.common.collect.ImmutableList;
import fi.riista.feature.common.decision.GrantStatus;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitNotFoundException;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.F;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static fi.riista.feature.common.decision.GrantStatus.REJECTED;
import static fi.riista.feature.common.decision.GrantStatus.RESTRICTED;
import static fi.riista.feature.common.decision.GrantStatus.UNCHANGED;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_OTTER;
import static fi.riista.feature.harvestpermit.search.HarvestPermitDecisionOrigin.LUPAHALLINTA;
import static fi.riista.feature.harvestpermit.search.HarvestPermitDecisionOrigin.OMA_RIISTA;
import static fi.riista.test.Asserts.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;

public class HarvestPermitSearchFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private HarvestPermitSearchFeature harvestPermitSearchFeature;
    private GameSpecies otterSpecies;

    @Before
    public void setup() {
        otterSpecies = model().newGameSpecies(OFFICIAL_CODE_OTTER);
    }

    @Test
    public void testListPermitTypes_empty() {
        final List<HarvestPermitTypeDTO> dtos = harvestPermitSearchFeature.listPermitTypes();
        assertThat(dtos, is(empty()));
    }

    @Test
    public void testListPermitTypes_Lupahallinta() {
        final HarvestPermit permit = createHarvestPermitWithSpeciesAmount("123");
        persistInNewTransaction();

        final List<HarvestPermitTypeDTO> dtos = harvestPermitSearchFeature.listPermitTypes();
        assertThat(dtos, hasSize(1));
        final HarvestPermitTypeDTO dto = dtos.get(0);
        assertThat(dto.getOrigin(), equalTo(LUPAHALLINTA));
        assertThat(dto.getPermitTypeCode(), equalTo("123"));
        assertThat(dto.getPermitType(), equalTo(permit.getPermitType()));
    }

    @Test
    public void testListPermitTypes_Omariista() {
        final HarvestPermit permit =
                createHarvestPermitWithSpeciesAmountAndDecision("456");
        persistInNewTransaction();

        final List<HarvestPermitTypeDTO> dtos = harvestPermitSearchFeature.listPermitTypes();
        assertThat(dtos, hasSize(1));

        final HarvestPermitTypeDTO dto = dtos.get(0);
        assertThat(dto.getOrigin(), equalTo(OMA_RIISTA));
        assertThat(dto.getPermitTypeCode(), equalTo("456"));
        assertThat(dto.getPermitType(), equalTo(permit.getPermitType()));
    }

    @Test
    public void testListPermitTypes_Omariista_noSpeciesPermit() {
        final HarvestPermit permit =
                createHarvestPermitAndDecisionWithoutSpeciesAmount(PermitTypeCode.WEAPON_TRANSPORTATION_BASED);
        persistInNewTransaction();

        final List<HarvestPermitTypeDTO> dtos = harvestPermitSearchFeature.listPermitTypes();
        assertThat(dtos, hasSize(1));

        final HarvestPermitTypeDTO dto = dtos.get(0);
        assertThat(dto.getOrigin(), equalTo(OMA_RIISTA));
        assertThat(dto.getPermitTypeCode(), equalTo(PermitTypeCode.WEAPON_TRANSPORTATION_BASED));
        assertThat(dto.getPermitType(), equalTo(permit.getPermitType()));
    }

    @Test
    public void testListPermitTypes_Lupahallinta_onlyOneType() {
        createHarvestPermitWithSpeciesAmount("123");
        createHarvestPermitWithSpeciesAmount("123");
        persistInNewTransaction();

        final List<HarvestPermitTypeDTO> dtos = harvestPermitSearchFeature.listPermitTypes();
        assertThat(dtos, hasSize(1));
        final HarvestPermitTypeDTO dto = dtos.get(0);
        assertThat(dto.getOrigin(), equalTo(LUPAHALLINTA));
        assertThat(dto.getPermitTypeCode(), equalTo("123"));
    }

    @Test
    public void testListPermitTypes_Omariista_onlyOneType() {
        createHarvestPermitWithSpeciesAmountAndDecision("456");
        createHarvestPermitWithSpeciesAmountAndDecision("456");
        persistInNewTransaction();

        final List<HarvestPermitTypeDTO> dtos = harvestPermitSearchFeature.listPermitTypes();
        assertThat(dtos, hasSize(1));
        final HarvestPermitTypeDTO dto = dtos.get(0);
        assertThat(dto.getOrigin(), equalTo(OMA_RIISTA));
        assertThat(dto.getPermitTypeCode(), equalTo("456"));
    }

    @Test
    public void testListPermitTypes_samePermitTypeFromBoth() {
        createHarvestPermitWithSpeciesAmount("456");
        createHarvestPermitWithSpeciesAmountAndDecision("456");
        persistInNewTransaction();

        final List<HarvestPermitTypeDTO> dtos = harvestPermitSearchFeature.listPermitTypes();
        assertThat(dtos, hasSize(2));
        dtos.forEach(dto -> assertThat(dto.getPermitTypeCode(), equalTo("456")));
        final ArrayList<HarvestPermitDecisionOrigin> origins = F.mapNonNullsToList(dtos,
                HarvestPermitTypeDTO::getOrigin);
        assertThat(origins, containsInAnyOrder(LUPAHALLINTA, OMA_RIISTA));
    }


    @Test
    public void testReturnsNotFinishedLupahallintaPermits() {
        final HarvestPermit permit = createHarvestPermitWithSpeciesAmount("300");
        permit.setHarvestReportState(null);

        onSavedAndAuthenticated(createNewModerator(), () -> {
            final HarvestPermitSearchDTO searchDTO = new HarvestPermitSearchDTO();
            searchDTO.setDecisionStatuses(ImmutableList.of(UNCHANGED, RESTRICTED));
            searchDTO.setReportNotDone(true);

            final List<HarvestPermitSearchResultDTO> result = harvestPermitSearchFeature.search(searchDTO);
            assertThat(result, hasSize(1));
            final HarvestPermitSearchResultDTO dto = result.get(0);
            assertThat(dto.getPermitNumber(), equalTo(permit.getPermitNumber()));
        });
    }

    @Test
    public void testReturnsOnlyPermitsWithCorrectGrantStatus() {
        final HarvestPermit unchangedPermit = createUnfinishedHarvestPermitWithGrantStatus(UNCHANGED);
        final HarvestPermit restrictedPermit = createUnfinishedHarvestPermitWithGrantStatus(RESTRICTED);
        createUnfinishedHarvestPermitWithGrantStatus(REJECTED);

        onSavedAndAuthenticated(createNewModerator(), () -> {
            final HarvestPermitSearchDTO searchDTO = new HarvestPermitSearchDTO();
            searchDTO.setDecisionStatuses(ImmutableList.of(UNCHANGED, RESTRICTED));
            searchDTO.setReportNotDone(true);

            final List<HarvestPermitSearchResultDTO> result = harvestPermitSearchFeature.search(searchDTO);
            assertThat(result, hasSize(2));
            final List<String> permitNumbers = F.mapNonNullsToList(result,
                    HarvestPermitSearchResultDTO::getPermitNumber);
            assertThat(permitNumbers, hasSize(2));
            assertThat(permitNumbers,
                    containsInAnyOrder(unchangedPermit.getPermitNumber(), restrictedPermit.getPermitNumber()));
        });
    }


    @Test
    public void testCheckHarvestPermitExists() {
        final HarvestPermit permit = model().newHarvestPermit(createUserWithPerson().getPerson());
        persistInNewTransaction();

        final HarvestPermitExistsDTO dto =
                harvestPermitSearchFeature.checkHarvestPermitExists(permit.getPermitNumber());
        assertThat(dto, is(notNullValue()));
        assertThat(dto.getId(), equalTo(permit.getId()));
    }

    @Test(expected = HarvestPermitNotFoundException.class)
    public void testCheckHarvestPermitExists_forNonExistingPermit() {
        harvestPermitSearchFeature.checkHarvestPermitExists("1234567");
    }

    @Test(expected = HarvestPermitNotFoundException.class)
    public void testCheckHarvestPermitExists_forMooselikePermit() {
        withRhy(rhy -> withPerson(person -> {

            final HarvestPermit moosePermit = model().newMooselikePermit(rhy);
            model().newHarvestPermitContactPerson(moosePermit, person);

            persistInNewTransaction();

            harvestPermitSearchFeature.checkHarvestPermitExists(moosePermit.getPermitNumber());
        }));
    }

    @Test(expected = HarvestPermitNotFoundException.class)
    public void testCheckHarvestPermitExists_forAmendmentPermit() {
        withPerson(person -> {

            final HarvestPermit originalPermit = model().newHarvestPermit(person);
            final HarvestPermit amendmentPermit = model().newHarvestPermit(originalPermit, person);

            persistInNewTransaction();

            harvestPermitSearchFeature.checkHarvestPermitExists(amendmentPermit.getPermitNumber());
        });
    }

    private HarvestPermit createUnfinishedHarvestPermitWithGrantStatus(final GrantStatus unchanged) {
        final HarvestPermit harvestPermit = createHarvestPermitWithSpeciesAmountAndDecision("300");
        harvestPermit.getPermitDecision().setGrantStatus(unchanged);
        harvestPermit.setHarvestReportState(null);
        return harvestPermit;
    }

    private HarvestPermit createHarvestPermitWithSpeciesAmountAndDecision(final String permitTypeCode) {
        final PermitDecision decision = model().newPermitDecision(model().newRiistanhoitoyhdistys());
        final HarvestPermit permit = createHarvestPermitWithSpeciesAmount(permitTypeCode);
        permit.setPermitDecision(decision);
        return permit;
    }

    private HarvestPermit createHarvestPermitWithSpeciesAmount(final String permitTypeCode) {
        final HarvestPermit permit = model().newHarvestPermit();
        permit.setPermitTypeCode(permitTypeCode);
        model().newHarvestPermitSpeciesAmount(permit, otterSpecies);
        return permit;
    }

    private HarvestPermit createHarvestPermitAndDecisionWithoutSpeciesAmount(final String permitTypeCode) {
        final PermitDecision decision = model().newPermitDecision(model().newRiistanhoitoyhdistys());

        final HarvestPermit permit = model().newHarvestPermit();
        permit.setPermitTypeCode(permitTypeCode);
        permit.setPermitDecision(decision);

        return permit;
    }
}
