package fi.riista.feature.otherwisedeceased;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import org.joda.time.DateTime;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

import static fi.riista.test.Asserts.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class OtherwiseDeceasedEntityTest extends EmbeddedDatabaseTest {

    @Resource
    private OtherwiseDeceasedRepository repository;

    @Test
    public void test_findAllByPointOfTimeBetween_currentYear() {

        final EntitySupplier es = getEntitySupplier();
        final SystemUser moderator = persistAndAuthenticateWithNewUser(SystemUser.Role.ROLE_MODERATOR);

        final OtherwiseDeceased item =  es.newOtherwiseDeceased();
        final OtherwiseDeceasedChange change = es.newOtherwiseDeceasedChange(item, moderator);
        final OtherwiseDeceasedAttachment attachment = es.newOtherwiseDeceasedAttachment(item);

        onSavedAndAuthenticated(moderator, () -> {
            runInTransaction(() -> {
                final DateTime begin = new DateTime(DateUtil.currentYear(), 1, 1, 0, 0, 0, 0);
                final DateTime end = new DateTime(DateUtil.currentYear(), 12, 31,23, 59, 59, 999);

                final List<OtherwiseDeceased> results = repository.findAllByPointOfTimeBetween(begin, end);
                assertThat(results, hasSize(1));

                final OtherwiseDeceased result = results.get(0);
                assertThat(result.getAttachments(), hasSize(1));
                assertThat(result.getChangeHistory(), hasSize(1));
            });
        });

    }
}
