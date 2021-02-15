package fi.riista.feature.dashboard;

public class DashboardShootingTestDTO {
    private long countOfTotalAttempts;
    private long countOfQualifiedAttempts;
    private long countOfQualifiedParticipants;
    private long countOfClosedEvents;
    private long countOfActiveRhy;
    private long countOfTotalRhy;

    public long getCountOfTotalAttempts() {
        return countOfTotalAttempts;
    }

    public void setCountOfTotalAttempts(final long countOfTotalAttempts) {
        this.countOfTotalAttempts = countOfTotalAttempts;
    }

    public long getCountOfQualifiedAttempts() {
        return countOfQualifiedAttempts;
    }

    public void setCountOfQualifiedAttempts(final long countOfQualifiedAttempts) {
        this.countOfQualifiedAttempts = countOfQualifiedAttempts;
    }

    public long getCountOfQualifiedParticipants() {
        return countOfQualifiedParticipants;
    }

    public void setCountOfQualifiedParticipants(final long countOfQualifiedParticipants) {
        this.countOfQualifiedParticipants = countOfQualifiedParticipants;
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
