package fi.riista.feature.news;

import fi.riista.util.DateUtil;
import fi.riista.util.DtoUtil;
import fi.riista.util.ListTransformer;
import org.joda.time.LocalDateTime;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class NewsDTOTransformer extends ListTransformer<News, NewsDTO> {

    @Nonnull
    @Override
    protected List<NewsDTO> transform(@Nonnull final List<News> list) {
        if (list.isEmpty()) {
            return Collections.emptyList();
        }

        return list.stream()
                .map(news -> {
                    final NewsDTO dto = new NewsDTO();
                    DtoUtil.copyBaseFields(news, dto);

                    dto.setTitleFi(news.getTitleFi());
                    dto.setTextFi(news.getTextFi());
                    dto.setLinkFi(news.getLinkFi());

                    dto.setTitleSv(news.getTitleSv());
                    dto.setTextSv(news.getTextSv());
                    dto.setLinkSv(news.getLinkSv());

                    final LocalDateTime publishDateTime = DateUtil.toLocalDateTimeNullSafe(news.getPublishTime());
                    if (publishDateTime != null) {
                        dto.setPublishDate(publishDateTime.toLocalDate());
                        dto.setPublishTime(publishDateTime.toLocalTime());
                    }

                    return dto;
                }).collect(Collectors.toList());
    }
}
