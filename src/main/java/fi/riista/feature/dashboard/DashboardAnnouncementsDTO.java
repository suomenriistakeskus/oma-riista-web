package fi.riista.feature.dashboard;

public class DashboardAnnouncementsDTO {
    private long total;
    private long senderTypeModerator;
    private long senderTypeCoordinator;
    private long senderTypeClub;

    public long getTotal() {
        return total;
    }

    public void setTotal(final long total) {
        this.total = total;
    }

    public long getSenderTypeModerator() {
        return senderTypeModerator;
    }

    public void setSenderTypeModerator(final long senderTypeModerator) {
        this.senderTypeModerator = senderTypeModerator;
    }

    public long getSenderTypeCoordinator() {
        return senderTypeCoordinator;
    }

    public void setSenderTypeCoordinator(final long senderTypeCoordinator) {
        this.senderTypeCoordinator = senderTypeCoordinator;
    }

    public long getSenderTypeClub() {
        return senderTypeClub;
    }

    public void setSenderTypeClub(final long senderTypeClub) {
        this.senderTypeClub = senderTypeClub;
    }
}
