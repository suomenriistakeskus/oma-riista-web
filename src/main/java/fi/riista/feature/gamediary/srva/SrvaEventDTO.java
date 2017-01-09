package fi.riista.feature.gamediary.srva;

import javax.annotation.Nonnull;

public class SrvaEventDTO extends SrvaEventDTOBase {

    public static SrvaEventDTO create(@Nonnull final SrvaEvent entity) {
        return createFromNonCollectionFields(entity, builder());
    }

    // Builder -->

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends SrvaEventDTOBase.Builder<SrvaEventDTO, Builder> {
        @Override
        protected SrvaEventDTO createDTO() {
            return new SrvaEventDTO();
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

}
