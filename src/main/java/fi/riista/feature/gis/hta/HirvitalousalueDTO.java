package fi.riista.feature.gis.hta;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import fi.riista.util.LocalisedString;

import javax.annotation.Nonnull;

public class HirvitalousalueDTO {
    private final Integer id;
    private final String nameFinnish;
    private final String nameSwedish;
    private final String number;

    public HirvitalousalueDTO(final Integer id,
                              final String nameFinnish,
                              final String nameSwedish,
                              final String number) {
        this.id = id;
        this.nameFinnish = nameFinnish;
        this.nameSwedish = nameSwedish;
        this.number = number;
    }

    @Nonnull
    @JsonIgnore
    public LocalisedString getNameLocalisation() {
        return LocalisedString.of(nameFinnish, nameSwedish);
    }

    public Integer getId() {
        return id;
    }

    @JsonProperty("nameFI")
    public String getNameFinnish() {
        return nameFinnish;
    }

    @JsonProperty("nameSV")
    public String getNameSwedish() {
        return nameSwedish;
    }

    public String getNumber() {
        return number;
    }
}
