package fi.riista.feature.huntingclub.deercensus;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.deercensus.attachment.DeerCensusAttachment;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.LinkedList;
import java.util.List;

@Entity
@Access(AccessType.FIELD)
public class DeerCensus extends LifecycleEntity<Long> {

    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HuntingClub huntingClub;

    @Column
    private LocalDate observationDate;

    @Column(nullable = false)
    private int year;

    @Min(0)
    @Column
    private Integer whiteTailDeers;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "TEXT")
    private String whiteTailDeersAdditionalInfo;

    @Min(0)
    @Column
    private Integer roeDeers;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "TEXT")
    private String roeDeersAdditionalInfo;

    @Min(0)
    @Column
    private Integer fallowDeers;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "TEXT")
    private String fallowDeersAdditionalInfo;

    @OneToMany(mappedBy = "deerCensus")
    private List<DeerCensusAttachment> attachments = new LinkedList<>();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deer_census_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public HuntingClub getHuntingClub() {
        return huntingClub;
    }

    public void setHuntingClub(HuntingClub huntingClub) {
        this.huntingClub = huntingClub;
    }

    public LocalDate getObservationDate() {
        return observationDate;
    }

    public void setObservationDate(@NotNull LocalDate observationDate) {
        this.observationDate = observationDate;
        this.year = observationDate.getYear();
    }

    public int getYear() { return year; }

    public Integer getWhiteTailDeers() {
        return whiteTailDeers;
    }

    public void setWhiteTailDeers(Integer whiteTailDeers) {
        this.whiteTailDeers = whiteTailDeers;
    }

    public String getWhiteTailDeersAdditionalInfo() {
        return whiteTailDeersAdditionalInfo;
    }

    public void setWhiteTailDeersAdditionalInfo(String whiteTailDeerAdditionalInfo) {
        this.whiteTailDeersAdditionalInfo = whiteTailDeerAdditionalInfo;
    }

    public Integer getRoeDeers() {
        return roeDeers;
    }

    public void setRoeDeers(Integer roeDeers) {
        this.roeDeers = roeDeers;
    }

    public String getRoeDeersAdditionalInfo() {
        return roeDeersAdditionalInfo;
    }

    public void setRoeDeersAdditionalInfo(String roeDeerAdditionalInfo) {
        this.roeDeersAdditionalInfo = roeDeerAdditionalInfo;
    }

    public Integer getFallowDeers() {
        return fallowDeers;
    }

    public void setFallowDeers(Integer fallowDeers) {
        this.fallowDeers = fallowDeers;
    }

    public String getFallowDeersAdditionalInfo() {
        return fallowDeersAdditionalInfo;
    }

    public void setFallowDeersAdditionalInfo(String fallowDeerAdditionalInfo) {
        this.fallowDeersAdditionalInfo = fallowDeerAdditionalInfo;
    }

    public List<DeerCensusAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<DeerCensusAttachment> attachments) {
        this.attachments = attachments;
    }
}
