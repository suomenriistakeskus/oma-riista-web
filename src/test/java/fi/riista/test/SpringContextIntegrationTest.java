package fi.riista.test;

import fi.riista.config.IntegrationTestApplicationContext;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.SystemUserPrivilege;
import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.feature.common.entity.EntityPersister;
import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.feature.huntingclub.support.ClubGroupUserFunctionsBuilder;
import fi.riista.feature.organization.Riistakeskus;
import fi.riista.feature.organization.fixture.OrganisationFixtureMixin;
import fi.riista.feature.organization.person.Person;
import fi.riista.test.rules.HibernateStatisticsVerifier;
import fi.riista.test.rules.SpringRuleConfigurer;
import fi.riista.util.DateUtil;
import fi.riista.util.NumberGenerator;
import fi.riista.util.NumberSequence;
import fi.riista.util.TransactionalTaskExecutor;
import fi.riista.util.ValueGeneratorMixin;
import io.vavr.Lazy;
import org.hibernate.stat.Statistics;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.springframework.data.domain.Persistable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@ContextConfiguration(classes = IntegrationTestApplicationContext.class)
public abstract class SpringContextIntegrationTest extends SpringRuleConfigurer
        implements OrganisationFixtureMixin, ValueGeneratorMixin {

    @Rule
    public final HibernateStatisticsVerifier statsVerifier = new HibernateStatisticsVerifier() {
        @Override
        protected Statistics getStatistics() {
            return getHibernateStatistics();
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

    // Query statistics based assertion helpers -->

    protected void clearHibernateStatistics() {
        getHibernateStatistics().clear();
    }

    protected void assertQueryCount(final int expectedQueryCount, final Runnable task) {
        HibernateStatisticsHelper.assertQueryCount(getHibernateStatistics(), expectedQueryCount, task);
    }

    protected void assertMaxQueryCount(final int expectedMaxQueryCount, final Runnable task) {
        HibernateStatisticsHelper.assertMaxQueryCount(getHibernateStatistics(), expectedMaxQueryCount, task);
    }

    // Authorization helpers -->

    protected boolean hasPermission(final BaseEntity<?> entity, final Enum<?> permission) {
        return activeUserService.checkHasPermission(entity, permission);
    }

    protected PermissionCheckContext onCheckingPermission(@Nonnull final Enum<?> permission) {
        return new PermissionCheckContext(entityManager, activeUserService, txExecutor).withPermission(permission);
    }

    protected void assertPermission(final boolean expectation, final BaseEntity<?> entity, final Enum<?> permission) {
        onCheckingPermission(permission).expect(expectation).apply(entity);
    }

    protected void assertHasPermission(final BaseEntity<?> entity, final Enum<?> permission) {
        assertPermission(true, entity, permission);
    }

    protected void assertNoPermission(final BaseEntity<?> entity, final Enum<?> permission) {
        assertPermission(false, entity, permission);
    }

    protected void assertHasPermissions(final BaseEntity<?> entity, final Collection<? extends Enum<?>> permissions) {
        assertPermissions(true, entity, permissions);
    }

    protected void assertNoPermissions(final BaseEntity<?> entity, final Collection<? extends Enum<?>> permissions) {
        assertPermissions(false, entity, permissions);
    }

    protected void assertPermissions(
            final boolean expected, final BaseEntity<?> entity, final Collection<? extends Enum<?>> permissions) {

        permissions.forEach(perm -> assertPermission(expected, entity, perm));
    }

    // Revision check utilities -->

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

    protected Statistics getHibernateStatistics() {
        return HibernateStatisticsHelper.getStatistics(entityManager);
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

    // Short alias for getEntitySupplier()
    protected EntitySupplier model() {
        return getEntitySupplier();
    }

    protected Riistakeskus getRiistakeskus() {
        return riistakeskusSupplier.get();
    }
}
