package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.huntingclub.HuntingClub;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class MobileHuntingClubDTO {

    private final long id;

    @ApiModelProperty(required = true)
    private final String officialCode;

    @ApiModelProperty(required = true)
    private final Map<String, String> name;

    public MobileHuntingClubDTO(@Nonnull final HuntingClub club) {
        requireNonNull(club);

        this.id = club.getId();
        this.officialCode = requireNonNull(club.getOfficialCode());
        this.name = requireNonNull(club.getNameLocalisation()).asMap();
    }

    public long getId() {
        return id;
    }

    public String getOfficialCode() {
        return officialCode;
    }

    public Map<String, String> getName() {
        return name;
    }
}
