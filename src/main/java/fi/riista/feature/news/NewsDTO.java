package fi.riista.feature.news;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fi.riista.config.jackson.LocalTimeToStringSerializer;
import fi.riista.feature.common.dto.BaseEntityDTO;
import org.hibernate.validator.constraints.SafeHtml;
import org.hibernate.validator.constraints.URL;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class NewsDTO extends BaseEntityDTO<Long> {

    private Long id;
    private Integer rev;

    @NotNull
    @Size(min = 1)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String titleFi;

    @NotNull
    @Size(min = 1)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String textFi;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @URL
    private String linkFi;

    @NotNull
    @Size(min = 1)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String titleSv;

    @NotNull
    @Size(min = 1)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String textSv;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @URL
    private String linkSv;

    @NotNull
    private LocalDate publishDate;

    @NotNull
    @JsonSerialize(using = LocalTimeToStringSerializer.class)
    private LocalTime publishTime;

    public NewsDTO() {
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return rev;
    }

    @Override
    public void setRev(final Integer rev) {
        this.rev = rev;
    }

    public String getTitleFi() {
        return titleFi;
    }

    public void setTitleFi(final String titleFi) {
        this.titleFi = titleFi;
    }

    public String getTextFi() {
        return textFi;
    }

    public void setTextFi(final String textFi) {
        this.textFi = textFi;
    }

    public String getLinkFi() {
        return linkFi;
    }

    public void setLinkFi(final String linkFi) {
        this.linkFi = linkFi;
    }

    public String getTitleSv() {
        return titleSv;
    }

    public void setTitleSv(final String titleSv) {
        this.titleSv = titleSv;
    }

    public String getTextSv() {
        return textSv;
    }

    public void setTextSv(final String textSv) {
        this.textSv = textSv;
    }

    public String getLinkSv() {
        return linkSv;
    }

    public void setLinkSv(final String linkSv) {
        this.linkSv = linkSv;
    }

    public LocalDate getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(final LocalDate publishDate) {
        this.publishDate = publishDate;
    }

    public LocalTime getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(final LocalTime publishTime) {
        this.publishTime = publishTime;
    }
}
