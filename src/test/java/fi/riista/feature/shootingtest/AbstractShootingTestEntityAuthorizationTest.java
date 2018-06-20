package fi.riista.feature.shootingtest;

import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Before;
import org.junit.Test;

import static fi.riista.feature.organization.occupation.OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.TOIMINNANOHJAAJA;
import static fi.riista.util.DateUtil.today;

public abstract class AbstractShootingTestEntityAuthorizationTest<T extends BaseEntity<Long>>
        extends EmbeddedDatabaseTest {

    private Riistanhoitoyhdistys rhy;
    private T entity;

    @Before
    public void setup() {
        rhy = model().newRiistanhoitoyhdistys();
        initBeforeCreatingTargetEntity(rhy);
        entity = createEntity();
    }

    protected void initBeforeCreatingTargetEntity(@SuppressWarnings("unused") final Riistanhoitoyhdistys rhy) {
    }

    protected abstract T createEntity();

    protected abstract void testAllPermissions(final boolean permitted, final int maxQueryCount);

    protected Riistanhoitoyhdistys getRhy() {
        return rhy;
    }

    protected T getEntity() {
        return entity;
    }

    @Test
    public void testAdmin() {
        onSavedAndAuthenticated(createNewAdmin(), () -> testAllPermissions(true, 0));
    }

    @Test
    public void testModerator() {
        onSavedAndAuthenticated(createNewModerator(), () -> testAllPermissions(true, 0));
    }

    @Test
    public void testActiveCoordinator() {
        withPerson(person -> {
            model().newOccupation(rhy, person, TOIMINNANOHJAAJA);
            onSavedAndAuthenticated(createUser(person), () -> testAllPermissions(true, 3));
        });
    }

    @Test
    public void testExpiredCoordinator() {
        withPerson(person -> {
            model().newOccupation(rhy, person, TOIMINNANOHJAAJA).setEndDate(today().minusDays(1));
            onSavedAndAuthenticated(createUser(person), () -> testAllPermissions(false));
        });
    }

    @Test
    public void testActiveCoordinatorInDifferentRhy() {
        withRhy(anotherRhy -> withPerson(person -> {
            model().newOccupation(anotherRhy, person, TOIMINNANOHJAAJA);
            onSavedAndAuthenticated(createUserWithPerson(), () -> testAllPermissions(false));
        }));
    }

    @Test
    public void testExpiredShootingTestOfficial() {
        withPerson(person -> {
            model().newOccupation(rhy, person, AMPUMAKOKEEN_VASTAANOTTAJA).setEndDate(today().minusDays(1));
            onSavedAndAuthenticated(createUser(person), () -> testAllPermissions(false));
        });
    }

    @Test
    public void testActiveShootingTestOfficialInDifferentRhy() {
        withRhy(anotherRhy -> withPerson(person -> {
            model().newOccupation(anotherRhy, person, AMPUMAKOKEEN_VASTAANOTTAJA);
            onSavedAndAuthenticated(createUser(person), () -> testAllPermissions(false));
        }));
    }

    @Test
    public void testNonRhyOccupiedPerson() {
        onSavedAndAuthenticated(createUserWithPerson(), () -> testAllPermissions(false));
    }

    protected void testPermission(final Enum<?> permission, final boolean permitted, final int maxQueryCount) {
        onCheckingPermission(permission).expect(permitted).expectNumberOfQueriesAtMost(maxQueryCount).apply(entity);
    }

    protected void testAllPermissions(final boolean permitted) {
        testAllPermissions(permitted, 4);
    }
}
