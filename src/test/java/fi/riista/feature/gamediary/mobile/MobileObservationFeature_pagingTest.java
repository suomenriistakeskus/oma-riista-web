package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.fixture.ObservationFixtureMixin;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationSpecVersion;
import fi.riista.test.TestUtils;
import fi.riista.util.DateUtil;
import fi.riista.util.MockTimeProvider;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.Minutes;
import org.junit.After;
import org.junit.Before;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import java.util.List;

import static fi.riista.test.Asserts.assertThat;
import static fi.riista.util.DateUtil.now;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@RunWith(Theories.class)
public class MobileObservationFeature_pagingTest extends MobileObservationFeatureTestBase
        implements ObservationFixtureMixin {

    @Resource
    private MobileObservationFeature feature;

    private GameSpecies species;

    @After
    public void tearDown() {
        MockTimeProvider.resetMock();
    }

    @Before
    public void setup() {
        species = model().newGameSpecies(GameSpecies.OFFICIAL_CODE_ROE_DEER);
    }


    @Theory
    public void testFetchPageForActiveUser(final ObservationSpecVersion version) {
        model().newObservationBaseFields(species, version);

        withPerson(person -> {
            final List<Observation> list = TestUtils.createList(1, () -> model().newObservation(species, person));

            onSavedAndAuthenticated(createUser(person), () -> {
                final MobileDiaryEntryPageDTO<MobileObservationDTO> page =
                        feature.fetchPageForActiveUser(null, version);

                assertThat(page.getContent(), hasSize(1));
                assertThat(page.isHasMore(), is(false));
                assertThat(page.getLatestEntry(), equalTo(ldt(list.get(0).getModificationTime())));
            });
        });
    }

    @Theory
    public void testFetchPageForActiveUser_onlyAuthor(final ObservationSpecVersion version) {
        model().newObservationBaseFields(species, version);

        withPerson(author -> {
            withPerson(observer -> {

                model().newObservation(species, author, observer);

                onSavedAndAuthenticated(createUser(author), () -> {
                    final MobileDiaryEntryPageDTO<MobileObservationDTO> page =
                            feature.fetchPageForActiveUser(null, version);

                    assertThat(page.getContent(), is(empty()));
                    assertThat(page.isHasMore(), is(false));
                    assertThat(page.getLatestEntry(), is(nullValue()));
                });
            });
        });
    }

    @Theory
    public void testFetchPageForActiveUser_onlyObserver(final ObservationSpecVersion version) {
        model().newObservationBaseFields(species, version);

        withPerson(author -> {
            withPerson(observer -> {

                final Observation observation = model().newObservation(species, author, observer);

                onSavedAndAuthenticated(createUser(observer), () -> {
                    final MobileDiaryEntryPageDTO<MobileObservationDTO> page =
                            feature.fetchPageForActiveUser(null, version);

                    assertThat(page.getContent(), hasSize(1));
                    assertThat(page.getContent().get(0).getId(), equalTo(observation.getId()));
                    assertThat(page.isHasMore(), is(false));
                    assertThat(page.getLatestEntry(), equalTo(ldt(observation.getModificationTime())));
                });
            });
        });
    }

    @Theory
    public void testFetchPageForActiveUser_doesNotReturnWithSameTimestamp(final ObservationSpecVersion version) {
        model().newObservationBaseFields(species, version);

        withPerson(person -> {
            final List<Observation> list = TestUtils.createList(1, () -> model().newObservation(species, person));

            onSavedAndAuthenticated(createUser(person), () -> {
                final MobileDiaryEntryPageDTO<MobileObservationDTO> page =
                        feature.fetchPageForActiveUser(null, version);

                assertThat(page.getContent(), hasSize(1));
                assertThat(page.isHasMore(), is(false));
                assertThat(page.getLatestEntry(), equalTo(ldt(list.get(0).getModificationTime())));

                final MobileDiaryEntryPageDTO<MobileObservationDTO> secondPage =
                        feature.fetchPageForActiveUser(page.getLatestEntry(), version);
                assertThat(secondPage.getContent(), is(empty()));
                assertThat(secondPage.isHasMore(), is(false));
                assertThat(secondPage.getLatestEntry(), is(nullValue()));
            });
        });
    }

    @Theory
    public void testFetchPageForActiveUser_modifiedAfterNewestEvent(final ObservationSpecVersion version) {
        model().newObservationBaseFields(species, version);

        withPerson(person -> {
            final List<Observation> list = TestUtils.createList(1, () -> model().newObservation(species, person));

            onSavedAndAuthenticated(createUser(person), () -> {
                final MobileDiaryEntryPageDTO<MobileObservationDTO> page =
                        feature.fetchPageForActiveUser(
                                ldt(list.get(0).getModificationTime().plusSeconds(1)), version);

                assertThat(page.getContent(), is(empty()));
                assertThat(page.isHasMore(), is(false));
                assertThat(page.getLatestEntry(), is(nullValue()));
            });
        });
    }

    @Theory
    public void testFetchPageForActiveUser_moreThanOnePage_differentTimeStamp(final ObservationSpecVersion version) {
        model().newObservationBaseFields(species, version);

        withPerson(person -> {

            MockTimeProvider.mockTime(now().minusHours(1).getMillis());

            final List<Observation> list = TestUtils.createList(51, () -> {
                MockTimeProvider.advance(Minutes.minutes(1));
                final Observation event = model().newObservation(species, person);
                persistInNewTransaction();
                return event;
            });

            onSavedAndAuthenticated(createUser(person), () -> {
                final MobileDiaryEntryPageDTO<MobileObservationDTO> page =
                        feature.fetchPageForActiveUser(null, version);

                assertThat(page.getContent(), hasSize(50));
                assertThat(page.isHasMore(), is(true));
                assertThat(page.getLatestEntry(), equalTo(ldt(list.get(49).getModificationTime())));
            });
        });
    }

    @Theory
    public void testFetchPageForActiveUser_moreThanOnePage_differentTimeStamp_fetchSecondPage(final ObservationSpecVersion version) {
        model().newObservationBaseFields(species, version);

        withPerson(person -> {
            MockTimeProvider.mockTime(now().minusHours(1).getMillis());

            final List<Observation> list = TestUtils.createList(51, () -> {
                MockTimeProvider.advance(Minutes.minutes(1));
                final Observation event = model().newObservation(species, person);
                persistInNewTransaction();
                return event;
            });

            onSavedAndAuthenticated(createUser(person), () -> {
                // When searching with timestamp of the second last item, only the last one should be returned
                final MobileDiaryEntryPageDTO<MobileObservationDTO> page =
                        feature.fetchPageForActiveUser(ldt(list.get(49).getModificationTime()), version);

                assertThat(page.getContent(), hasSize(1));
                assertThat(page.isHasMore(), is(false));
                assertThat(page.getLatestEntry(), equalTo(ldt(list.get(50).getModificationTime())));
                assertThat(page.getContent().get(0).getId(), equalTo(list.get(50).getId()));
            });
        });
    }

    @Theory
    public void testFetchPageForActiveUser_pagingDoesNotReturnDuplicates(final ObservationSpecVersion version) {
        model().newObservationBaseFields(species, version);

        withPerson(person -> {
            MockTimeProvider.mockTime(now().minusHours(1).getMillis());
            final LocalDateTime firstBatchTime = DateUtil.toLocalDateTimeNullSafe(now());

            final List<Observation> list = TestUtils.createList(50, () -> model().newObservation(species, person));
            persistInNewTransaction();

            MockTimeProvider.advance(1);
            final LocalDateTime secondBatchTime = DateUtil.toLocalDateTimeNullSafe(now());
            final List<Observation> secondList = TestUtils.createList(1, () -> model().newObservation(species, person));
            persistInNewTransaction();


            onSavedAndAuthenticated(createUser(person), () -> {
                final MobileDiaryEntryPageDTO<MobileObservationDTO> page =
                        feature.fetchPageForActiveUser(null, version);

                assertThat(page.getContent(), hasSize(50));
                assertThat(page.isHasMore(), is(true));
                assertThat(page.getLatestEntry(), equalTo(firstBatchTime));


                final MobileDiaryEntryPageDTO<MobileObservationDTO> secondPage =
                        feature.fetchPageForActiveUser(page.getLatestEntry(), version);

                assertThat(secondPage.getContent(), hasSize(1));
                assertThat(secondPage.isHasMore(), is(false));
                assertThat(secondPage.getLatestEntry(), equalTo(secondBatchTime));
                assertThat(secondPage.getContent().get(0).getId(), equalTo(secondList.get(0).getId()));
            });
        });
    }

    @Theory
    public void testFetchPageForActiveUser_moreThanOnePage_sameTimeStamp(final ObservationSpecVersion version) {
        model().newObservationBaseFields(species, version);

        withPerson(person -> {
            MockTimeProvider.mockTime(now().minusHours(1).getMillis());

            final List<Observation> list = TestUtils.createList(51, () -> model().newObservation(species, person));

            onSavedAndAuthenticated(createUser(person), () -> {
                // When more that pageful of entries found with same modification time, extra items are returned
                // in the list
                final MobileDiaryEntryPageDTO<MobileObservationDTO> page =
                        feature.fetchPageForActiveUser(null, version);

                assertThat(page.getContent(), hasSize(51));
                assertThat(page.isHasMore(), is(true)); // Does not actually have more in this special case
                assertThat(page.getLatestEntry(), equalTo(ldt(list.get(50).getModificationTime())));

                final MobileDiaryEntryPageDTO<MobileObservationDTO> secondPage =
                        feature.fetchPageForActiveUser(ldt((list.get(50).getModificationTime())), version);

                assertThat(secondPage.getContent(), is(empty()));
                assertThat(secondPage.isHasMore(), is(false));
                assertThat(secondPage.getLatestEntry(), is(nullValue()));
            });
        });
    }

    private LocalDateTime ldt(final DateTime modificationTime) {
        return DateUtil.toLocalDateTimeNullSafe(modificationTime);
    }
}
