package fi.riista.feature.common.service;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.dto.LastModifierDTO;
import fi.riista.feature.common.entity.DateTestEntity;
import fi.riista.feature.organization.person.Person;
import fi.riista.test.EmbeddedDatabaseTest;
import org.joda.time.DateTime;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class LastModifierServiceTest extends EmbeddedDatabaseTest {

    @Resource
    private LastModifierService service;

    @Test
    public void testGetLastModifiers() {
        runInTransaction(() -> {

            // Will be saved without authentication context.
            final DateTestEntity entity1 = new DateTestEntity();

            final SystemUser moderator = createNewModerator();
            moderator.setFirstName("abc");
            moderator.setLastName("def");

            // Persist order is important, entity must be before moderator.
            persistInCurrentlyOpenTransaction(asList(entity1, moderator));

            // Next entities will be persisted as moderator.
            authenticate(moderator);

            // Will be saved as moderator.
            final DateTestEntity entity2 = new DateTestEntity();

            final Person person = model().newPerson();
            final SystemUser personUser = createUser(person);

            // Persist order is important, entity must be before person and user.
            persistInCurrentlyOpenTransaction(asList(entity2, person, personUser));

            // Next entities will be persisted as personUser.
            authenticate(personUser);

            // Will be saved as user for created person.
            final DateTestEntity entity3 = new DateTestEntity();

            persistInCurrentlyOpenTransaction(asList(entity3));

            assertMaxQueryCount(2, () -> {

                final Map<DateTestEntity, LastModifierDTO> results =
                        service.getLastModifiers(asList(entity1, entity2, entity3));

                assertEquals(LastModifierDTO.createForAutomatedTask(ts(entity1)), results.get(entity1));
                assertEquals(LastModifierDTO.createForAdminOrModerator(moderator, ts(entity2)), results.get(entity2));
                assertEquals(LastModifierDTO.createForPerson(person, ts(entity3)), results.get(entity3));
            });
        });
    }

    private static DateTime ts(final DateTestEntity obj) {
        return obj.getModificationTime();
    }
}
