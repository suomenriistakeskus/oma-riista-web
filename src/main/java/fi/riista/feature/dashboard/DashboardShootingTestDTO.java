package fi.riista.feature.dashboard;

public class DashboardShootingTestDTO {
    private long countOfTotalAttempts;
    private long countOfClosedEvents;
    private long countOfActiveRhy;
    private long countOfTotalRhy;

    public long getCountOfTotalAttempts() {
        return countOfTotalAttempts;
    }

    public void setCountOfTotalAttempts(final long countOfTotalAttempts) {
        this.countOfTotalAttempts = countOfTotalAttempts;
    }

    public long getCountOfClosedEvents() {
        return countOfClosedEvents;
    }

    public void setCountOfClosedEvents(final long countOfClosedEvents) {
        this.countOfClosedEvents = countOfClosedEvents;
    }

    public long getCountOfActiveRhy() {
        return countOfActiveRhy;
    }

    public void setCountOfActiveRhy(final long countOfActiveRhy) {
        this.countOfActiveRhy = countOfActiveRhy;
    }

    public long getCountOfTotalRhy() {
        return countOfTotalRhy;
    }

    public void setCountOfTotalRhy(final long countOfTotalRhy) {
        this.countOfTotalRhy = countOfTotalRhy;
    }
}
