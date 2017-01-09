package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.gamediary.srva.SrvaEventSpecVersion;
import fi.riista.feature.gamediary.srva.SrvaEventDTOBase;
import fi.riista.feature.gamediary.srva.SrvaEvent;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import java.util.Objects;

public class MobileSrvaEventDTO extends SrvaEventDTOBase {

    public static MobileSrvaEventDTO create(
            @Nonnull final SrvaEvent srvaEvent, @Nonnull final SrvaEventSpecVersion srvaEventSpecVersion) {

        final MobileSrvaEventDTO dto = createFromNonCollectionFields(srvaEvent, builder());

        dto.setMobileClientRefId(srvaEvent.getMobileClientRefId());
        dto.setSrvaEventSpecVersion(Objects.requireNonNull(srvaEventSpecVersion, "srvaEventSpecVersion is null"));

        // Author, Gamespecies, Methods, Specimens and imageIds are set by DTOtransformer
        return dto;
    }

    private Long mobileClientRefId;

    @NotNull
    private SrvaEventSpecVersion srvaEventSpecVersion;

    // Accessors -->

    public Long getMobileClientRefId() {
        return mobileClientRefId;
    }

    public void setMobileClientRefId(final Long mobileClientRefId) {
        this.mobileClientRefId = mobileClientRefId;
    }

    public SrvaEventSpecVersion getSrvaEventSpecVersion() {
        return srvaEventSpecVersion;
    }

    public void setSrvaEventSpecVersion(final SrvaEventSpecVersion srvaEventSpecVersion) {
        this.srvaEventSpecVersion = srvaEventSpecVersion;
    }

    // Builder -->

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends SrvaEventDTOBase.Builder<MobileSrvaEventDTO, Builder> {
        @Override
        protected MobileSrvaEventDTO createDTO() {
            return new MobileSrvaEventDTO();
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

}
