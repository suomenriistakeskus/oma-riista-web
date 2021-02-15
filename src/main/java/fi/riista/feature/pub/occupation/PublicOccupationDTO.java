package fi.riista.feature.pub.occupation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class PublicOccupationDTO {

    private final PublicOccupationTypeDTO occupationType;

    private final long orgId;

    private final String personName;

    @JsonInclude(Include.NON_NULL)
    private String additionalInfo;
    private Integer callOrder;

    private String email;
    private String phoneNumber;
    private String streetAddress;
    private String city;
    private String postalCode;
    private String country;

    private PublicOccupationBoardRepresentationDTO boardRepresentation;

    public PublicOccupationDTO(PublicOccupationTypeDTO occupationType, long orgId, String personName) {
        this.occupationType = occupationType;
        this.orgId = orgId;
        this.personName = personName;
    }

    public PublicOccupationTypeDTO getOccupationType() {
        return occupationType;
    }

    public long getOrganisationId() {
        return orgId;
    }

    public String getPersonName() {
        return personName;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public Integer getCallOrder() {
        return callOrder;
    }

    public void setCallOrder(Integer callOrder) {
        this.callOrder = callOrder;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public PublicOccupationBoardRepresentationDTO getBoardRepresentation() {
        return boardRepresentation;
    }

    public void setBoardRepresentation(final PublicOccupationBoardRepresentationDTO boardRepresentation) {
        this.boardRepresentation = boardRepresentation;
    }
}
