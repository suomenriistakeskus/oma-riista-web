package fi.riista.feature.organization.rhy.taxation;

import fi.riista.feature.common.dto.BaseEntityDTO;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

public class HarvestTaxationReportAttachmentDTO extends BaseEntityDTO<Long> {

    public enum State {
        DRAFT,
        CONFIRMED
    }

    private Long id;

    private Integer rev;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String filename;

    public static HarvestTaxationReportAttachmentDTO create(@Nonnull final HarvestTaxationReportAttachment entity) {
        requireNonNull(entity);

        final HarvestTaxationReportAttachmentDTO dto = new HarvestTaxationReportAttachmentDTO();
        dto.setFilename(entity.getFileMetadata().getOriginalFilename());
        dto.setRev(entity.getConsistencyVersion());
        dto.setId(entity.getId());

        return dto;
    }

    public HarvestTaxationReportAttachmentDTO() {
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

    public void setId(final long id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(final String filename) {
        this.filename = filename;
    }
}
