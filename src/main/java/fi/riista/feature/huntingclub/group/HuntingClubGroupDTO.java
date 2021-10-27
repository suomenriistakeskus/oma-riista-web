package fi.riista.feature.huntingclub.group;

import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesDTO;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import fi.riista.validation.DoNotValidate;
import javax.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.SafeHtml;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Objects;

public class HuntingClubGroupDTO extends BaseEntityDTO<Long> {


    public static class PermitDTO {
        public static PermitDTO create(final HarvestPermit permit) {
            final PermitDTO dto = new PermitDTO();
            dto.setId(permit.getId());
            dto.setPermitNumber(permit.getPermitNumber());

            return dto;
        }

        public static List<PermitDTO> create(List<HarvestPermit> permits) {
            return F.mapNonNullsToList(permits, PermitDTO::create);
        }

        private Long id;

        @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
        private String permitNumber;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getPermitNumber() {
            return permitNumber;
        }

        public void setPermitNumber(String permitNumber) {
            this.permitNumber = permitNumber;
        }
    }

    public static HuntingClubGroupDTO create(@Nonnull final HuntingClubGroup group,
                                             @Nullable final GameSpecies gameSpecies,
                                             @Nullable final HarvestPermit harvestPermit) {

        Objects.requireNonNull(group, "group is null");

        final HuntingClubGroupDTO dto = new HuntingClubGroupDTO();
        DtoUtil.copyBaseFields(group, dto);

        dto.setNameFI(group.getNameFinnish());
        dto.setNameSV(group.getNameSwedish());
        dto.setHuntingYear(group.getHuntingYear());

        dto.setClubId(F.getId(group.getParentOrganisation()));
        dto.setHuntingAreaId(F.getId(group.getHuntingArea()));

        dto.setFromMooseDataCard(group.isFromMooseDataCard());

        if (gameSpecies != null) {
            dto.setGameSpeciesCode(gameSpecies.getOfficialCode());
            dto.setSpecies(GameSpeciesDTO.create(gameSpecies));
        }

        if (harvestPermit != null) {
            dto.setPermit(PermitDTO.create(harvestPermit));
        }

        return dto;
    }

    private Long id;
    private Integer rev;

    @NotBlank
    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String nameFI;

    @NotBlank
    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String nameSV;

    @Min(HuntingClubGroup.MIN_YEAR)
    @Max(HuntingClubGroup.MAX_YEAR)
    private int huntingYear;

    @DoNotValidate
    private GameSpeciesDTO species;

    private int gameSpeciesCode;
    private Long clubId;

    @NotNull
    private Long huntingAreaId;

    @DoNotValidate
    private PermitDTO permit;

    private boolean canEdit;
    private boolean huntingDaysExist;
    private boolean huntingFinished;
    private boolean fromMooseDataCard;
    private Long memberCount;

    public HuntingClubGroupDTO() {
    }

    @AssertTrue
    public boolean isPermitNumberPresentWhenGroupIsGeneratedFromMooseDataCard() {
        return !fromMooseDataCard || hasPermitNumber();
    }

    @AssertTrue
    public boolean isFromMooseDataCardConsistentWithGameSpeciesCode() {
        return !fromMooseDataCard || GameSpecies.isMoose(gameSpeciesCode);
    }

    public boolean hasPermitNumber() {
        return permit != null && StringUtils.hasText(permit.getPermitNumber());
    }

    // Accessors -->

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return rev;
    }

    @Override
    public void setRev(final Integer rev) {
        this.rev = rev;
    }

    public String getNameFI() {
        return nameFI;
    }

    public void setNameFI(final String nameFI) {
        this.nameFI = nameFI;
    }

    public String getNameSV() {
        return nameSV;
    }

    public void setNameSV(final String nameSV) {
        this.nameSV = nameSV;
    }

    public int getHuntingYear() {
        return huntingYear;
    }

    public void setHuntingYear(final int huntingYear) {
        this.huntingYear = huntingYear;
    }

    public GameSpeciesDTO getSpecies() {
        return species;
    }

    public void setSpecies(final GameSpeciesDTO species) {
        this.species = species;
    }

    public int getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public void setGameSpeciesCode(final int gameSpeciesCode) {
        this.gameSpeciesCode = gameSpeciesCode;
    }

    public Long getClubId() {
        return clubId;
    }

    public void setClubId(final Long clubId) {
        this.clubId = clubId;
    }

    public Long getHuntingAreaId() {
        return huntingAreaId;
    }

    public void setHuntingAreaId(final Long huntingAreaId) {
        this.huntingAreaId = huntingAreaId;
    }

    public PermitDTO getPermit() {
        return permit;
    }

    public void setPermit(final PermitDTO permit) {
        this.permit = permit;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(final boolean canEdit) {
        this.canEdit = canEdit;
    }

    public boolean isHuntingDaysExist() {
        return huntingDaysExist;
    }

    public void setHuntingDaysExist(final boolean huntingDaysExist) {
        this.huntingDaysExist = huntingDaysExist;
    }

    public boolean isHuntingFinished() {
        return huntingFinished;
    }

    public void setHuntingFinished(final boolean huntingFinished) {
        this.huntingFinished = huntingFinished;
    }

    public boolean isFromMooseDataCard() {
        return fromMooseDataCard;
    }

    public void setFromMooseDataCard(final boolean fromMooseDataCard) {
        this.fromMooseDataCard = fromMooseDataCard;
    }

    public Long getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(final Long memberCount) {
        this.memberCount = memberCount;
    }
}
