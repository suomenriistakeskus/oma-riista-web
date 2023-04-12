package fi.riista.api.club;

import org.joda.time.LocalDate;

import javax.validation.constraints.AssertTrue;

public class ClubHarvestSummaryRequestDTO {

    private long clubId;
    private LocalDate begin;
    private LocalDate end;

    public ClubHarvestSummaryRequestDTO() {
    }

    public ClubHarvestSummaryRequestDTO(final long clubId, final LocalDate begin, final LocalDate end) {
        this.clubId = clubId;
        this.begin = begin;
        this.end = end;
    }

    public long getClubId() {
        return clubId;
    }

    public void setClubId(final long clubId) {
        this.clubId = clubId;
    }

    public LocalDate getBegin() {
        return begin;
    }

    public void setBegin(final LocalDate begin) {
        this.begin = begin;
    }

    public LocalDate getEnd() {
        return end;
    }

    public void setEnd(final LocalDate end) {
        this.end = end;
    }

    @AssertTrue
    public boolean isPeriodValid() {
        // End date should not be before begin and period should be one year at most
        return !this.begin.isAfter(this.end)
                && this.begin.plusYears(1).isAfter(this.end);
    }
}
