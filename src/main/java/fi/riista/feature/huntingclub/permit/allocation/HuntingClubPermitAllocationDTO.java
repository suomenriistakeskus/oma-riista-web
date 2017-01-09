package fi.riista.feature.huntingclub.permit.allocation;

import java.util.Map;
import java.util.Optional;

public class HuntingClubPermitAllocationDTO {

    private long huntingClubId;
    private Map<String, String> huntingClubName;

    private Float total;
    private Integer adultMales;
    private Integer adultFemales;
    private Integer young;

    public long getHuntingClubId() {
        return huntingClubId;
    }

    public void setHuntingClubId(final long huntingClubId) {
        this.huntingClubId = huntingClubId;
    }

    public Map<String, String> getHuntingClubName() {
        return huntingClubName;
    }

    public void setHuntingClubName(final Map<String, String> huntingClubName) {
        this.huntingClubName = huntingClubName;
    }

    public Float getTotal() {
        return Optional.ofNullable(total).orElse(0f);
    }

    public void setTotal(final Float total) {
        this.total = total;
    }

    public Integer getAdultMales() {
        return Optional.ofNullable(adultMales).orElse(0);
    }

    public void setAdultMales(final Integer adultMales) {
        this.adultMales = adultMales;
    }

    public Integer getAdultFemales() {
        return Optional.ofNullable(adultFemales).orElse(0);
    }

    public void setAdultFemales(final Integer adultFemales) {
        this.adultFemales = adultFemales;
    }

    public Integer getYoung() {
        return Optional.ofNullable(young).orElse(0);
    }

    public void setYoung(final Integer young) {
        this.young = young;
    }
}
