package fi.riista.feature.dashboard;

public class DashboardClubsDTO {
    private long allClubs;
    private long userCreatedClubs;
    private long clubAreas;
    private long clubPermitAreas;
    private long permitApplications;

    public long getAllClubs() {
        return allClubs;
    }

    public void setAllClubs(long allClubs) {
        this.allClubs = allClubs;
    }

    public long getUserCreatedClubs() {
        return userCreatedClubs;
    }

    public void setUserCreatedClubs(long userCreatedClubs) {
        this.userCreatedClubs = userCreatedClubs;
    }

    public long getClubAreas() {
        return clubAreas;
    }

    public void setClubAreas(long clubAreas) {
        this.clubAreas = clubAreas;
    }

    public long getClubPermitAreas() {
        return clubPermitAreas;
    }

    public void setClubPermitAreas(long clubPermitAreas) {
        this.clubPermitAreas = clubPermitAreas;
    }

    public long getPermitApplications() {
        return permitApplications;
    }

    public void setPermitApplications(long permitApplications) {
        this.permitApplications = permitApplications;
    }
}
