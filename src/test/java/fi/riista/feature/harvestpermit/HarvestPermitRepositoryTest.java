package fi.riista.feature.harvestpermit;

import fi.riista.api.pub.PublicCarnivorePermitDTO;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.MockTimeProvider;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import javax.annotation.Resource;
import java.util.Optional;
import java.util.function.Consumer;

import static fi.riista.feature.common.decision.GrantStatus.REJECTED;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_LYNX;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_OTTER;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.MAMMAL;
import static fi.riista.test.Asserts.assertThat;
import static fi.riista.util.DateUtil.beginOfCalendarYear;
import static fi.riista.util.DateUtil.currentYear;
import static fi.riista.util.DateUtil.now;
import static fi.riista.util.DateUtil.toDateTimeNullSafe;
import static fi.riista.util.DateUtil.today;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;


public class HarvestPermitRepositoryTest extends EmbeddedDatabaseTest implements HarvestPermitFixtureMixin {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Resource
    private HarvestPermitRepository repository;
    private GameSpecies lynx;

    @Before
    public void setup() {
        lynx = model().newGameSpecies(OFFICIAL_CODE_LYNX);
    }

    @After
    public void tearDown() {
        MockTimeProvider.resetMock();
    }

    @Test
    public void testFindsOnlyCarnivorePermits() {
        final GameSpecies otter = model().newGameSpecies(OFFICIAL_CODE_OTTER);
        withPublicPermit(lynx, lynxFixture ->
                withPublicPermit(otter, otterFixture -> {

                    persistInNewTransaction();
                    runInTransaction(() -> {
                        final Slice<PublicCarnivorePermitDTO> slice =
                                repository.findCarnivorePermits(null, null, null, null, pr());

                        assertThat(slice.hasNext(), is(false));
                        assertDtoContent(lynxFixture, slice);
                    });
                }));
    }

    @Test
    public void testDoesNotListTooNewDecisions_beforeLastMinuteOfDay() {
        final LocalDateTime localDateTime = today().toLocalDateTime(new LocalTime(23, 58));
        final long mockedTime = toDateTimeNullSafe(localDateTime).getMillis();
        MockTimeProvider.mockTime(mockedTime);

        withPermit(lynx, currentYear(), yesterdayFixture ->
                withPermit(lynx, currentYear(), dayBeforeYesterdayFixture -> {
                    yesterdayFixture.decision.setPublishDate(now().minusDays(1));
                    dayBeforeYesterdayFixture.decision.setPublishDate(now().minusDays(2));

                    persistInNewTransaction();
                    runInTransaction(() -> {
                        final Slice<PublicCarnivorePermitDTO> slice =
                                repository.findCarnivorePermits(null, null, null, null, pr());
                        assertThat(slice.hasNext(), is(false));
                        assertDtoContent(dayBeforeYesterdayFixture, slice);
                    });
                }));

    }

    @Test
    public void testDoesNotListTooNewDecisions_lastMinuteOfDay() {
        final LocalDateTime localDateTime = today().toLocalDateTime(new LocalTime(23, 59));
        final long mockedTime = toDateTimeNullSafe(localDateTime).getMillis();
        MockTimeProvider.mockTime(mockedTime);

        withPermit(lynx, currentYear(), yesterdayFixture ->
                withPermit(lynx, currentYear(), dayBeforeYesterdayFixture -> {
                    yesterdayFixture.decision.setPublishDate(now().minusDays(1));
                    dayBeforeYesterdayFixture.decision.setPublishDate(now().minusDays(2));

                    persistInNewTransaction();
                    runInTransaction(() -> {
                        final Slice<PublicCarnivorePermitDTO> slice =
                                repository.findCarnivorePermits(null, null, null, null, pr());
                        assertThat(slice.hasNext(), is(false));
                        assertThat(slice.getContent(), hasSize(2));
                    });
                }));
    }


    @Test
    public void testTooNewDecisionsNotAvailable_beforeLastMinuteOfDay() {
        testDecisionAvailability(new LocalTime(23, 58), false);
    }

    @Test
    public void testTooNewDecisionsNotAvailable_lastMinuteOfDay() {
        testDecisionAvailability(new LocalTime(23, 59), true);
    }

    private void testDecisionAvailability(final LocalTime time, final boolean b) {
        final LocalDateTime localDateTime = today().toLocalDateTime(time);
        final long mockedTime = toDateTimeNullSafe(localDateTime).getMillis();
        MockTimeProvider.mockTime(mockedTime);

        withPermit(lynx, currentYear(), yesterdayFixture -> {
            yesterdayFixture.decision.setPublishDate(now().minusDays(1));

            persistInNewTransaction();
            runInTransaction(() -> {
                final Optional<Long> decisionIdOptional =
                        repository.isCarnivorePermitAvailable(yesterdayFixture.permit.getPermitNumber());
                assertThat(decisionIdOptional.isPresent(), is(b));
                decisionIdOptional.ifPresent(id -> assertThat(id, equalTo(yesterdayFixture.decision.getId())));
            });
        });
    }

    @Test
    public void testFindsOnlyPublishedPermits() {
        withPublicPermit(lynx, lynxFixture ->
                withPublicPermit(lynx, draftFixture -> {
                    final PermitDecision draftDecision = draftFixture.decision;
                    draftDecision.setLockedDate(null);
                    draftDecision.setStatusDraft();

                    persistInNewTransaction();
                    runInTransaction(() -> {
                        final Slice<PublicCarnivorePermitDTO> slice =
                                repository.findCarnivorePermits(null, null, null, null, pr());

                        assertThat(slice.hasNext(), is(false));
                        assertDtoContent(lynxFixture, slice);
                    });
                }));
    }

    @Test
    public void testFindsRejectedPermitsToo() {
        withRejectedPublicDecision(lynx, rejectedDecision -> {
            persistInNewTransaction();
            runInTransaction(() -> {
                final Slice<PublicCarnivorePermitDTO> slice =
                        repository.findCarnivorePermits(null, null, null, null, pr());

                assertThat(slice.hasNext(), is(false));
                assertThat(slice.getContent(), hasSize(1));
                assertThat(slice.getContent().get(0).getPermitNumber(), equalTo(rejectedDecision.createPermitNumber()));
            });
        });
    }

    @Test
    public void testFindsByPermitNumber() {

        withPublicPermit(lynx, lynxFixture ->
                withPublicPermit(lynx, otherRkaFixture -> {

                    persistInNewTransaction();
                    runInTransaction(() -> {
                        final String permitNumber = lynxFixture.permit.getPermitNumber();
                        final Slice<PublicCarnivorePermitDTO> slice =
                                repository.findCarnivorePermits(permitNumber, null, null, null, pr());

                        assertThat(slice.hasNext(), is(false));
                        assertDtoContent(lynxFixture, slice);
                    });
                }));
    }

    @Test
    public void testFindsBySpeciesCode() {
        final GameSpecies bear = model().newGameSpecies(OFFICIAL_CODE_BEAR);

        withPublicPermit(lynx, lynxFixture ->
                withPublicPermit(bear, bearFixture -> {

                    persistInNewTransaction();
                    runInTransaction(() -> {
                        final Slice<PublicCarnivorePermitDTO> slice =
                                repository.findCarnivorePermits(null, lynx.getOfficialCode(), null, null, pr());

                        assertThat(slice.hasNext(), is(false));
                        assertDtoContent(lynxFixture, slice);
                    });
                }));
    }


    @Test
    public void testFindsByPublishedDate_publishedOnFirstDayOfYear() {
        final int previousYear = currentYear() - 1;
        final DateTime beginOfPreviousCalendarYear = beginOfCalendarYear(previousYear);

        withPermit(lynx, previousYear, lynxFixture ->
                withPermit(lynx, previousYear - 1, oldFixture -> {
                    oldFixture.decision.setPublishDate(beginOfPreviousCalendarYear.minusSeconds(1));
                    lynxFixture.decision.setPublishDate(beginOfPreviousCalendarYear);

                    persistInNewTransaction();
                    runInTransaction(() -> {
                        final Slice<PublicCarnivorePermitDTO> slice =
                                repository.findCarnivorePermits(null, null, previousYear, null, pr());

                        assertThat(slice.hasNext(), is(false));
                        assertDtoContent(lynxFixture, slice);
                    });
                }));
    }

    @Test
    public void testFindsByPublishedDate_publishedOnLastDayOfYear() {
        final int previousYear = currentYear() - 1;
        final int yearToSearch = previousYear - 1;

        withPermit(lynx, yearToSearch, lynxFixture ->
                withPermit(lynx, previousYear, tooNewFixture -> {
                    final DateTime beginOfPreviousYear = beginOfCalendarYear(previousYear);
                    tooNewFixture.decision.setPublishDate(beginOfPreviousYear);

                    // This fixture should be included in the result when searching
                    lynxFixture.decision.setPublishDate(beginOfPreviousYear.minusSeconds(1));

                    persistInNewTransaction();
                    runInTransaction(() -> {
                        final Slice<PublicCarnivorePermitDTO> slice =
                                repository.findCarnivorePermits(null, null, yearToSearch, null, pr());

                        assertThat(slice.hasNext(), is(false));
                        assertDtoContent(lynxFixture, slice);
                    });
                }));
    }

    @Test
    public void testFindsByRka() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final Riistanhoitoyhdistys otherRhy = model().newRiistanhoitoyhdistys();

        withPublicPermit(lynx, rhy, lynxFixture ->
                withPublicPermit(lynx, otherRhy, otherRkaFixture -> {

                    persistInNewTransaction();
                    runInTransaction(() -> {
                        final String rkaCode = rhy.getRiistakeskuksenAlue().getOfficialCode();
                        final Slice<PublicCarnivorePermitDTO> slice =
                                repository.findCarnivorePermits(null, null, null, rkaCode, pr());

                        assertThat(slice.hasNext(), is(false));
                        assertDtoContent(lynxFixture, slice);
                    });
                }));
    }

    @Test
    public void testPaging() {
        withPublicPermit(lynx, lynxFixture1 ->
                withPublicPermit(lynx, lynxFixture2 -> {

                    persistInNewTransaction();
                    runInTransaction(() -> {
                        final Slice<PublicCarnivorePermitDTO> firstPage =
                                repository.findCarnivorePermits(null, null, null, null, PageRequest.of(0, 1));

                        // Larger permit number should be returned first
                        assertThat(firstPage.hasNext(), is(true));
                        assertDtoContent(lynxFixture2, firstPage);

                    });

                    runInTransaction(() -> {
                        final Slice<PublicCarnivorePermitDTO> secondPage =
                                repository.findCarnivorePermits(null, null, null, null, PageRequest.of(1, 1));

                        assertThat(secondPage.hasNext(), is(false));
                        assertDtoContent(lynxFixture1, secondPage);

                    });

                }));
    }

    // Decisions are available through public api with delay so use earlier publish date for these decisions
    private void withPublicPermit(final GameSpecies species, final Consumer<HarvestPermitFixture> consumer) {
        final DateTime oneWeekAgo = now().minusWeeks(1);

        withPermit(species, oneWeekAgo.getYear(), fixture -> {
            fixture.decision.setPublishDate(now().minusWeeks(1));
            consumer.accept(fixture);
        });
    }

    private void withPublicPermit(final GameSpecies species, final Riistanhoitoyhdistys rhy,
                                  final Consumer<HarvestPermitFixture> consumer) {
        final DateTime oneWeekAgo = now().minusWeeks(1);

        withPermit(species, rhy, oneWeekAgo.getYear(), fixture -> {
            fixture.decision.setPublishDate(oneWeekAgo);
            consumer.accept(fixture);
        });
    }

    private void withRejectedPublicDecision(final GameSpecies species, final Consumer<PermitDecision> consumer) {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final HarvestPermitApplication application = model().newHarvestPermitApplication(
                rhy, null, species, MAMMAL);

        final PermitDecision decision = model().newPermitDecision(application);
        decision.setPublishDate(now().minusWeeks(1));
        decision.setGrantStatus(REJECTED);
        model().newPermitDecisionSpeciesAmount(decision, species, 0.0f);

        model().newHarvestPermit(rhy, decision.createPermitNumber(), PermitTypeCode.MAMMAL_DAMAGE_BASED, decision);

        consumer.accept(decision);
    }

    private static Pageable pr() {
        return PageRequest.of(0, 10);
    }

    private static void assertDtoContent(final HarvestPermitFixture permitFixture,
                                         final Slice<PublicCarnivorePermitDTO> slice) {
        assertThat(slice.getContent(), hasSize(1));
        final PublicCarnivorePermitDTO dto = slice.getContent().get(0);

        assertThat(dto.getPermitNumber(), equalTo(permitFixture.permit.getPermitNumber()));
        assertThat(dto.getDecisionDate(), equalTo(permitFixture.decision.getPublishDate().toLocalDate()));
        assertThat(dto.getRkaCode(), equalTo(permitFixture.rhy.getRiistakeskuksenAlue().getOfficialCode()));
        assertThat(dto.getSpeciesCode(), equalTo(permitFixture.species.getOfficialCode()));
    }

    @Override
    public TemporaryFolder getTemporaryFolder() {
        return folder;
    }
}
