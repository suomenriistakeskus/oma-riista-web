package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.common.entity.Has2BeginEndDates;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import io.swagger.annotations.ApiModelProperty;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.validation.constraints.NotNull;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.riista.util.F.mapNullable;
import static java.util.Objects.requireNonNull;

public class MobileHuntingClubGroupDTO {

    private final long id;
    private final long clubId;
    private final int speciesCode;
    private final int huntingYear;

    private final LocalDate beginDate;
    private final LocalDate endDate;
    private final LocalDate beginDate2;
    private final LocalDate endDate2;

    @ApiModelProperty(required = true)
    private final String permitNumber;

    @ApiModelProperty(required = true)
    private final Map<String, String> name;

    public MobileHuntingClubGroupDTO(@Nonnull final HuntingClubGroup group,
                                     @Nonnull final GameSpecies species,
                                     @Nonnull final String permitNumber,
                                     @Nullable final HarvestPermitSpeciesAmount amount) {
        requireNonNull(group);

        this.id = group.getId();
        this.clubId = group.getParentOrganisation().getId();
        this.speciesCode = requireNonNull(species).getOfficialCode();
        this.huntingYear = group.getHuntingYear();
        this.permitNumber = requireNonNull(permitNumber);
        this.name = requireNonNull(group.getNameLocalisation()).asMap();

        this.beginDate = mapNullable(amount, HarvestPermitSpeciesAmount::getBeginDate);
        this.endDate = mapNullable(amount, HarvestPermitSpeciesAmount::getEndDate);
        this.beginDate2 = mapNullable(amount, HarvestPermitSpeciesAmount::getBeginDate2);
        this.endDate2 = mapNullable(amount, HarvestPermitSpeciesAmount::getEndDate2);

    }

    public long getId() {
        return id;
    }

    public long getClubId() {
        return clubId;
    }

    public int getSpeciesCode() {
        return speciesCode;
    }

    public int getHuntingYear() {
        return huntingYear;
    }

    public String getPermitNumber() {
        return permitNumber;
    }

    public Map<String, String> getName() {
        return name;
    }
}
