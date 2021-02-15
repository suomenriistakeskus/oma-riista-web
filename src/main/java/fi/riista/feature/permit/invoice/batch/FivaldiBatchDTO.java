package fi.riista.feature.permit.invoice.batch;

import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.util.DtoUtil;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

public class FivaldiBatchDTO extends BaseEntityDTO<Long> {

    public static FivaldiBatchDTO create(@Nonnull final PermitDecisionInvoiceBatch batch) {
        requireNonNull(batch);

        final FivaldiBatchDTO dto = new FivaldiBatchDTO();
        DtoUtil.copyBaseFields(batch, dto);
        dto.setCreationTime(batch.getCreationTime());
        return dto;
    }

    private Long id;
    private Integer rev;

    private DateTime creationTime;

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

    public DateTime getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(final DateTime creationTime) {
        this.creationTime = creationTime;
    }
}
