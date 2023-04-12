package fi.riista.feature.gamediary.mobile.srva;

import fi.riista.feature.gamediary.srva.SrvaEvent;
import fi.riista.feature.gamediary.srva.SrvaEventDTOTransformerBase;
import fi.riista.feature.gamediary.srva.SrvaEventSpecVersion;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public abstract class MobileSrvaEventDTOTransformerBase extends SrvaEventDTOTransformerBase<MobileSrvaEventDTO> {

    // Transactional propagation not mandated since entity associations are not traversed.
    @Transactional(readOnly = true)
    @Nullable
    public List<MobileSrvaEventDTO> apply(@Nullable final List<SrvaEvent> list,
                                          @Nonnull final SrvaEventSpecVersion specVersion) {

        return list == null ? null : transform(list, specVersion);
    }

    // Transactional propagation not mandated since entity associations are not traversed.
    @Transactional(readOnly = true)
    @Nullable
    public MobileSrvaEventDTO apply(@Nullable final SrvaEvent srvaEvent,
                                    @Nonnull final SrvaEventSpecVersion specVersion) {

        if (srvaEvent == null) {
            return null;
        }

        final List<MobileSrvaEventDTO> singletonList = apply(Collections.singletonList(srvaEvent), specVersion);

        if (singletonList.size() != 1) {
            throw new IllegalStateException(
                    "Expected list containing exactly one srva event but has: " + singletonList.size());
        }

        return singletonList.get(0);
    }

    @Override
    protected List<MobileSrvaEventDTO> transform(@Nonnull final List<SrvaEvent> list) {
        throw new UnsupportedOperationException("No transformation without srvaEventSpecVersion supported");
    }

    @Nonnull
    protected abstract List<MobileSrvaEventDTO> transform(@Nonnull final List<SrvaEvent> srvaEvents,
                                                          @Nonnull final SrvaEventSpecVersion srvaEventSpecVersion);
}
