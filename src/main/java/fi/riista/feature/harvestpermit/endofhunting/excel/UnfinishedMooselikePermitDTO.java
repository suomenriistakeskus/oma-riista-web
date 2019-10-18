package fi.riista.feature.harvestpermit.endofhunting.excel;

import fi.riista.feature.organization.person.PersonContactInfoDTO;
import fi.riista.util.LocalisedString;

import java.io.Serializable;

public class UnfinishedMooselikePermitDTO implements Serializable {

    private long speciesAmountId;

    private long permitId;
    private String permitNumber;

    private LocalisedString rhyName;

    private int gameSpeciesCode;
    private LocalisedString speciesName;

    private String permitHolderCustomerNumber;
    private String permitHolderName;

    private PersonContactInfoDTO originalContactPerson;

    public long getSpeciesAmountId() {
        return speciesAmountId;
    }

    public void setSpeciesAmountId(final long speciesAmountId) {
        this.speciesAmountId = speciesAmountId;
    }

    public long getPermitId() {
        return permitId;
    }

    public void setPermitId(final long permitId) {
        this.permitId = permitId;
    }

    public String getPermitNumber() {
        return permitNumber;
    }

    public void setPermitNumber(final String permitNumber) {
        this.permitNumber = permitNumber;
    }

    public LocalisedString getRhyName() {
        return rhyName;
    }

    public void setRhyName(final LocalisedString rhyName) {
        this.rhyName = rhyName;
    }

    public int getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public void setGameSpeciesCode(final int gameSpeciesCode) {
        this.gameSpeciesCode = gameSpeciesCode;
    }

    public LocalisedString getSpeciesName() {
        return speciesName;
    }

    public void setSpeciesName(final LocalisedString speciesName) {
        this.speciesName = speciesName;
    }

    public String getPermitHolderCustomerNumber() {
        return permitHolderCustomerNumber;
    }

    public void setPermitHolderCustomerNumber(final String permitHolderCustomerNumber) {
        this.permitHolderCustomerNumber = permitHolderCustomerNumber;
    }

    public String getPermitHolderName() {
        return permitHolderName;
    }

    public void setPermitHolderName(final String permitHolderName) {
        this.permitHolderName = permitHolderName;
    }

    public PersonContactInfoDTO getOriginalContactPerson() {
        return originalContactPerson;
    }

    public void setOriginalContactPerson(final PersonContactInfoDTO originalContactPerson) {
        this.originalContactPerson = originalContactPerson;
    }
}
