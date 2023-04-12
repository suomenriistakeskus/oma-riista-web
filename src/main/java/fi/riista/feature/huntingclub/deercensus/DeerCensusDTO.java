package fi.riista.feature.huntingclub.deercensus;

import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.util.DtoUtil;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

public class DeerCensusDTO extends BaseEntityDTO<Long> {

    @Nonnull
    public static DeerCensusDTO transform(@Nonnull final DeerCensus entity) {
        final DeerCensusDTO dto = new DeerCensusDTO();
        DtoUtil.copyBaseFields(entity, dto);

        final HuntingClub huntingClub = entity.getHuntingClub();
        dto.setHuntingClubId(huntingClub.getId());

        dto.setObservationDate(entity.getObservationDate());
        dto.setYear(entity.getYear());

        dto.setWhiteTailDeers(entity.getWhiteTailDeers());
        dto.setWhiteTailDeersAdditionalInfo(entity.getWhiteTailDeersAdditionalInfo());

        dto.setRoeDeers(entity.getRoeDeers());
        dto.setRoeDeersAdditionalInfo(entity.getRoeDeersAdditionalInfo());

        dto.setFallowDeers(entity.getFallowDeers());
        dto.setFallowDeersAdditionalInfo(entity.getFallowDeersAdditionalInfo());

        return dto;
    }

    private Long id;

    private Integer rev;

    private long huntingClubId;

    @NotNull
    private LocalDate observationDate;

    private Integer year;

    @Min(0)
    private Integer whiteTailDeers;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String whiteTailDeersAdditionalInfo;

    @Min(0)
    private Integer roeDeers;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String roeDeersAdditionalInfo;

    @Min(0)
    private Integer fallowDeers;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String fallowDeersAdditionalInfo;

    private List<Long> attachmentIds;

    public DeerCensusDTO() {}

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

    public long getHuntingClubId() {
        return huntingClubId;
    }

    public void setHuntingClubId(long huntingClubId) {
        this.huntingClubId = huntingClubId;
    }

    public LocalDate getObservationDate() {
        return observationDate;
    }

    public void setObservationDate(LocalDate observationDate) {
        this.observationDate = observationDate;
    }

    public Integer getYear() { return year; }

    public void setYear(Integer year) { this.year = year; }

    public Integer getWhiteTailDeers() {
        return whiteTailDeers;
    }

    public void setWhiteTailDeers(Integer whiteTailDeers) {
        this.whiteTailDeers = whiteTailDeers;
    }

    public String getWhiteTailDeersAdditionalInfo() {
        return whiteTailDeersAdditionalInfo;
    }

    public void setWhiteTailDeersAdditionalInfo(String whiteTailDeersAdditionalInfo) {
        this.whiteTailDeersAdditionalInfo = whiteTailDeersAdditionalInfo;
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

    public void setRoeDeersAdditionalInfo(String roeDeersAdditionalInfo) {
        this.roeDeersAdditionalInfo = roeDeersAdditionalInfo;
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

    public void setFallowDeersAdditionalInfo(String fallowDeersAdditionalInfo) {
        this.fallowDeersAdditionalInfo = fallowDeersAdditionalInfo;
    }

    public List<Long> getAttachmentIds() {
        return attachmentIds;
    }

    public void setAttachmentIds(List<Long> attachmentIds) {
        this.attachmentIds = attachmentIds;
    }
}
