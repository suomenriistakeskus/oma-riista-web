package fi.riista.feature.account.todo;

public class AccountSrvaTodoCountDTO {
    private long unfinishedSrvaEvents;

    public AccountSrvaTodoCountDTO(long unfinishedSrvaEvents) {
        this.unfinishedSrvaEvents = unfinishedSrvaEvents;
    }

    public long getUnfinishedSrvaEvents() {
        return unfinishedSrvaEvents;
    }
}
