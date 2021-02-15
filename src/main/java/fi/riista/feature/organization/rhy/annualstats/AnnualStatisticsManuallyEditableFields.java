package fi.riista.feature.organization.rhy.annualstats;

import fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup;
import fi.riista.util.DateUtil;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

public interface AnnualStatisticsManuallyEditableFields<T> {

    AnnualStatisticGroup getGroup();

    DateTime getLastModified();

    void setLastModified(DateTime timestamp);

    boolean isEqualTo(T that);

    void assignFrom(T that);

    /**
     * @return a boolean value indicating if changes were detected and copied.
     */
    default boolean merge(@Nonnull final T that) {
        requireNonNull(that);

        if (isEqualTo(that)) {
            return false;
        }

        assignFrom(that);
        setLastModified(DateUtil.now());
        return true;
    }
}
