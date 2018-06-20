package fi.riista.feature.dashboard;

public class DashboardHarvestReportDTO {
    private String rka;
    private String species;
    private int userCount;
    private int moderatorCount;
    private int reportsTotal;
    private boolean permit;
    private boolean season;
    private String permitType;
    private String permitTypeCode;

    public String getRka() {
        return rka;
    }

    public void setRka(String rka) {
        this.rka = rka;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public int getUserCount() {
        return userCount;
    }

    public void setUserCount(int userCount) {
        this.userCount = userCount;
    }

    public int getModeratorCount() {
        return moderatorCount;
    }

    public void setModeratorCount(int moderatorCount) {
        this.moderatorCount = moderatorCount;
    }

    public int getReportsTotal() {
        return reportsTotal;
    }

    public void setReportsTotal(int total) {
        this.reportsTotal = total;
    }

    public boolean isPermit() {
        return permit;
    }

    public void setPermit(boolean permit) {
        this.permit = permit;
    }

    public boolean isSeason() {
        return season;
    }

    public void setSeason(boolean season) {
        this.season = season;
    }

    public void setPermitType(String permitType) {
        this.permitType = permitType;
    }

    public String getPermitType() {
        return permitType;
    }

    public void setPermitTypeCode(String permitTypeCode) {
        this.permitTypeCode = permitTypeCode;
    }

    public String getPermitTypeCode() {
        return permitTypeCode;
    }
}
