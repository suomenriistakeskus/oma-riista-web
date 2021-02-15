package fi.riista.feature.dashboard;

public class DashboardDeerPilotMemberDTO {
    private final int age;
    private final int observationCount;
    private final int harvestCount;

    public DashboardDeerPilotMemberDTO(final int age, final int observationCount, final int harvestCount) {
        this.age = age;
        this.observationCount = observationCount;
        this.harvestCount = harvestCount;
    }

    public int getAge() {
        return age;
    }

    public int getObservationCount() {
        return observationCount;
    }

    public int getHarvestCount() {
        return harvestCount;
    }
}
