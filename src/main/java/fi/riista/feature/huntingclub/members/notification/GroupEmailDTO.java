package fi.riista.feature.huntingclub.members.notification;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.List;

public class GroupEmailDTO {

    private long id;
    private long clubId;
    private long rhyId;

    private String nameFinnish;
    private String nameSwedish;

    private int huntingYear;
    private String permitNumber;

    private String speciesNameFinnish;
    private String speciesNameSwedish;

    private List<LeaderEmailDTO> leaders;

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final GroupEmailDTO that = (GroupEmailDTO) obj;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(clubId, that.clubId)
                .append(rhyId, that.rhyId)
                .append(nameFinnish, that.nameFinnish)
                .append(nameSwedish, that.nameSwedish)
                .append(huntingYear, that.huntingYear)
                .append(permitNumber, that.permitNumber)
                .append(speciesNameFinnish, that.speciesNameFinnish)
                .append(speciesNameSwedish, that.speciesNameSwedish)
                .append(leaders, that.leaders)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(clubId)
                .append(rhyId)
                .append(nameFinnish)
                .append(nameSwedish)
                .append(huntingYear)
                .append(permitNumber)
                .append(speciesNameFinnish)
                .append(speciesNameSwedish)
                .append(leaders)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", id)
                .append("clubId", clubId)
                .append("rhyId", rhyId)
                .append("nameFinnish", nameFinnish)
                .append("nameSwedish", nameSwedish)
                .append("huntingYear", huntingYear)
                .append("permitNumber", permitNumber)
                .append("speciesNameFinnish", speciesNameFinnish)
                .append("speciesNameFinnish", speciesNameSwedish)
                .append("leaders", leaders)
                .toString();
    }

    // Accessors ->

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public long getClubId() {
        return clubId;
    }

    public void setClubId(final long clubId) {
        this.clubId = clubId;
    }

    public long getRhyId() {
        return rhyId;
    }

    public void setRhyId(final long rhyId) {
        this.rhyId = rhyId;
    }

    public String getNameFinnish() {
        return nameFinnish;
    }

    public void setNameFinnish(final String nameFinnish) {
        this.nameFinnish = nameFinnish;
    }

    public String getNameSwedish() {
        return nameSwedish;
    }

    public void setNameSwedish(final String nameSwedish) {
        this.nameSwedish = nameSwedish;
    }

    public int getHuntingYear() {
        return huntingYear;
    }

    public void setHuntingYear(final int huntingYear) {
        this.huntingYear = huntingYear;
    }

    public String getPermitNumber() {
        return permitNumber;
    }

    public void setPermitNumber(final String permitNumber) {
        this.permitNumber = permitNumber;
    }

    public String getSpeciesNameFinnish() {
        return speciesNameFinnish;
    }

    public void setSpeciesNameFinnish(final String speciesNameFinnish) {
        this.speciesNameFinnish = speciesNameFinnish;
    }

    public String getSpeciesNameSwedish() {
        return speciesNameSwedish;
    }

    public void setSpeciesNameSwedish(final String speciesNameSwedish) {
        this.speciesNameSwedish = speciesNameSwedish;
    }

    public List<LeaderEmailDTO> getLeaders() {
        return leaders;
    }

    public void setLeaders(final List<LeaderEmailDTO> leaders) {
        this.leaders = leaders;
    }
}
