package fi.riista.util;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

public interface NumberGenerator {

    void reset();

    int nextInt();

    long nextLong();

    double nextDouble();

    default int nextIntBetween(final int closedLowerBound, final int closedUpperBound) {
        if (closedLowerBound > closedUpperBound) {
            throw new IllegalArgumentException(String.format(
                    "Illegal range given: [%d..%d]", closedLowerBound, closedUpperBound));
        }

        return closedLowerBound + nextInt() % (closedUpperBound - closedLowerBound + 1);
    }

    default int nextNonNegativeInt() {
        return nextNonNegativeIntAtMost(Integer.MAX_VALUE);
    }

    default int nextNonNegativeIntAtMost(final int closedUpperBound) {
        return nextIntBetween(0, closedUpperBound);
    }

    default int nextNonNegativeIntBelow(final int openUpperBound) {
        return nextNonNegativeIntAtMost(openUpperBound - 1);
    }

    default int nextPositiveInt() {
        return nextPositiveIntAtMost(Integer.MAX_VALUE);
    }

    default int nextPositiveIntAtMost(final int closedUpperBound) {
        return nextIntBetween(1, closedUpperBound);
    }

    default int nextPositiveIntBelow(final int openUpperBound) {
        return nextPositiveIntAtMost(openUpperBound - 1);
    }

    default int nextInt(final Range<Integer> range) {
        final int upperEndPoint = range.hasUpperBound()
                ? range.upperBoundType() == BoundType.CLOSED ? range.upperEndpoint() : range.upperEndpoint() - 1
                : Integer.MAX_VALUE;

        final int lowerEndPoint = range.hasLowerBound()
                ? range.lowerBoundType() == BoundType.CLOSED ? range.lowerEndpoint() : range.lowerEndpoint() + 1
                : Integer.MIN_VALUE;

        return nextIntBetween(lowerEndPoint, upperEndPoint);
    }

}
