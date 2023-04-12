package fi.riista.feature.permit.decision;

import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.decision.revision.PermitDecisionRevision;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import fi.riista.util.MockTimeProvider;
import org.joda.time.LocalDateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static fi.riista.feature.harvestpermit.HarvestPermitCategory.DISABILITY;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.DOG_DISTURBANCE;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.DOG_UNLEASH;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.GAME_MANAGEMENT;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.IMPORTING;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.LAW_SECTION_TEN;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.MOOSELIKE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class PermitDecisionPaymentAmountUpdateFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private PermitDecisionPaymentAmountUpdateFeature feature;

    @Resource
    private PermitDecisionRepository repository;

    private final LocalDateTime newPriceDate = new LocalDateTime(2022, 1, 1, 0, 0, 0);
    private final LocalDateTime oldPriceDate = new LocalDateTime(2021, 12, 31, 23, 59, 0);

    @Before
    public void setup() {
        MockTimeProvider.mockTime(newPriceDate.minusMinutes(1).toDate().getTime());
    }

    @After
    public void tearDown() {
        MockTimeProvider.resetMock();
    }

    @Test
    public void testUpdateNewPayments_mooselike() {
        createDecision(MOOSELIKE, PermitDecisionPaymentAmount2021.PRICE_MOOSELIKE, newPriceDate);

        onSavedAndAuthenticated(createNewAdmin(), () -> {
            feature.updateNewPayments();

            runInTransaction(() -> {
                final List<PermitDecision> decisions = repository.findAll();
                assertResult(decisions, PermitDecisionPaymentAmount2022.PRICE_MOOSELIKE);
            });
        });
    }

    @Test
    public void testUpdateNewPayments_mooselikeNotUpdated() {
        createDecision(MOOSELIKE, PermitDecisionPaymentAmount2021.PRICE_MOOSELIKE, oldPriceDate);

        onSavedAndAuthenticated(createNewAdmin(), () -> {
            feature.updateNewPayments();

            runInTransaction(() -> {
                final List<PermitDecision> decisions = repository.findAll();
                assertResult(decisions, PermitDecisionPaymentAmount2021.PRICE_MOOSELIKE);
            });
        });
    }

    @Test
    public void testUpdateNewPayments_lawSectionTen() {
        createDecision(LAW_SECTION_TEN, PermitDecisionPaymentAmount2021.PRICE_LAW_SECTION_TEN, newPriceDate);

        onSavedAndAuthenticated(createNewAdmin(), () -> {
            feature.updateNewPayments();

            runInTransaction(() -> {
                final List<PermitDecision> decisions = repository.findAll();
                assertResult(decisions, PermitDecisionPaymentAmount2022.PRICE_LAW_SECTION_TEN);
            });
        });
    }

    @Test
    public void testUpdateNewPayments_lawSectionTenNotUpdated() {
        createDecision(LAW_SECTION_TEN, PermitDecisionPaymentAmount2021.PRICE_LAW_SECTION_TEN, oldPriceDate);

        onSavedAndAuthenticated(createNewAdmin(), () -> {
            feature.updateNewPayments();

            runInTransaction(() -> {
                final List<PermitDecision> decisions = repository.findAll();
                assertResult(decisions, PermitDecisionPaymentAmount2021.PRICE_LAW_SECTION_TEN);
            });
        });
    }

    @Test
    public void testUpdateNewPayments_disability() {
        createDecision(DISABILITY, PermitDecisionPaymentAmount2021.PRICE_DISABILITY, newPriceDate);

        onSavedAndAuthenticated(createNewAdmin(), () -> {
            feature.updateNewPayments();

            runInTransaction(() -> {
                final List<PermitDecision> decisions = repository.findAll();
                assertResult(decisions, PermitDecisionPaymentAmount2022.PRICE_DISABILITY);
            });
        });
    }

    @Test
    public void testUpdateNewPayments_disabilityNotUpdated() {
        createDecision(DISABILITY, PermitDecisionPaymentAmount2021.PRICE_DISABILITY, oldPriceDate);

        onSavedAndAuthenticated(createNewAdmin(), () -> {
            feature.updateNewPayments();

            runInTransaction(() -> {
                final List<PermitDecision> decisions = repository.findAll();
                assertResult(decisions, PermitDecisionPaymentAmount2021.PRICE_DISABILITY);
            });
        });
    }

    @Test
    public void testUpdateNewPayments_dogUnleash() {
        createDecision(DOG_UNLEASH, PermitDecisionPaymentAmount2021.PRICE_DOG_EVENT, newPriceDate);

        onSavedAndAuthenticated(createNewAdmin(), () -> {
            feature.updateNewPayments();

            runInTransaction(() -> {
                final List<PermitDecision> decisions = repository.findAll();
                assertResult(decisions, PermitDecisionPaymentAmount2022.PRICE_DOG_EVENT);
            });
        });
    }

    @Test
    public void testUpdateNewPayments_dogUnleashNotUpdated() {
        createDecision(DOG_UNLEASH, PermitDecisionPaymentAmount2021.PRICE_DOG_EVENT, oldPriceDate);

        onSavedAndAuthenticated(createNewAdmin(), () -> {
            feature.updateNewPayments();

            runInTransaction(() -> {
                final List<PermitDecision> decisions = repository.findAll();
                assertResult(decisions, PermitDecisionPaymentAmount2021.PRICE_DOG_EVENT);
            });
        });
    }

    @Test
    public void testUpdateNewPayments_dogDisturbance() {
        createDecision(DOG_DISTURBANCE, PermitDecisionPaymentAmount2021.PRICE_DOG_EVENT, newPriceDate);

        onSavedAndAuthenticated(createNewAdmin(), () -> {
            feature.updateNewPayments();

            runInTransaction(() -> {
                final List<PermitDecision> decisions = repository.findAll();
                assertResult(decisions, PermitDecisionPaymentAmount2022.PRICE_DOG_EVENT);
            });
        });
    }

    @Test
    public void testUpdateNewPayments_dogDisturbanceNotUpdated() {
        createDecision(DOG_DISTURBANCE, PermitDecisionPaymentAmount2021.PRICE_DOG_EVENT, oldPriceDate);

        onSavedAndAuthenticated(createNewAdmin(), () -> {
            feature.updateNewPayments();

            runInTransaction(() -> {
                final List<PermitDecision> decisions = repository.findAll();
                assertResult(decisions, PermitDecisionPaymentAmount2021.PRICE_DOG_EVENT);
            });
        });
    }

    @Test
    public void testUpdateNewPayments_importing() {
        createDecision(IMPORTING, PermitDecisionPaymentAmount2021.PRICE_IMPORTING, newPriceDate);

        onSavedAndAuthenticated(createNewAdmin(), () -> {
            feature.updateNewPayments();

            runInTransaction(() -> {
                final List<PermitDecision> decisions = repository.findAll();
                assertResult(decisions, PermitDecisionPaymentAmount2022.PRICE_IMPORTING);
            });
        });
    }

    @Test
    public void testUpdateNewPayments_importingNotUpdated() {
        createDecision(IMPORTING, PermitDecisionPaymentAmount2021.PRICE_IMPORTING, oldPriceDate);

        onSavedAndAuthenticated(createNewAdmin(), () -> {
            feature.updateNewPayments();

            runInTransaction(() -> {
                final List<PermitDecision> decisions = repository.findAll();
                assertResult(decisions, PermitDecisionPaymentAmount2021.PRICE_IMPORTING);
            });
        });
    }

    @Test
    public void testUpdateNewPayments_gameManagement() {
        createDecision(GAME_MANAGEMENT, PermitDecisionPaymentAmount2021.PRICE_GAME_MANAGEMENT, newPriceDate);

        onSavedAndAuthenticated(createNewAdmin(), () -> {
            feature.updateNewPayments();

            runInTransaction(() -> {
                final List<PermitDecision> decisions = repository.findAll();
                assertResult(decisions, PermitDecisionPaymentAmount2022.PRICE_GAME_MANAGEMENT);
            });
        });
    }

    @Test
    public void testUpdateNewPayments_gameManagementNotUpdated() {
        createDecision(GAME_MANAGEMENT, PermitDecisionPaymentAmount2021.PRICE_GAME_MANAGEMENT, oldPriceDate);

        onSavedAndAuthenticated(createNewAdmin(), () -> {
            feature.updateNewPayments();

            runInTransaction(() -> {
                final List<PermitDecision> decisions = repository.findAll();
                assertResult(decisions, PermitDecisionPaymentAmount2021.PRICE_GAME_MANAGEMENT);
            });
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testUpdateNewPayments_moderator() {
        createDecision(MOOSELIKE, PermitDecisionPaymentAmount2021.PRICE_MOOSELIKE, newPriceDate);

        onSavedAndAuthenticated(createNewModerator(), () -> {
            feature.updateNewPayments();
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testUpdateNewPayments_user() {
        createDecision(MOOSELIKE, PermitDecisionPaymentAmount2021.PRICE_MOOSELIKE, newPriceDate);

        onSavedAndAuthenticated(createNewUser(), () -> {
            feature.updateNewPayments();
        });
    }

    @Test
    public void testUpdateNewPayments_alreadyPublishedDecisionWithNewRevision() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final HarvestPermitApplication application =
                model().newHarvestPermitApplication(rhy, null, GAME_MANAGEMENT);
        final PermitDecision decision = model().newPermitDecision(application);
        decision.setPaymentAmount(PermitDecisionPaymentAmount2021.PRICE_GAME_MANAGEMENT);
        final PermitDecisionRevision revision = model().newPermitDecisionRevision(decision);
        revision.setScheduledPublishDate(DateUtil.toDateTimeNullSafe(oldPriceDate));
        final PermitDecisionRevision revision2 = model().newPermitDecisionRevision(decision);
        revision2.setScheduledPublishDate(DateUtil.toDateTimeNullSafe(newPriceDate));

        onSavedAndAuthenticated(createNewAdmin(), () -> {
            feature.updateNewPayments();

            runInTransaction(() -> {
                final PermitDecision expectedDecision2 = repository.getOne(decision.getId());
                assertResult(Collections.singletonList(expectedDecision2), PermitDecisionPaymentAmount2021.PRICE_GAME_MANAGEMENT);
            });
        });
    }

    private void createDecision(final HarvestPermitCategory category, final BigDecimal price, final LocalDateTime scheduledPublishDate) {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final HarvestPermitApplication application =
                model().newHarvestPermitApplication(rhy, null, category);
        final PermitDecision decision = model().newPermitDecision(application);
        decision.setPaymentAmount(price);
        final PermitDecisionRevision revision = model().newPermitDecisionRevision(decision);
        revision.setScheduledPublishDate(DateUtil.toDateTimeNullSafe(scheduledPublishDate));
    }

    private void assertResult(final List<PermitDecision> decisions, final BigDecimal price) {
        assertThat(decisions, hasSize(1));

        final PermitDecision decision = decisions.get(0);
        assertThat(decision.getPaymentAmount(), is(equalTo(price)));
    }
}
