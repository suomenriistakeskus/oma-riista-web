package fi.riista.feature.gamediary.mobile;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.DateUtil;
import fi.riista.util.F;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Test;

import javax.annotation.Resource;

import javaslang.Tuple;
import javaslang.Tuple2;

public class MobileAccountFeatureV2Test extends MobileAccountFeatureTest {

    @Resource
    private MobileGameDiaryV2Feature feature;

    @Override
    protected MobileGameDiaryFeature feature() {
        return feature;
    }

    @Test
    public void testGetMobileAccount_succeedsWhenUserAuthenticated() {
        final SystemUser user = createEntityGraphForMobileAccountTest(
                asList(Tuple.of(2014, 1), Tuple.of(2015, 2)),
                asList(Tuple.of(2013, 1), Tuple.of(2015, 1)));

        persistInNewTransaction();

        authenticate(user);

        final DateTime now = DateUtil.now();
        final MobileAccountV2DTO dto = feature.getMobileAccount();

        runInTransaction(() -> {
            // Refresh Person object in order to have home municipality associated properly.
            final Person person2 = personRepo.findOne(user.getPerson().getId());

            doMobileAccountAssertions(dto, person2, user.getUsername(), F.newSortedSet(2013, 2014, 2015), now);
            assertEquals(F.newSortedSet(2014, 2015), dto.getHarvestYears());
            assertEquals(F.newSortedSet(2013, 2015), dto.getObservationYears());

            assertEquals(0, dto.getOccupations().size());
        });
    }

    private SystemUser createEntityGraphForMobileAccountTest(
            final Iterable<Tuple2<Integer, Integer>> pairsOfHarvestYearAndAmount,
            final Iterable<Tuple2<Integer, Integer>> pairsOfObservationYearAndAmount) {

        final SystemUser user = createEntityGraphForMobileAccountTest(pairsOfHarvestYearAndAmount);
        final Person person = user.getPerson();

        // Create an observation that is observed by another person (but
        // authored by the requesting person) before the first given
        // observation year to test that non-observed events are not included
        // in array of observation years.
        final int firstObservationYear = pairsOfObservationYearAndAmount.iterator().next()._1();
        final Observation observationObservedByOtherPerson = model().newObservation(
                model().newGameSpecies(), model().newPerson(), new LocalDate(firstObservationYear - 1, 8, 1));
        observationObservedByOtherPerson.setAuthor(person);

        for (final Tuple2<Integer, Integer> pair : pairsOfObservationYearAndAmount) {
            final int observationYear = pair._1();
            final LocalDate firstObservationDateForYear = new LocalDate(observationYear, 8, 1);
            final GameSpecies species = model().newGameSpecies();

            final int numObservationsForYear = pair._2();
            for (int i = 0; i < numObservationsForYear; i++) {
                model().newObservation(species, person, firstObservationDateForYear.plusDays(i));
            }
        }

        return user;
    }

}
