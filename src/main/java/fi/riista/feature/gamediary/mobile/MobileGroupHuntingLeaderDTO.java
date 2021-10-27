package fi.riista.feature.gamediary.mobile;

import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class MobileGroupHuntingLeaderDTO {

    @ApiModelProperty(required = true)
    private final List<MobileHuntingClubDTO> clubs;

    @ApiModelProperty(required = true)
    private final List<MobileHuntingClubGroupDTO> groups;

    public MobileGroupHuntingLeaderDTO(@Nonnull final List<MobileHuntingClubDTO> clubs,
                                       @Nonnull final List<MobileHuntingClubGroupDTO> groups) {
        this.clubs = requireNonNull(clubs);
        this.groups = requireNonNull(groups);
    }

    public List<MobileHuntingClubDTO> getClubs() {
        return clubs;
    }

    public List<MobileHuntingClubGroupDTO> getGroups() {
        return groups;
    }
}
