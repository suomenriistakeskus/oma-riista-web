package fi.riista.feature.huntingclub.search;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import fi.riista.feature.huntingclub.HuntingClub;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;

public class HuntingClubNameDTO {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long id;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String nameFI;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String nameSV;

    public static @Nonnull HuntingClubNameDTO create(@Nonnull final HuntingClub huntingClub) {
        return new HuntingClubNameDTO(huntingClub);
    }
    public HuntingClubNameDTO() { }

    public HuntingClubNameDTO(@Nonnull final HuntingClub huntingClub) {
        setId(huntingClub.getId());
        setNameFI(huntingClub.getNameFinnish());
        setNameSV(huntingClub.getNameSwedish());
    }

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Long getId() {
        return id;
    }

    @JsonIgnore
    public void setId(Long id) {
        this.id = id;
    }

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getNameFI() {
        return nameFI;
    }

    @JsonIgnore
    public void setNameFI(String nameFI) {
        this.nameFI = nameFI;
    }

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getNameSV() {
        return nameSV;
    }

    @JsonIgnore
    public void setNameSV(String nameSV) {
        this.nameSV = nameSV;
    }
}
