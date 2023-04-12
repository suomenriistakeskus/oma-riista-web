package fi.riista.feature.news;

import fi.riista.feature.common.entity.LifecycleEntity;
import org.hibernate.validator.constraints.SafeHtml;
import org.hibernate.validator.constraints.URL;
import org.joda.time.DateTime;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Entity
@Access(value = AccessType.FIELD)
public class News extends LifecycleEntity<Long> {

    public static final String ID_COLUMN_NAME = "news_id";

    private Long id;

    @NotNull
    @Column(columnDefinition = "TEXT", nullable = false)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String titleFi;

    @NotNull
    @Column(columnDefinition = "TEXT", nullable = false)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String textFi;

    @Column(columnDefinition = "TEXT")
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @URL
    private String linkFi;

    @NotNull
    @Column(columnDefinition = "TEXT", nullable = false)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String titleSv;

    @NotNull
    @Column(columnDefinition = "TEXT", nullable = false)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String textSv;

    @Column(columnDefinition = "TEXT")
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @URL
    private String linkSv;

    @NotNull
    @Column(nullable = false)
    private DateTime publishTime;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = ID_COLUMN_NAME, nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
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

    public DateTime getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(final DateTime publishDate) {
        this.publishTime = publishDate;
    }
}
