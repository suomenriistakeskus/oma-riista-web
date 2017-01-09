package fi.riista.feature.huntingclub.permit.summary;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AreaSizeAssertionHelper {

    public static void assertGivenAreaSizeToPermitAreaSize(
            @Nonnull final Integer permitAreaSize,
            @Nullable final Integer totalAreaSize,
            @Nullable final Integer effectiveAreaSize) {

        Preconditions.checkNotNull(permitAreaSize, "permitAreaSize should be always non-null");

        if (totalAreaSize != null && totalAreaSize > permitAreaSize) {
            throw new TotalAreaSizeTooBigException(
                    String.format("totalAreaSize:%d is greater than permitAreaSize:%d", totalAreaSize, permitAreaSize));
        }

        if (effectiveAreaSize != null && effectiveAreaSize > permitAreaSize) {
            throw new EffectiveAreaSizeTooBigException(
                    String.format("effectiveAreaSize:%d is greater than permitAreaSize:%d", effectiveAreaSize, permitAreaSize));
        }

        if (totalAreaSize != null && effectiveAreaSize != null && totalAreaSize < effectiveAreaSize) {
            throw new TotalAreaSizeSmallerThanEffectiveAreaSizeException(
                    String.format("totalAreaSize:%d is smaller than effectiveAreaSize:%d ", totalAreaSize, effectiveAreaSize));
        }
    }

    public static class TotalAreaSizeTooBigException extends RuntimeException {
        public TotalAreaSizeTooBigException(String msg) {
            super(msg);
        }
    }

    public static class EffectiveAreaSizeTooBigException extends RuntimeException {
        public EffectiveAreaSizeTooBigException(String msg) {
            super(msg);
        }
    }

    public static class TotalAreaSizeSmallerThanEffectiveAreaSizeException extends RuntimeException {
        public TotalAreaSizeSmallerThanEffectiveAreaSizeException(String message) {
            super(message);
        }
    }
}
