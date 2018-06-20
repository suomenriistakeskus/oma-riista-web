package fi.riista.feature.dashboard;

public class DashboardUsersDTO {
    private long countNormalUser;
    private long countPerson;

    private long countNormalUserWithOccupationAndPassword;
    private long countAllPeopleWithOccupation;

    private long countModeratorWithPassword;
    private long countAllModerators;

    private long countRHYToiminnanohjaajaWithPassword;
    private long countAllRHYToiminnanohjaaja;

    public long getCountNormalUser() {
        return countNormalUser;
    }

    public void setCountNormalUser(long countNormalUser) {
        this.countNormalUser = countNormalUser;
    }

    public long getCountPerson() {
        return countPerson;
    }

    public void setCountPerson(long countPerson) {
        this.countPerson = countPerson;
    }

    public long getCountNormalUserWithOccupationAndPassword() {
        return countNormalUserWithOccupationAndPassword;
    }

    public void setCountNormalUserWithOccupationAndPassword(long countNormalUserWithOccupationAndPassword) {
        this.countNormalUserWithOccupationAndPassword = countNormalUserWithOccupationAndPassword;
    }

    public long getCountAllPeopleWithOccupation() {
        return countAllPeopleWithOccupation;
    }

    public void setCountAllPeopleWithOccupation(long countAllPeopleWithOccupation) {
        this.countAllPeopleWithOccupation = countAllPeopleWithOccupation;
    }

    public long getCountModeratorWithPassword() {
        return countModeratorWithPassword;
    }

    public void setCountModeratorWithPassword(long countModeratorWithPassword) {
        this.countModeratorWithPassword = countModeratorWithPassword;
    }

    public long getCountAllModerators() {
        return countAllModerators;
    }

    public void setCountAllModerators(long countAllModerators) {
        this.countAllModerators = countAllModerators;
    }

    public long getCountRHYToiminnanohjaajaWithPassword() {
        return countRHYToiminnanohjaajaWithPassword;
    }

    public void setCountRHYToiminnanohjaajaWithPassword(long countRHYToiminnanohjaajaWithPassword) {
        this.countRHYToiminnanohjaajaWithPassword = countRHYToiminnanohjaajaWithPassword;
    }

    public long getCountAllRHYToiminnanohjaaja() {
        return countAllRHYToiminnanohjaaja;
    }

    public void setCountAllRHYToiminnanohjaaja(long countAllRHYToiminnanohjaaja) {
        this.countAllRHYToiminnanohjaaja = countAllRHYToiminnanohjaaja;
    }
}
