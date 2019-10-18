package fi.riista.feature.harvestpermit.list;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import static org.junit.Assert.assertEquals;

public class HarvestPermitListFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private HarvestPermitListFeature harvestPermitListFeature;

    private RiistakeskuksenAlue rka;

    @Before
    public void initRka() {
        this.rka = model().newRiistakeskuksenAlue();
    }

    @Test
    public void testListMyPermits() {
        doTestListPermits(createUserWithPerson("user1"), null);
    }

    @Test
    public void testListMyPermitsAsModerator() {
        doTestListPermits(createNewModerator(), model().newPerson());
    }

    private void doTestListPermits(final SystemUser callingUser, Person personViewed) {
        withRhy(rhy -> {
            final Person permitContact = personViewed != null ? personViewed : callingUser.getPerson();

            final HarvestPermit permit = model().newHarvestPermit(rhy, permitContact, true);

            final HarvestPermit amendmentPermit = model().newHarvestPermit(permit, permitContact, true);
            amendmentPermit.setPermitTypeCode(PermitTypeCode.MOOSELIKE_AMENDMENT);

            // Permit for which the user is not contact person, should not be included in the results.
            model().newHarvestPermit(rhy, model().newPerson(), true);

            onSavedAndAuthenticated(callingUser, () -> {
                final List<ListHarvestPermitDTO> results = harvestPermitListFeature.listPermitsForPerson(F.getId(personViewed));

                // Very limited testing - does not test other content of DTOs yet.
                assertEquals(F.getUniqueIds(permit), F.getUniqueIds(results));
            });
        });
    }

    @Test
    public void testMooselikePermits() {
        runTest((species, rhy) -> newHarvestPermit(rhy, species));
    }

    @Test
    public void testMooselikePermits_relatedRhy() {
        runTest((species, rhy) -> {
            final HarvestPermit p = newHarvestPermit(model().newRiistanhoitoyhdistys(this.rka), species);
            p.setRelatedRhys(Collections.singleton(rhy));
            return p;
        });
    }

    private HarvestPermit newHarvestPermit(final Riistanhoitoyhdistys rhy, final GameSpecies species) {
        final HarvestPermit permit = model().newMooselikePermit(rhy);
        final HuntingClub club = model().newHuntingClub(rhy);
        permit.setHuntingClub(club);
        permit.setPermitHolder(PermitHolder.createHolderForClub(club));

        model().newHarvestPermitSpeciesAmount(permit, species);

        return permit;
    }

    private void runTest(BiFunction<GameSpecies, Riistanhoitoyhdistys, HarvestPermit> permitCreator) {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys(this.rka);
        final Riistanhoitoyhdistys otherRhy = model().newRiistanhoitoyhdistys(this.rka);

        final GameSpecies moose = model().newGameSpeciesMoose();

        final HarvestPermit permit = permitCreator.apply(moose, rhy);
        permitCreator.apply(moose, otherRhy);

        onSavedAndAuthenticated(createNewAdmin(), tx(() -> {
            final List<MooselikeHuntingYearDTO> years = harvestPermitListFeature.listRhyMooselikeHuntingYears(rhy.getId());
            assertEquals(1, years.size());

            final int year = DateUtil.currentYear();
            assertEquals(year, permit.getPermitYear());

            final List<MooselikePermitListDTO> permits = harvestPermitListFeature.listRhyMooselikePermits(rhy.getId(), year, GameSpecies.OFFICIAL_CODE_MOOSE, null);
            assertEquals(1, permits.size());
            assertEquals((long) permit.getId(), permits.get(0).getId());
        }));
    }
}
