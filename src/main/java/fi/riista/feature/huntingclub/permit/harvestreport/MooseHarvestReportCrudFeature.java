package fi.riista.feature.huntingclub.permit.harvestreport;

import fi.riista.feature.SimpleAbstractCrudFeature;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.feature.huntingclub.permit.HuntingClubPermitService;
import fi.riista.feature.storage.FileStorageService;
import fi.riista.feature.storage.metadata.FileType;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Component
public class MooseHarvestReportCrudFeature
        extends SimpleAbstractCrudFeature<Long, MooseHarvestReport, MooseHarvestReportDTO> {

    private static final Logger LOG = LoggerFactory.getLogger(MooseHarvestReportCrudFeature.class);

    @Resource
    private MooseHarvestReportRepository mooseHarvestReportRepository;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Resource
    private HarvestPermitSpeciesAmountRepository harvestPermitSpeciesAmountRepository;

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private HuntingClubPermitService huntingPermitService;

    @Override
    protected Function<MooseHarvestReport, MooseHarvestReportDTO> entityToDTOFunction() {
        return MooseHarvestReportDTO::create;
    }

    @Override
    protected JpaRepository<MooseHarvestReport, Long> getRepository() {
        return mooseHarvestReportRepository;
    }

    @Override
    protected void updateEntity(MooseHarvestReport entity, MooseHarvestReportDTO dto) {
        final HarvestPermit permit = harvestPermitRepository.getOne(dto.getHarvestPermitId());

        boolean noHarvests = dto.isNoHarvests();
        boolean moderatorOverride = dto.isModeratorOverride();
        boolean harvestsReportedForPermit = isHarvestsReportedForPermit(permit, dto.getGameSpeciesCode());

        assertConsistency(noHarvests, moderatorOverride, harvestsReportedForPermit);

        final Optional<HarvestPermitSpeciesAmount> hpsa = harvestPermitSpeciesAmountRepository
                .findOneByHarvestPermitIdAndSpeciesCode(dto.getHarvestPermitId(), dto.getGameSpeciesCode());
        entity.setSpeciesAmount(hpsa.orElseGet(null));

        if (moderatorOverride) {
            entity.setNoHarvests(false);
            entity.setModeratorOverride(true);
            entity.setReceiptFileMetadata(null);
        } else if (noHarvests) {
            entity.setNoHarvests(true);
            entity.setModeratorOverride(false);
            entity.setReceiptFileMetadata(null);
        } else {
            entity.setNoHarvests(false);
            MultipartFile receiptFile = dto.getReceiptFile();
            try {
                entity.setReceiptFileMetadata(fileStorageService.storeFile(
                        UUID.randomUUID(),
                        receiptFile.getBytes(),
                        FileType.MOOSE_PERMIT_FINISHED_RECEIPT,
                        receiptFile.getContentType(),
                        receiptFile.getOriginalFilename()));
            } catch (IOException e) {
                LOG.warn("Saving receipt failed", e);
                throw new RuntimeException(e);
            }
        }
    }

    private static void assertConsistency(boolean noHarvests, boolean moderatorOverride, boolean harvestsReportedForPermit) {
        if (moderatorOverride) {
            if (noHarvests) {
                throw new MooseHarvestReportException("Wished to make moderator override, but no-harvests is true");
            }

        } else {
            if (noHarvests && harvestsReportedForPermit) {
                throw new MooseHarvestReportException("Permit has harvests, but wished to make no-harvests report");
            }
            if (!noHarvests && !harvestsReportedForPermit) {
                throw new MooseHarvestReportException("Permit has no harvests, but wished to attach receipt");
            }
        }
    }

    private boolean isHarvestsReportedForPermit(HarvestPermit permit, int speciesCode) {
        return huntingPermitService.getHarvestCountsGroupedByClubId(permit, speciesCode).values()
                .stream()
                .anyMatch(c -> c.countAdults() + c.countYoung() > 0);
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public ResponseEntity<byte[]> getReceiptFile(long permitId, int speciesCode) throws IOException {
        final MooseHarvestReport mooseHarvestReport = findMooseHarvestReport(permitId, speciesCode);
        final PersistentFileMetadata metadata = mooseHarvestReport.getReceiptFileMetadata();
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentLength(metadata.getContentSize());
        headers.setContentType(MediaType.parseMediaType(metadata.getContentType()));
        headers.setContentDispositionFormData("file", metadata.getOriginalFilename());

        return ResponseEntity.ok()
                .headers(headers)
                .body(fileStorageService.getBytes(metadata.getId()));
    }

    @Transactional
    public void delete(long permitId, int speciesCode) {
        final MooseHarvestReport mooseHarvestReport = findMooseHarvestReport(permitId, speciesCode);
        if (mooseHarvestReport != null) {
            delete(mooseHarvestReport.getId());
            if (mooseHarvestReport.getReceiptFileMetadata() != null) {
                fileStorageService.remove(mooseHarvestReport.getReceiptFileMetadata().getId());
            }
        }
    }

    private MooseHarvestReport findMooseHarvestReport(long permitId, int speciesCode) {
        final HarvestPermitSpeciesAmount hpsa =
                harvestPermitSpeciesAmountRepository.getOneByHarvestPermitIdAndSpeciesCode(permitId, speciesCode);
        return mooseHarvestReportRepository.findBySpeciesAmount(hpsa);
    }
}
