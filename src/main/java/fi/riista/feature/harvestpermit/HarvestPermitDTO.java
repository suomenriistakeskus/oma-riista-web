package fi.riista.feature.harvestpermit;

import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.util.DtoUtil;
import fi.riista.validation.FinnishHuntingPermitNumber;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Set;

public class HarvestPermitDTO extends BaseEntityDTO<Long> {

    public HarvestPermitDTO(@Nonnull final HarvestPermit harvestPermit,
                            @Nonnull final Set<Integer> gameSpeciesCodes,
                            final boolean canAddHarvest,
                            final boolean canEditHarvest) {
        Objects.requireNonNull(harvestPermit, "harvestPermit must not be null");
        DtoUtil.copyBaseFields(harvestPermit, this);

        this.permitNumber = harvestPermit.getPermitNumber();
        this.permitType = harvestPermit.getPermitType();
        this.permitTypeCode = harvestPermit.getPermitTypeCode();
        this.harvestReportState = harvestPermit.getHarvestReportState();
        this.gameSpeciesCodes = gameSpeciesCodes;
        this.canAddHarvest = canAddHarvest;
        this.canEndHunting = canEditHarvest;
    }

    private Long id;
    private Integer rev;

    @FinnishHuntingPermitNumber
    private String permitNumber;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String permitType;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String permitTypeCode;

    private Set<Integer> gameSpeciesCodes;

    private HarvestReportState harvestReportState;

    private boolean canAddHarvest;
    private boolean canEndHunting;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return rev;
    }

    @Override
    public void setRev(Integer rev) {
        this.rev = rev;
    }

    public String getPermitNumber() {
        return permitNumber;
    }

    public void setPermitNumber(final String permitNumber) {
        this.permitNumber = permitNumber;
    }

    public String getPermitType() {
        return permitType;
    }

    public void setPermitType(final String permitType) {
        this.permitType = permitType;
    }

    public String getPermitTypeCode() {
        return permitTypeCode;
    }

    public void setPermitTypeCode(final String permitTypeCode) {
        this.permitTypeCode = permitTypeCode;
    }

    public Set<Integer> getGameSpeciesCodes() {
        return gameSpeciesCodes;
    }

    public void setGameSpeciesCodes(final Set<Integer> gameSpeciesCodes) {
        this.gameSpeciesCodes = gameSpeciesCodes;
    }

    public HarvestReportState getHarvestReportState() {
        return harvestReportState;
    }

    public void setHarvestReportState(final HarvestReportState harvestReportState) {
        this.harvestReportState = harvestReportState;
    }

    public boolean isCanAddHarvest() {
        return canAddHarvest;
    }

    public void setCanAddHarvest(final boolean canAddHarvest) {
        this.canAddHarvest = canAddHarvest;
    }

    public boolean isCanEndHunting() {
        return canEndHunting;
    }

    public void setCanEndHunting(final boolean canEndHunting) {
        this.canEndHunting = canEndHunting;
    }
}
