package fi.riista.feature.huntingclub.moosedatacard;

import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.util.DateUtil;
import fi.riista.util.DtoUtil;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MooseDataCardImportDTO extends BaseEntityDTO<Long> {

    public static MooseDataCardImportDTO from(
            @Nonnull final MooseDataCardImport entity, @Nonnull final List<String> messages) {

        Objects.requireNonNull(entity, "entity must not be null");
        Objects.requireNonNull(messages, "messages must not be null");

        final MooseDataCardImportDTO dto = new MooseDataCardImportDTO();
        DtoUtil.copyBaseFields(entity, dto);

        dto.setHuntingGroupId(entity.getGroup().getId());
        dto.setImportTimestamp(DateUtil.toLocalDateTimeNullSafe(entity.getCreationTime()));
        dto.setFilenameTimestamp(DateUtil.toLocalDateTimeNullSafe(entity.getFilenameTimestamp()));
        dto.setReportingPeriodBeginDate(entity.getBeginDate());
        dto.setReportingPeriodEndDate(entity.getEndDate());
        dto.setRevocationTimestamp(DateUtil.toLocalDateTimeNullSafe(entity.getDeletionTime()));

        dto.getMessages().addAll(messages);

        return dto;
    }

    private Long id;
    private Integer rev;

    private long huntingGroupId;

    private LocalDateTime importTimestamp;

    private LocalDateTime filenameTimestamp;

    private LocalDate reportingPeriodBeginDate;

    private LocalDate reportingPeriodEndDate;

    private LocalDateTime revocationTimestamp;

    private final List<String> messages = new ArrayList<>();

    // Accessors -->

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

    public long getHuntingGroupId() {
        return huntingGroupId;
    }

    public void setHuntingGroupId(final long huntingGroupId) {
        this.huntingGroupId = huntingGroupId;
    }

    public LocalDateTime getImportTimestamp() {
        return importTimestamp;
    }

    public void setImportTimestamp(final LocalDateTime importTimestamp) {
        this.importTimestamp = importTimestamp;
    }

    public LocalDateTime getFilenameTimestamp() {
        return filenameTimestamp;
    }

    public void setFilenameTimestamp(final LocalDateTime fileTimestamp) {
        this.filenameTimestamp = fileTimestamp;
    }

    public LocalDate getReportingPeriodBeginDate() {
        return reportingPeriodBeginDate;
    }

    public void setReportingPeriodBeginDate(final LocalDate reportingPeriodBeginDate) {
        this.reportingPeriodBeginDate = reportingPeriodBeginDate;
    }

    public LocalDate getReportingPeriodEndDate() {
        return reportingPeriodEndDate;
    }

    public void setReportingPeriodEndDate(final LocalDate reportingPeriodEndDate) {
        this.reportingPeriodEndDate = reportingPeriodEndDate;
    }

    public LocalDateTime getRevocationTimestamp() {
        return revocationTimestamp;
    }

    public void setRevocationTimestamp(final LocalDateTime deletionTimestamp) {
        this.revocationTimestamp = deletionTimestamp;
    }

    public List<String> getMessages() {
        return messages;
    }

}
