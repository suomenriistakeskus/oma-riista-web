package fi.riista.feature.organization.jht;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import fi.riista.util.DateUtil;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.joda.time.Period;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

/**
 * Toimikausipäivämäärät oletuksena:
 *
 * Alkupäivämäärä on sen hetkinen päivämäärä paitsi vuonna 2016 ennen 1.8.2016 asetetaan 1.8.2016
 *
 * Loppupäivämäärä on aina 31.7. sitä vuotta, joka on enemmän kuin neljä,
 * mutta vähemmän tai yhtäsuuri kuin viisi vuotta ko. alkupäivämäärästä lähtien.
 *
 * Eli: jos nimitys esim. 10.8.2016-31.7.2017 välisenä aikana niin nimitys päättyy aina 31.7.2021.
 * Jos nimityspäivä 1.8.2017-31.7.2018, nimitys päättyy aina 31.7.2022.
 */
public class JHTPeriod {
    @NotNull
    private final LocalDate beginDate;

    @NotNull
    private final LocalDate endDate;

    @JsonCreator
    public JHTPeriod(final @JsonProperty("beginDate") LocalDate beginDate,
                     final @JsonProperty("endDate") LocalDate endDate) {
        this.beginDate = beginDate;
        this.endDate = endDate;
    }

    @AssertTrue
    public boolean isPeriodValid() {
        if (this.beginDate == null || this.endDate == null) {
            return true;
        }

        final DateTime start = this.beginDate.toDateTimeAtStartOfDay();
        final DateTime end = this.endDate.toDateTimeAtStartOfDay();

        final Period occupationPeriod = new Duration(start, end).toPeriodFrom(start);

        return occupationPeriod.getYears() == 4 || (occupationPeriod.getYears() == 5
                && occupationPeriod.getMonths() == 0
                && occupationPeriod.getDays() == 0);
    }

    @AssertTrue
    public boolean isEndDateValid() {
        return this.endDate != null && this.endDate.getMonthOfYear() == 7 && this.endDate.getDayOfMonth() == 31;
    }

    public JHTPeriod(final LocalDate today) {
        if (today.isBefore(DateUtil.huntingYearBeginDate(2016))) {
            this.beginDate = DateUtil.huntingYearBeginDate(2016);
            this.endDate = DateUtil.huntingYearEndDate(2020);

        } else {
            final int huntingYear = DateUtil.huntingYearContaining(today);

            this.beginDate = today;
            this.endDate = DateUtil.huntingYearEndDate(huntingYear + 4);
        }
    }

    public LocalDate getBeginDate() {
        return beginDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}
