package fi.riista.feature.huntingclub.permit.stats;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import fi.riista.util.LocalisedString;

import java.util.Map;

public class MoosePermitStatisticsDTO {
    private String permitNumber;
    private LocalisedString permitHolderLocalisedString;
    private String permitHolderOfficialCode;
    private boolean huntingFinished;
    private double permitAmount;
    private MoosePermitStatisticsCount harvestCount;

    // Builder methods

    public MoosePermitStatisticsDTO withPermitNumber(final String permitNumber) {
        this.permitNumber = permitNumber;
        return this;
    }

    public MoosePermitStatisticsDTO withPermitHolderName(final LocalisedString permitHolderName) {
        this.permitHolderLocalisedString = permitHolderName;
        return this;
    }

    public MoosePermitStatisticsDTO withPermitHolderOfficialCode(final String permitHolderOfficialCode) {
        this.permitHolderOfficialCode = permitHolderOfficialCode;
        return this;
    }

    public MoosePermitStatisticsDTO withHuntingFinished(final boolean huntingFinished) {
        this.huntingFinished = huntingFinished;
        return this;
    }

    public MoosePermitStatisticsDTO withPermitAmount(final double permitAmount) {
        this.permitAmount = permitAmount;
        return this;
    }

    public MoosePermitStatisticsDTO withHarvestCount(final MoosePermitStatisticsCount harvestCount) {
        this.harvestCount = harvestCount;
        return this;
    }

    // Json accessor

    @JsonGetter
    public Map<String, String> getPermitHolder() {
        return permitHolderLocalisedString != null ? permitHolderLocalisedString.asMap() : null;
    }

    // Accessors

    public String getPermitNumber() {
        return permitNumber;
    }

    @JsonIgnore
    public LocalisedString getPermitHolderLocalisedString() {
        return permitHolderLocalisedString;
    }

    public String getPermitHolderOfficialCode() {
        return permitHolderOfficialCode;
    }

    public boolean isHuntingFinished() {
        return huntingFinished;
    }

    public double getPermitAmount() {
        return permitAmount;
    }

    public MoosePermitStatisticsCount getHarvestCount() {
        return harvestCount;
    }
}
