package fi.riista.feature.account.mobile;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import io.vavr.Tuple;
import org.joda.time.DateTime;
import org.junit.Test;

import javax.annotation.Resource;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class MobileAccountFeatureV1Test extends MobileAccountFeatureTest {

    @Resource
    private MobileAccountV1Feature feature;

    @Override
    protected MobileAccountFeature feature() {
        return feature;
    }

    @Test
    public void testGetMobileAccount_succeedsWhenUserAuthenticated() {
        final SystemUser user = createEntityGraphForMobileAccountTest(asList(Tuple.of(2014, 1), Tuple.of(2015, 2)));

        persistInNewTransaction();

        authenticate(user);

        final DateTime now = DateUtil.now();
        final MobileAccountV1DTO dto = feature.getMobileAccount();

        runInTransaction(() -> {
            // Refresh Person object in order to have home municipality set.
            final Person person2 = personRepo.findOne(user.getPerson().getId());

            doMobileAccountAssertions(dto, person2, user.getUsername(), F.newSortedSet(2014, 2015), now);
            assertEquals(0, dto.getOccupations().size());
        });
    }
}
