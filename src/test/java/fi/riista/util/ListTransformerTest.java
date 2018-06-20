package fi.riista.util;

import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Ints;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.SystemUserPrivilege;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.test.EmbeddedDatabaseTest;
import org.hibernate.LazyInitializationException;
import org.junit.Test;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@Component
class LazyInitializationTestTransformer extends ListTransformer<SystemUser, Set<SystemUserPrivilege>> {

    @Resource
    private UserRepository userRepo;

    @Transactional(readOnly = true)
    @Nonnull
    @Override
    public List<Set<SystemUserPrivilege>> transform(@Nonnull final List<SystemUser> users) {
        return transformNonTransactionally(users);
    }

    @Transactional(propagation = Propagation.NEVER)
    @Nonnull
    public List<Set<SystemUserPrivilege>> transformNonTransactionally(@Nonnull final List<SystemUser> users) {
        final Set<Long> userIds = F.getUniqueIds(Objects.requireNonNull(users));

        return F.mapNonNullsToList(userRepo.findAll(userIds),
                user -> user == null ? null : ImmutableSet.copyOf(user.getPrivileges()));
    }
}

public class ListTransformerTest extends EmbeddedDatabaseTest {

    private static final ListTransformer<Integer, String> NON_PROXY_INSTANCE = new ListTransformer<Integer, String>() {
        @Nonnull
        @Override
        protected List<String> transform(@Nonnull final List<Integer> list) {
            return toStringList(list);
        }
    };

    private static final Integer NULL = null;
    private static final Integer NON_NULL = 1234567890;

    private static final List<Integer> VALUE_LIST = Ints.asList(1, 2, 3, 5, 8, 13, 21, 34, 55, 89);

    @Resource
    private LazyInitializationTestTransformer proxyInstance;

    @Resource
    private UserRepository userRepo;

    @Test
    public void testApply() {
        assertNull((List<Integer>) null);
        assertEquals(toStringList(VALUE_LIST), NON_PROXY_INSTANCE.apply(VALUE_LIST));
    }

    @Test
    public void testApply_singularVersion() {
        assertNull(NON_PROXY_INSTANCE.apply(NULL));
        assertEquals(NON_NULL.toString(), NON_PROXY_INSTANCE.apply(NON_NULL));
    }

    @Test
    public void testAsFunction() {
        assertNull(NON_PROXY_INSTANCE.asFunction().apply((List<Integer>) null));
        assertEquals(toStringList(VALUE_LIST), NON_PROXY_INSTANCE.asFunction().apply(VALUE_LIST));
    }

    @Test
    public void testAsSingletonFunction() {
        assertEquals(NON_PROXY_INSTANCE.apply(NULL), NON_PROXY_INSTANCE.asSingletonFunction().apply(NULL));
        assertEquals(NON_PROXY_INSTANCE.apply(NON_NULL), NON_PROXY_INSTANCE.asSingletonFunction().apply(NON_NULL));
    }

    // Test that @Transactional annotation is in effect and
    // calls are intercepted by Spring's AOP proxy (CGLIB).
    @Test
    public void testTransactionalApplication_withTransaction() {
        persistUsersWithPasswords();

        // Should not trigger LazyInitializationException.
        proxyInstance.apply(userRepo.findAll());
    }

    @Test(expected = LazyInitializationException.class)
    public void testNonTransactionalApplication_withoutTransaction() {
        persistUsersWithPasswords();
        proxyInstance.transformNonTransactionally(userRepo.findAll());
    }

    private static List<String> toStringList(final List<Integer> list) {
        return Objects.requireNonNull(list).stream().map(Objects::toString).collect(toList());
    }

    private void persistUsersWithPasswords() {
        for (int i = 1; i <= 3; i++) {
            final SystemUser user = createNewUser("test" + i);
            user.addPrivilege(SystemUserPrivilege.CHECK_EXTERNAL_AUTHENTICATION);
            user.addPrivilege(SystemUserPrivilege.EXPORT_HUNTINGCLUB_AREA);
            user.addPrivilege(SystemUserPrivilege.IMPORT_JHT);
        }

        persistInNewTransaction();
    }

}
