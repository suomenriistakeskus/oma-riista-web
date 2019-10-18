package fi.riista.feature.account.mobile;

import com.google.common.collect.ImmutableSortedMap;
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
import java.util.SortedMap;

import static org.junit.Assert.assertEquals;

public class MobileAccountFeatureV2Test extends MobileAccountFeatureTest {

    @Resource
    private MobileAccountV2Feature feature;

    @Override
    protected MobileAccountFeature feature() {
        return feature;
    }

    @Test
    public void testGetMobileAccount_succeedsWhenUserAuthenticated() {
        final SystemUser user = createEntityGraphForMobileAccountTest(
                ImmutableSortedMap.of(2014, 1, 2015, 2),
                ImmutableSortedMap.of(2013, 1, 2015, 1));

        onSavedAndAuthenticated(user, () -> {
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
        });
    }

    private SystemUser createEntityGraphForMobileAccountTest(final SortedMap<Integer, Integer> pairsOfHarvestYearAndAmount,
                                                             final SortedMap<Integer, Integer> pairsOfObservationYearAndAmount) {

        final SystemUser user = createEntityGraphForMobileAccountTest(pairsOfHarvestYearAndAmount);
        final Person person = user.getPerson();

        // Create an observation that is observed by another person (but
        // authored by the requesting person) before the first given
        // observation year to test that non-observed events are not included
        // in array of observation years.
        final int firstObservationYear = pairsOfObservationYearAndAmount.keySet().iterator().next();
        final Observation observationObservedByOtherPerson = model().newObservation(
                model().newGameSpecies(), model().newPerson(), new LocalDate(firstObservationYear - 1, 8, 1));
        observationObservedByOtherPerson.setAuthor(person);

        pairsOfObservationYearAndAmount.forEach((observationYear, observationAmount) -> {
            final LocalDate firstObservationDateForYear = new LocalDate(observationYear, 8, 1);
            final GameSpecies species = model().newGameSpecies();

            for (int i = 0; i < observationAmount; i++) {
                model().newObservation(species, person, firstObservationDateForYear.plusDays(i));
            }
        });

        return user;
    }

}
