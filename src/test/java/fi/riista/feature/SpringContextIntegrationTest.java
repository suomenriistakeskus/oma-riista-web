package fi.riista.feature;

import fi.riista.config.IntegrationTestApplicationContext;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.SystemUserPrivilege;
import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.feature.common.entity.EntityPersister;
import fi.riista.feature.common.fixture.FixtureMixins;
import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.feature.huntingclub.support.ClubGroupUserFunctionsBuilder;
import fi.riista.feature.organization.Riistakeskus;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.UserInfo;
import fi.riista.test.TransactionalTaskExecutor;
import fi.riista.util.DateUtil;
import fi.riista.util.NumberGenerator;
import fi.riista.util.NumberSequence;
import fi.riista.util.ValueGeneratorMixin;
import javaslang.Lazy;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.data.domain.Persistable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = IntegrationTestApplicationContext.class)
public abstract class SpringContextIntegrationTest implements ValueGeneratorMixin, FixtureMixins {

    @Rule
    public HibernateStatisticsVerifier statsVerifier = new HibernateStatisticsVerifier() {
        @Override
        protected Statistics getStatistics() {
            return sessionFactory().getStatistics();
        }
    };

    @PersistenceContext
    private EntityManager entityManager;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private EntityPersister persister;

    @Resource
    private TransactionalTaskExecutor txExecutor;

    private DateTime testStartTime;

    private Supplier<Riistakeskus> riistakeskusSupplier;

    private final List<Persistable<?>> transientEntityList = new ArrayList<>();
    private final EntitySupplier entitySupplier =
            new EntitySupplier(getNumberGenerator(), transientEntityList, () -> riistakeskusSupplier.get());

    @Before
    public void initTest() {
        this.testStartTime = DateUtil.now();
        reset();
    }

    protected void reset() {
        SecurityContextHolder.clearContext();

        transientEntityList.clear();

        riistakeskusSupplier = Lazy.of(() -> {
            final Riistakeskus rk = new Riistakeskus("Riistakeskus", "Riistakeskus");
            transientEntityList.add(rk);
            return rk;
        });
    }

    protected DateTime getTestStartTime() {
        return this.testStartTime;
    }

    protected SystemUser createNewUser(final String username) {
        return entitySupplier.newUser(username, SystemUser.Role.ROLE_USER, passwordEncoder);
    }

    protected SystemUser createNewUser(final SystemUser.Role role) {
        return entitySupplier.newUser(role, passwordEncoder);
    }

    protected SystemUser createNewUser() {
        return createNewUser(SystemUser.Role.ROLE_USER);
    }

    protected SystemUser createNewAdmin() {
        return createNewUser(SystemUser.Role.ROLE_ADMIN);
    }

    protected SystemUser createNewModerator() {
        return createNewUser(SystemUser.Role.ROLE_MODERATOR);
    }

    protected SystemUser createNewApiUser() {
        return createNewUser(SystemUser.Role.ROLE_REST);
    }

    protected SystemUser createNewApiUser(final SystemUserPrivilege privilege) {
        final SystemUser u = createNewApiUser();
        u.getPrivileges().add(privilege);
        return u;
    }

    protected SystemUser createNewUserWithPasswordAndPerson(
            final String username, final String password, final SystemUser.Role role) {

        final Person person = entitySupplier.newPerson();
        final SystemUser u = entitySupplier.newUser(username, password, role, passwordEncoder);
        u.setPerson(person);
        return u;
    }

    protected SystemUser createNewUser(final String username, final Person person) {
        final SystemUser user = createNewUser(username);
        user.setPerson(person);
        return user;
    }

    protected SystemUser createUserWithPerson() {
        return createUser(entitySupplier.newPerson());
    }

    protected SystemUser createUser(final Person person) {
        final SystemUser user = createNewUser();
        user.setPerson(person);
        return user;
    }

    protected SystemUser createUserWithPerson(final String username) {
        return createNewUser(username, entitySupplier.newPerson());
    }

    protected ClubGroupUserFunctionsBuilder clubGroupUserFunctionsBuilder() {
        return new ClubGroupUserFunctionsBuilder(entitySupplier, passwordEncoder);
    }

    protected void authenticate(final SystemUser user) {
        activeUserService.loginWithoutCheck(user);
    }

    protected void persistInNewTransaction() {
        persistInNewTransaction(transientEntityList);
        transientEntityList.clear();
    }

    protected void persistInNewTransaction(final Collection<? extends Persistable<?>> entities) {
        persister.saveInNewTransaction(entities);
        entityManager.clear();
        clearHibernateStatistics();
    }

    protected void persistInCurrentlyOpenTransaction() {
        persistInCurrentlyOpenTransaction(transientEntityList);
        transientEntityList.clear();
    }

    protected void persistInCurrentlyOpenTransaction(final Collection<? extends Persistable<?>> entities) {
        persister.saveInCurrentlyOpenTransaction(entities);
        clearHibernateStatistics();
    }

    protected SystemUser persistAndAuthenticateWithNewUser(final SystemUser.Role role) {
        final SystemUser user = createUserWithPerson();
        user.setRole(role);
        persistInNewTransaction();
        authenticate(user);
        return user;
    }

    protected SystemUser persistAndAuthenticateWithNewUser(final boolean createPerson) {
        final SystemUser user = createPerson ? createUserWithPerson() : createNewUser();
        persistInNewTransaction();
        authenticate(user);
        return user;
    }

    protected void onSavedAndAuthenticated(final SystemUser user, final Runnable task) {
        persistInNewTransaction();
        authenticate(user);
        try {
            task.run();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    protected void onSavedAndAuthenticated(final SystemUser user, final Consumer<SystemUser> task) {
        onSavedAndAuthenticated(user, () -> task.accept(user));
    }

    protected Runnable tx(final Runnable task) {
        return () -> runInTransaction(task);
    }

    protected <T> Consumer<T> tx(final Consumer<T> task) {
        return obj -> runInTransaction(() -> task.accept(obj));
    }

    protected void runInTransaction(final Runnable runnable) {
        txExecutor.execute(runnable);
    }

    protected <T> T callInTransaction(final Callable<T> callable) {
        return txExecutor.execute(callable);
    }

    protected void clearHibernateStatistics() {
        getHibernateStatistics().clear();
    }

    protected String getActiveUserName() {
        return Optional.ofNullable(activeUserService.getActiveUserInfo()).map(UserInfo::getUsername).orElse(null);
    }

    protected boolean hasPermission(final BaseEntity<?> entity, final Enum<?> permission) {
        return activeUserService.checkHasPermission(entity, permission);
    }

    protected void assertHasPermission(final boolean shouldHave, final BaseEntity<?> entity, final Enum<?> permission) {
        if (shouldHave) {
            assertHasPermission(entity, permission);
        } else {
            assertNoPermission(entity, permission);
        }
    }

    protected void assertHasPermission(final BaseEntity<?> entity, final Enum<?> permission) {
        Objects.requireNonNull(entity, "object is null");
        Objects.requireNonNull(permission, "permission is null");

        assertTrue(
                String.format("User %s should have %s permission on %s instance",
                        getActiveUserName(), permission.name(), entity.getClass().getSimpleName()),
                hasPermission(entity, permission));
    }

    protected void assertNoPermission(final BaseEntity<?> entity, final Enum<?> permission) {
        Objects.requireNonNull(entity, "object is null");
        Objects.requireNonNull(permission, "permission is null");

        assertFalse(
                String.format("User %s should not have %s permission on %s instance",
                        getActiveUserName(), permission.name(), entity.getClass().getSimpleName()),
                hasPermission(entity, permission));
    }

    protected void assertHasPermissions(final BaseEntity<?> entity, final Collection<? extends Enum<?>> permissions) {
        assertHasPermissions(true, entity, permissions);
    }

    protected void assertNoPermissions(final BaseEntity<?> entity, final Collection<? extends Enum<?>> permissions) {
        assertHasPermissions(false, entity, permissions);
    }

    protected void assertHasPermissions(
            final boolean shouldHave, final BaseEntity<?> entity, final Collection<? extends Enum<?>> permissions) {

        permissions.forEach(perm -> assertHasPermission(shouldHave, entity, perm));
    }

    protected void assertVersion(final BaseEntity<? extends Serializable> entity, final int version) {
        assertEquals(Integer.valueOf(version), entity.getConsistencyVersion());
    }

    protected <ENTITY extends BaseEntity<ID>, DTO extends BaseEntityDTO<ID>, ID extends Serializable> DTO checkDtoVersionAgainstEntity(
            final DTO dto, final Class<ENTITY> entityClass) {

        final ENTITY entity = entityManager.find(entityClass, dto.getId());
        assertNotNull(entity);
        assertEquals(entity.getConsistencyVersion(), dto.getRev());
        return dto;
    }

    // Getters -->

    @Override
    public NumberGenerator getNumberGenerator() {
        return NumberSequence.INSTANCE;
    }

    protected EntityManager entityManager() {
        return entityManager;
    }

    protected SessionFactory sessionFactory() {
        return entityManager.getEntityManagerFactory().unwrap(SessionFactory.class);
    }

    protected Statistics getHibernateStatistics() {
        return sessionFactory().getStatistics();
    }

    protected ActiveUserService activeUserService() {
        return activeUserService;
    }

    protected TransactionalTaskExecutor getTransactionalExecutor() {
        return txExecutor;
    }

    @Override
    public EntitySupplier getEntitySupplier() {
        return entitySupplier;
    }

    // Short alias for previous method
    protected EntitySupplier model() {
        return getEntitySupplier();
    }

    protected Riistakeskus getRiistakeskus() {
        return riistakeskusSupplier.get();
    }

}
