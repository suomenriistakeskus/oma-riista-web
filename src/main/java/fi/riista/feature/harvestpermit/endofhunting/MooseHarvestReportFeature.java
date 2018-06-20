package fi.riista.feature.harvestpermit.endofhunting;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountNotFound;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.feature.huntingclub.permit.HuntingClubPermitService;
import fi.riista.feature.storage.FileDownloadService;
import fi.riista.feature.storage.FileStorageService;
import fi.riista.feature.storage.metadata.FileType;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.security.EntityPermission;
import fi.riista.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.UUID;

@Component
public class MooseHarvestReportFeature {

    private static final Logger LOG = LoggerFactory.getLogger(MooseHarvestReportFeature.class);

    @Resource
    private MooseHarvestReportRepository mooseHarvestReportRepository;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Resource
    private HarvestPermitSpeciesAmountRepository harvestPermitSpeciesAmountRepository;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private FileDownloadService fileDownloadService;

    @Resource
    private HuntingClubPermitService huntingPermitService;

    @Transactional
    public MooseHarvestReportDTO create(final MooseHarvestReportDTO dto) {
        final HarvestPermit permit = harvestPermitRepository.getOne(dto.getHarvestPermitId());
        dto.assertConsistency(isHarvestsReportedForPermit(permit, dto.getGameSpeciesCode()));

        final MooseHarvestReport entity = new MooseHarvestReport();

        entity.setSpeciesAmount(harvestPermitSpeciesAmountRepository
                .findOneByHarvestPermitIdAndSpeciesCode(dto.getHarvestPermitId(), dto.getGameSpeciesCode())
                .orElseThrow(() -> new HarvestPermitSpeciesAmountNotFound(
                        permit.getPermitNumber(), dto.getGameSpeciesCode(), DateUtil.today())));

        if (dto.isModeratorOverride()) {
            entity.setNoHarvests(false);
            entity.setModeratorOverride(true);
            entity.setReceiptFileMetadata(null);

        } else if (dto.isNoHarvests()) {
            entity.setNoHarvests(true);
            entity.setModeratorOverride(false);
            entity.setReceiptFileMetadata(null);

        } else if (dto.getReceiptFile() != null) {
            entity.setNoHarvests(false);
            entity.setReceiptFileMetadata(storeReceipt(dto.getReceiptFile()));

        } else {
            throw new IllegalArgumentException("receipt is missing");
        }

        return MooseHarvestReportDTO.create(mooseHarvestReportRepository.saveAndFlush(entity));
    }

    private PersistentFileMetadata storeReceipt(final MultipartFile receiptFile) {
        try {
            return fileStorageService.storeFile(UUID.randomUUID(), receiptFile.getBytes(),
                    FileType.MOOSE_PERMIT_FINISHED_RECEIPT, receiptFile.getContentType(),
                    receiptFile.getOriginalFilename());

        } catch (IOException e) {
            LOG.warn("Saving receipt failed", e);

            throw new RuntimeException(e);
        }
    }

    private boolean isHarvestsReportedForPermit(HarvestPermit permit, int speciesCode) {
        return huntingPermitService.getHarvestCountsGroupedByClubId(permit, speciesCode).values()
                .stream()
                .anyMatch(c -> c.countAdults() + c.countYoung() - c.getNumberOfNonEdibleAdults() - c.getNumberOfNonEdibleYoungs() > 0);
    }

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public ResponseEntity<byte[]> getReceiptFile(long permitId, int speciesCode) throws IOException {
        final MooseHarvestReport mooseHarvestReport = findMooseHarvestReport(permitId, speciesCode);
        return fileDownloadService.download(mooseHarvestReport.getReceiptFileMetadata());
    }

    @Transactional
    public void delete(long permitId, int speciesCode) {
        final MooseHarvestReport mooseHarvestReport = findMooseHarvestReport(permitId, speciesCode);

        if (mooseHarvestReport == null) {
            return;
        }

        activeUserService.assertHasPermission(mooseHarvestReport, EntityPermission.DELETE);
        mooseHarvestReportRepository.delete(mooseHarvestReport);

        if (mooseHarvestReport.getReceiptFileMetadata() != null) {
            fileStorageService.remove(mooseHarvestReport.getReceiptFileMetadata().getId());
        }
    }

    private MooseHarvestReport findMooseHarvestReport(final long permitId, final int speciesCode) {
        return mooseHarvestReportRepository.findBySpeciesAmount(
                harvestPermitSpeciesAmountRepository.getOneByHarvestPermitIdAndSpeciesCode(permitId, speciesCode));
    }
}
