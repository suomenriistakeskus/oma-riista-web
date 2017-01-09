package fi.riista.feature.huntingclub.permit.harvestreport;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import java.util.UUID;

public class MooseHarvestReportDTO extends BaseEntityDTO<Long> {

    public static @Nullable MooseHarvestReportDTO create(@Nullable final MooseHarvestReport entity) {
        if (entity == null) {
            return null;
        }
        final MooseHarvestReportDTO dto = new MooseHarvestReportDTO();
        DtoUtil.copyBaseFields(entity, dto);

        dto.setHarvestPermitId(entity.getSpeciesAmount().getHarvestPermit().getId());
        dto.setNoHarvests(entity.isNoHarvests());
        dto.setModeratorOverride(entity.isModeratorOverride());
        dto.setGameSpeciesCode(entity.getSpeciesAmount().getGameSpecies().getOfficialCode());
        dto.setReceiptUuid(F.getId(entity.getReceiptFileMetadata()));
        return dto;
    }

    private Long id;
    private Integer rev;

    private Long harvestPermitId;
    private int gameSpeciesCode;

    private UUID receiptUuid;
    private boolean noHarvests;
    private boolean moderatorOverride;

    @JsonIgnore
    private MultipartFile receiptFile;

    public MooseHarvestReportDTO() {
    }

    private MooseHarvestReportDTO(long harvestPermitId, int gameSpeciesCode) {
        this.harvestPermitId = harvestPermitId;
        this.gameSpeciesCode = gameSpeciesCode;
    }

    public static MooseHarvestReportDTO withReceipt(long harvestPermitId, int gameSpeciesCode, MultipartFile receiptFile) {
        MooseHarvestReportDTO dto = new MooseHarvestReportDTO(harvestPermitId, gameSpeciesCode);
        dto.setReceiptFile(receiptFile);
        return dto;
    }

    public static MooseHarvestReportDTO withNoHarvests(long harvestPermitId, int gameSpeciesCode) {
        MooseHarvestReportDTO dto = new MooseHarvestReportDTO(harvestPermitId, gameSpeciesCode);
        dto.setNoHarvests(true);
        return dto;
    }

    public static MooseHarvestReportDTO withModeratorOverride(Long harvestPermitId, int gameSpeciesCode) {
        MooseHarvestReportDTO dto = new MooseHarvestReportDTO(harvestPermitId, gameSpeciesCode);
        dto.setModeratorOverride(true);
        return dto;
    }

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

    public Long getHarvestPermitId() {
        return harvestPermitId;
    }

    public void setHarvestPermitId(Long harvestPermitId) {
        this.harvestPermitId = harvestPermitId;
    }

    public int getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public void setGameSpeciesCode(int gameSpeciesCode) {
        this.gameSpeciesCode = gameSpeciesCode;
    }

    public UUID getReceiptUuid() {
        return receiptUuid;
    }

    public void setReceiptUuid(UUID receiptUuid) {
        this.receiptUuid = receiptUuid;
    }

    public boolean isNoHarvests() {
        return noHarvests;
    }

    public void setNoHarvests(boolean noHarvests) {
        this.noHarvests = noHarvests;
    }

    public boolean isModeratorOverride() {
        return moderatorOverride;
    }

    public void setModeratorOverride(boolean moderatorOverride) {
        this.moderatorOverride = moderatorOverride;
    }

    public MultipartFile getReceiptFile() {
        return receiptFile;
    }

    public void setReceiptFile(MultipartFile receiptFile) {
        this.receiptFile = receiptFile;
    }
}
