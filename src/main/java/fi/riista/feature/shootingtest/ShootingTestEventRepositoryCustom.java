package fi.riista.feature.shootingtest;

import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import org.joda.time.LocalDate;

public interface ShootingTestEventRepositoryCustom {

    /**
     * Gives a number of shooting test events that are either (1) not opened,
     * (2) not closed, or (3) closed but no participants are registered.
     *
     * @param rhyId
     *            the RHY whose shooting test events are searched
     * @param beginDate
     *            the begin date of search interval
     * @param endDate
     *            the end date of search interval
     */
    long countShootingTestEventsNotProperlyFinished(Riistanhoitoyhdistys rhy, LocalDate beginDate, LocalDate endDate);

}
