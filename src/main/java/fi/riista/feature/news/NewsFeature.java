package fi.riista.feature.news;

import com.querydsl.core.types.dsl.BooleanExpression;
import fi.riista.feature.AbstractCrudFeature;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NewsFeature extends AbstractCrudFeature<Long, News, NewsDTO> {

    @Resource
    private NewsRepository newsRepository;

    @Resource
    private NewsDTOTransformer transformer;

    @Override
    protected JpaRepository<News, Long> getRepository() {
        return newsRepository;
    }

    @Override
    protected void updateEntity(final News news, final NewsDTO newsDTO) {
        news.setTitleFi(newsDTO.getTitleFi());
        news.setTextFi(newsDTO.getTextFi());
        news.setLinkFi(newsDTO.getLinkFi());

        news.setTitleSv(newsDTO.getTitleSv());
        news.setTextSv(newsDTO.getTextSv());
        news.setLinkSv(newsDTO.getLinkSv());

        final LocalDate publishDate = newsDTO.getPublishDate();
        if (publishDate != null) {
            final LocalTime publishTime = newsDTO.getPublishTime();
            if (publishTime == null) {
                news.setPublishTime(DateUtil.toDateTimeNullSafe(publishDate));
            } else {
                news.setPublishTime(publishDate.toDateTime(publishTime));
            }
        }
    }

    @Override
    protected NewsDTO toDTO(@Nonnull final News news) {
        return transformer.apply(news);
   }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN') or hasPrivilege('PUBLISH_FRONTPAGE_NEWS')")
    public Slice<NewsDTO> listNews(final Pageable pageRequest) {
        final Slice<News> news = newsRepository.findAllAsSlice(null, pageRequest);
        return transformer.apply(news, pageRequest);
    }

    // Public for frontpage news
    @Transactional(readOnly = true)
    public List<NewsDTO> listLatestNews() {
        final QNews NEWS = QNews.news;
        final BooleanExpression predicate = NEWS.publishTime.lt(DateUtil.now());
        final JpaSort sort = JpaSort.of(
                Sort.Direction.DESC,
                News_.publishTime);
        final PageRequest pageRequest = PageRequest.of(0, 2, sort);
        final Slice<News> news = newsRepository.findAllAsSlice(predicate, pageRequest);
        return transformer.apply(news, pageRequest).stream().collect(Collectors.toList());
    }
}
