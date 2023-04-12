package fi.riista.feature.news;

import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import fi.riista.util.DtoUtil;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static fi.riista.feature.account.user.SystemUserPrivilege.PUBLISH_FRONTPAGE_NEWS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class NewsFeatureTest  extends EmbeddedDatabaseTest {

    @Resource
    private NewsFeature feature;

    @Resource
    private NewsRepository repository;

    @Test
    public void testCreate() {
        onSavedAndAuthenticated(createNewModerator(PUBLISH_FRONTPAGE_NEWS), () -> {
            final NewsDTO dto = new NewsDTO();
            dto.setTitleFi("Title fi");
            dto.setTextFi("Text fi");
            dto.setLinkFi("http://link.fi");

            dto.setTitleSv("Title sv");
            dto.setTextSv("Text sv");
            dto.setLinkSv("http://link.sv");

            final LocalDateTime now = DateUtil.localDateTime();
            dto.setPublishDate(now.toLocalDate());
            dto.setPublishTime(now.toLocalTime());

            final NewsDTO outputDTO = feature.create(dto);

            runInTransaction(() -> {
                final News news = repository.getOne(outputDTO.getId());
                assertResult(news, dto);
            });
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testCreate_user() {
        onSavedAndAuthenticated(createNewUser(), () -> {
            final NewsDTO dto = new NewsDTO();
            feature.create(dto);
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testCreate_moderator() {
        onSavedAndAuthenticated(createNewModerator(), () -> {
            final NewsDTO dto = new NewsDTO();
            feature.create(dto);
        });
    }

    @Test
    public void testUpdate() {
        final News original = model().newNews("Title fi", "Text fi", "http://link.fi",
                "Title sv", "Text sv", "http://link.sv", DateTime.now());

        onSavedAndAuthenticated(createNewModerator(PUBLISH_FRONTPAGE_NEWS), () -> {
            final NewsDTO updateDTO = createMutateDTO(original);

            feature.update(updateDTO);

            runInTransaction(() -> {
                final News news = repository.getOne(original.getId());
                assertResult(news, updateDTO);
            });
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testUpdate_user() {
        final News original = model().newNews("Title fi", "Text fi", "http://link.fi",
                "Title sv", "Text sv", "http://link.sv", DateTime.now());

        onSavedAndAuthenticated(createNewUser(), () -> {
            final NewsDTO updateDTO = createMutateDTO(original);
            feature.update(updateDTO);
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testUpdate_moderator() {
        final News original = model().newNews("Title fi", "Text fi", "http://link.fi",
                "Title sv", "Text sv", "http://link.sv", DateTime.now());

        onSavedAndAuthenticated(createNewModerator(), () -> {
            final NewsDTO updateDTO = createMutateDTO(original);
            feature.update(updateDTO);
        });
    }

    @Test
    public void testListNews() {
        final DateTime now = DateTime.now();
        final List<News> originalNews = new ArrayList<>();
        IntStream.range(0, 10)
                .forEach(i -> originalNews.add(
                        model().newNews("title_fi_" + i, "text_fi_" + i, "http://link.fi/" + i,
                                "title_sv_" + i, "text_sv_" + i, "http://link.sv/" + i,
                                now.plusDays(i))));

        onSavedAndAuthenticated(createNewModerator(PUBLISH_FRONTPAGE_NEWS), () -> {
            final Slice<NewsDTO> newsList = feature.listNews(PageRequest.of(
                    0,
                    5,
                    JpaSort.of(Sort.Direction.DESC, News_.publishTime)));
            assertThat(newsList.getContent(), hasSize(5));
            assertResult(originalNews.get(9), newsList.getContent().get(0));
            assertResult(originalNews.get(8), newsList.getContent().get(1));
            assertResult(originalNews.get(7), newsList.getContent().get(2));
            assertResult(originalNews.get(6), newsList.getContent().get(3));
            assertResult(originalNews.get(5), newsList.getContent().get(4));
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testListNews_user() {
        final DateTime now = DateTime.now();
        final List<News> newsList = new ArrayList<>();
        IntStream.range(0, 10)
                .forEach(i -> newsList.add(
                        model().newNews("title_fi_" + i, "text_fi_" + i, "http://link.fi/" + i,
                                "title_sv_" + i, "text_sv_" + i, "http://link.sv/" + i,
                                now.plusDays(i))));

        onSavedAndAuthenticated(createNewUser(), () -> {
            feature.listNews(PageRequest.of(
                    0,
                    5,
                    JpaSort.of(Sort.Direction.DESC, News_.publishTime)));
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testListNews_moderator() {
        final DateTime now = DateTime.now();
        final List<News> newsList = new ArrayList<>();
        IntStream.range(0, 10)
                .forEach(i -> newsList.add(
                        model().newNews("title_fi_" + i, "text_fi_" + i, "http://link.fi/" + i,
                                "title_sv_" + i, "text_sv_" + i, "http://link.sv/" + i,
                                now.plusDays(i))));

        onSavedAndAuthenticated(createNewModerator(), () -> {
            feature.listNews(PageRequest.of(
                    0,
                    5,
                    JpaSort.of(Sort.Direction.DESC, News_.publishTime)));
        });
    }

    @Test
    public void testListLatestNews() {
        final DateTime now = DateTime.now().minusDays(1).minusHours(1);
        final List<News> originalNews = new ArrayList<>();
        IntStream.range(0, 10)
                .forEach(i -> originalNews.add(
                        model().newNews("title_fi_" + i, "text_fi_" + i, "http://link.fi/" + i,
                                "title_sv_" + i, "text_sv_" + i, "http://link.sv/" + i,
                                now.plusDays(i))));

        persistInNewTransaction();

        final List<NewsDTO> newsList = feature.listLatestNews();

        assertThat(newsList, hasSize(2));
        assertResult(originalNews.get(1), newsList.get(0));
        assertResult(originalNews.get(0), newsList.get(1));
    }

    private void assertResult(final News entity, final NewsDTO dto) {
        assertThat(entity.getTitleFi(), is(equalTo(dto.getTitleFi())));
        assertThat(entity.getTextFi(), is(equalTo(dto.getTextFi())));
        assertThat(entity.getLinkFi(), is(equalTo(dto.getLinkFi())));

        assertThat(entity.getTitleSv(), is(equalTo(dto.getTitleSv())));
        assertThat(entity.getTextSv(), is(equalTo(dto.getTextSv())));
        assertThat(entity.getLinkSv(), is(equalTo(dto.getLinkSv())));

        final DateTime entityPublishTime = entity.getPublishTime();
        assertThat(entityPublishTime.toLocalDate(), is(equalTo(dto.getPublishDate())));
        assertThat(entityPublishTime.toLocalTime(), is(equalTo(dto.getPublishTime())));
    }

    private NewsDTO createMutateDTO(final News original) {
        final NewsDTO dto = new NewsDTO();
        DtoUtil.copyBaseFields(original, dto);
        dto.setTitleFi(original.getTitleFi() + "_updated");
        dto.setTextFi(original.getTextFi() + "_updated");
        dto.setLinkFi(original.getLinkFi() + "/updated");
        dto.setTitleSv(original.getTitleSv() + "_updated");
        dto.setTextSv(original.getTextSv() + "_updated");
        dto.setLinkSv(original.getLinkSv() + "/updated");

        final DateTime originalPublishTime = original.getPublishTime();
        dto.setPublishDate(originalPublishTime.toLocalDate().plusDays(1));
        dto.setPublishTime(originalPublishTime.toLocalTime().plusHours(1));
        return dto;
    }
}
