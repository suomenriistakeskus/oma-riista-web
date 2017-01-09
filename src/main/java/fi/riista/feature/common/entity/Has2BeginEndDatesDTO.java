package fi.riista.feature.common.entity;

import org.joda.time.LocalDate;

import javax.annotation.Nullable;

public class Has2BeginEndDatesDTO implements Has2BeginEndDates {

    private LocalDate beginDate;
    private LocalDate endDate;

    private LocalDate beginDate2;
    private LocalDate endDate2;

    public Has2BeginEndDatesDTO() {
    }

    public Has2BeginEndDatesDTO(
            @Nullable final LocalDate beginDate,
            @Nullable final LocalDate endDate,
            @Nullable final LocalDate beginDate2,
            @Nullable final LocalDate endDate2) {

        setDates(beginDate, endDate, beginDate2, endDate2);
    }

    @Override
    public LocalDate getBeginDate() {
        return beginDate;
    }

    @Override
    public void setBeginDate(final LocalDate beginDate) {
        this.beginDate = beginDate;
    }

    @Override
    public LocalDate getEndDate() {
        return endDate;
    }

    @Override
    public void setEndDate(final LocalDate endDate) {
        this.endDate = endDate;
    }

    @Override
    public LocalDate getBeginDate2() {
        return beginDate2;
    }

    @Override
    public void setBeginDate2(final LocalDate beginDate2) {
        this.beginDate2 = beginDate2;
    }

    @Override
    public LocalDate getEndDate2() {
        return endDate2;
    }

    @Override
    public void setEndDate2(final LocalDate endDate2) {
        this.endDate2 = endDate2;
    }

}
