package fi.riista.feature.account.todo;

public class AccountTodoCountDTO {
    private long harvests;
    private long permits;
    private long harvestsAndPermitsTotal;

    private long invitations;

    public AccountTodoCountDTO(long harvests, long permits, long invitations) {
        this.harvests = harvests;
        this.permits = permits;
        this.harvestsAndPermitsTotal = harvests + permits;
        this.invitations = invitations;
    }

    public long getHarvests() {
        return harvests;
    }

    public long getPermits() {
        return permits;
    }

    public long getHarvestsAndPermitsTotal() {
        return harvestsAndPermitsTotal;
    }

    public long getInvitations() {
        return invitations;
    }
}

