package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.test.TestUtils;
import fi.riista.util.DateUtil;
import fi.riista.util.MockTimeProvider;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.Minutes;
import org.junit.After;
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
public class MobileHarvestFeature_pagingTest extends EmbeddedDatabaseTest {

    @Resource
    private MobileHarvestFeature feature;

    @After
    public void tearDown() {
        MockTimeProvider.resetMock();
    }

    @Theory
    public void testFetchPageForActiveUser(final HarvestSpecVersion version) {
        withPerson(person -> {
            final List<Harvest> list = TestUtils.createList(1, () -> model().newHarvest(person, person));

            onSavedAndAuthenticated(createUser(person), () -> {
                final MobileDiaryEntryPageDTO<MobileHarvestDTO> page =
                        feature.fetchPageForActiveUser(null, version);

                assertThat(page.getContent(), hasSize(1));
                assertThat(page.isHasMore(), is(false));
                assertThat(page.getLatestEntry(), equalTo(ldt(list.get(0).getModificationTime())));
            });
        });
    }

    @Theory
    public void testFetchPageForActiveUser_onlyAuthor(final HarvestSpecVersion version) {
        withPerson(author -> {
            withPerson(shooter -> {
                model().newHarvest(author, shooter);

                onSavedAndAuthenticated(createUser(author), () -> {
                    final MobileDiaryEntryPageDTO<MobileHarvestDTO> page =
                            feature.fetchPageForActiveUser(null, version);

                    assertThat(page.getContent(), is(empty()));
                    assertThat(page.getLatestEntry(), is(nullValue()));
                });
            });
        });
    }

    @Theory
    public void testFetchPageForActiveUser_onlyShooter(final HarvestSpecVersion version) {
        withPerson(author -> {
            withPerson(shooter -> {
                final Harvest harvest = model().newHarvest(author, shooter);

                onSavedAndAuthenticated(createUser(shooter), () -> {
                    final MobileDiaryEntryPageDTO<MobileHarvestDTO> page =
                            feature.fetchPageForActiveUser(null, version);

                    assertThat(page.getContent(), hasSize(1));
                    assertThat(page.getContent().get(0).getId(), equalTo(harvest.getId()));
                    assertThat(page.getLatestEntry(), equalTo(ldt(harvest.getModificationTime())));
                });
            });
        });
    }

    @Theory
    public void testFetchPageForActiveUser_doesNotReturnWithSameTimestamp(final HarvestSpecVersion version) {
        withPerson(person -> {
            final List<Harvest> list = TestUtils.createList(1, () -> model().newHarvest(person, person));

            onSavedAndAuthenticated(createUser(person), () -> {
                final MobileDiaryEntryPageDTO<MobileHarvestDTO> page =
                        feature.fetchPageForActiveUser(null, version);

                assertThat(page.getContent(), hasSize(1));
                assertThat(page.isHasMore(), is(false));
                assertThat(page.getLatestEntry(), equalTo(ldt(list.get(0).getModificationTime())));

                final MobileDiaryEntryPageDTO<MobileHarvestDTO> secondPage =
                        feature.fetchPageForActiveUser(page.getLatestEntry(), version);
                assertThat(secondPage.getContent(), is(empty()));
                assertThat(secondPage.isHasMore(), is(false));
                assertThat(secondPage.getLatestEntry(), is(nullValue()));
            });
        });
    }

    @Theory
    public void testFetchPageForActiveUser_modifiedAfterNewestEvent(final HarvestSpecVersion version) {
        withPerson(person -> {
            final List<Harvest> list = TestUtils.createList(1, () -> model().newHarvest(person, person));

            onSavedAndAuthenticated(createUser(person), () -> {
                final MobileDiaryEntryPageDTO<MobileHarvestDTO> page =
                        feature.fetchPageForActiveUser(
                                ldt(list.get(0).getModificationTime().plusSeconds(1)), version);

                assertThat(page.getContent(), is(empty()));
                assertThat(page.isHasMore(), is(false));
                assertThat(page.getLatestEntry(), is(nullValue()));
            });
        });
    }

    @Theory
    public void testFetchPageForActiveUser_moreThanOnePage_differentTimeStamp(final HarvestSpecVersion version) {
        withPerson(person -> {
            MockTimeProvider.mockTime(now().minusHours(1).getMillis());

            final List<Harvest> list = TestUtils.createList(51, () -> {
                MockTimeProvider.advance(Minutes.minutes(1));
                final Harvest event = model().newHarvest(person, person);
                persistInNewTransaction();
                return event;
            });

            onSavedAndAuthenticated(createUser(person), () -> {
                final MobileDiaryEntryPageDTO<MobileHarvestDTO> page =
                        feature.fetchPageForActiveUser(null, version);

                assertThat(page.getContent(), hasSize(50));
                assertThat(page.isHasMore(), is(true));
                assertThat(page.getLatestEntry(), equalTo(ldt(list.get(49).getModificationTime())));
            });
        });
    }

    @Theory
    public void testFetchPageForActiveUser_moreThanOnePage_differentTimeStamp_fetchSecondPage(final HarvestSpecVersion version) {
        withPerson(person -> {
            MockTimeProvider.mockTime(now().minusHours(1).getMillis());

            final List<Harvest> list = TestUtils.createList(51, () -> {
                MockTimeProvider.advance(Minutes.minutes(1));
                final Harvest event = model().newHarvest(person, person);
                persistInNewTransaction();
                return event;
            });

            onSavedAndAuthenticated(createUser(person), () -> {
                // When searching with timestamp of the second last item, only the last one should be returned
                final MobileDiaryEntryPageDTO<MobileHarvestDTO> page =
                        feature.fetchPageForActiveUser(ldt(list.get(49).getModificationTime()), version);

                assertThat(page.getContent(), hasSize(1));
                assertThat(page.isHasMore(), is(false));
                assertThat(page.getLatestEntry(), equalTo(ldt(list.get(50).getModificationTime())));
                assertThat(page.getContent().get(0).getId(), equalTo(list.get(50).getId()));
            });
        });
    }

    @Theory
    public void testFetchPageForActiveUser_pagingDoesNotReturnDuplicates(final HarvestSpecVersion version) {
        withPerson(person -> {
            MockTimeProvider.mockTime(now().minusHours(1).getMillis());
            final LocalDateTime firstBatchTime = DateUtil.toLocalDateTimeNullSafe(now());

            final List<Harvest> list = TestUtils.createList(50, () -> model().newHarvest(person, person));
            persistInNewTransaction();

            MockTimeProvider.advance(1);
            final LocalDateTime secondBatchTime = DateUtil.toLocalDateTimeNullSafe(now());
            final List<Harvest> secondList = TestUtils.createList(1, () -> model().newHarvest(person, person));
            persistInNewTransaction();


            onSavedAndAuthenticated(createUser(person), () -> {
                final MobileDiaryEntryPageDTO<MobileHarvestDTO> page =
                        feature.fetchPageForActiveUser(null, version);

                assertThat(page.getContent(), hasSize(50));
                assertThat(page.isHasMore(), is(true));
                assertThat(page.getLatestEntry(), equalTo(firstBatchTime));


                final MobileDiaryEntryPageDTO<MobileHarvestDTO> secondPage =
                        feature.fetchPageForActiveUser(page.getLatestEntry(), version);

                assertThat(secondPage.getContent(), hasSize(1));
                assertThat(secondPage.isHasMore(), is(false));
                assertThat(secondPage.getLatestEntry(), equalTo(secondBatchTime));
                assertThat(secondPage.getContent().get(0).getId(), equalTo(secondList.get(0).getId()));
            });
        });
    }

    @Theory
    public void testFetchPageForActiveUser_moreThanOnePage_sameTimeStamp(final HarvestSpecVersion version) {
        withPerson(person -> {
            MockTimeProvider.mockTime(now().minusHours(1).getMillis());

            final List<Harvest> list = TestUtils.createList(51, () -> model().newHarvest(person, person));

            onSavedAndAuthenticated(createUser(person), () -> {
                // When more that pageful of entries found with same modification time, extra items are returned
                // in the list
                final MobileDiaryEntryPageDTO<MobileHarvestDTO> page =
                        feature.fetchPageForActiveUser(null, version);

                assertThat(page.getContent(), hasSize(51));
                assertThat(page.isHasMore(), is(true)); // Does not actually have more in this special case
                assertThat(page.getLatestEntry(), equalTo(ldt(list.get(50).getModificationTime())));

                final MobileDiaryEntryPageDTO<MobileHarvestDTO> secondPage =
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
