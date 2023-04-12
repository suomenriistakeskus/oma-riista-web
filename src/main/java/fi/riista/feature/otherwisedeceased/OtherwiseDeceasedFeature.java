package fi.riista.feature.otherwisedeceased;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.gis.GISQueryService;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.otherwisedeceased.OtherwiseDeceasedChange.ChangeType;
import fi.riista.util.DateUtil;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

import static fi.riista.config.Constants.DEFAULT_TIMEZONE;

@Component
public class OtherwiseDeceasedFeature {

    private static final Logger LOG = LoggerFactory.getLogger(OtherwiseDeceasedFeature.class);

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private OtherwiseDeceasedRepository otherwiseDeceasedRepository;

    @Resource
    private OtherwiseDeceasedChangeRepository changeRepository;

    @Resource
    private OtherwiseDeceasedAttachmentService attachmentService;

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Resource
    private GISQueryService gisQueryService;

    @Resource
    private OtherwiseDeceasedBriefDTOTransformer briefDTOTransformer;

    @Resource
    private OtherwiseDeceasedDTOTransformer dtoTransformer;

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasPrivilege('MUUTOIN_KUOLLEET')")
    @Transactional(readOnly = true)
    public Slice<OtherwiseDeceasedBriefDTO> searchPage(final @Validated OtherwiseDeceasedFilterDTO filterDTO,
                                                       final @Validated Pageable pageable) {
        LOG.info("search()");
        final Slice<OtherwiseDeceased> slice = otherwiseDeceasedRepository.searchPage(filterDTO, pageable);
        return new SliceImpl<>(briefDTOTransformer.transform(slice.getContent()), pageable, slice.hasNext());
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasPrivilege('MUUTOIN_KUOLLEET')")
    @Transactional(readOnly = true)
    public OtherwiseDeceasedDTO getDetails(final long id) {
        LOG.info("getDetails("+id+")");
        final OtherwiseDeceased entity = otherwiseDeceasedRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("OtherwiseDeceased not found, id: " + id));
        return dtoTransformer.createDTO(entity);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasPrivilege('MUUTOIN_KUOLLEET')")
    @Transactional
    public OtherwiseDeceasedDTO save(final OtherwiseDeceasedDTO dto) {
        LOG.info("save");
        return dto.getId() == null ? create(dto) : update(dto);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasPrivilege('MUUTOIN_KUOLLEET')")
    @Transactional(rollbackFor = IOException.class)
    public OtherwiseDeceasedDTO save(final OtherwiseDeceasedDTO dto,
                                     final List<MultipartFile> attachments) throws IOException {
        LOG.info("save with attachments");
        final long id = dto.getId() == null ? create(dto).getId() : update(dto).getId();
        final OtherwiseDeceased entity = otherwiseDeceasedRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("OtherwiseDeceased not found, id: " + id));
        attachmentService.addAttachments(entity, attachments);
        return getDetails(id);
    }

    private OtherwiseDeceasedDTO create(final OtherwiseDeceasedDTO dto) {
        final Riistanhoitoyhdistys rhy = gisQueryService.findRhyByLocation(dto.getGeoLocation());
        final OtherwiseDeceased entity = OtherwiseDeceased.create(
                gameSpeciesService.requireByOfficialCode(dto.getGameSpeciesCode()),
                dto.getAge(),
                dto.getGender(),
                dto.getWeight(),
                dto.getPointOfTime().toDateTime(DEFAULT_TIMEZONE),
                dto.isNoExactLocation(),
                dto.getGeoLocation(),
                gisQueryService.findMunicipality(dto.getGeoLocation()),
                rhy,
                rhy.getRiistakeskuksenAlue(),
                dto.getCause(),
                dto.getCauseDescription(),
                dto.getSource(),
                dto.getSourceDescription(),
                dto.getDescription(),
                dto.getAdditionalInfo()
        );
        otherwiseDeceasedRepository.save(entity);
        changeRepository.save(createChange(entity, ChangeType.CREATE, null));
        return dtoTransformer.createDTO(entity);
    }

    private OtherwiseDeceasedDTO update(final OtherwiseDeceasedDTO dto) {
        final Long id = dto.getId();
        LOG.info("update("+id+")");

        final OtherwiseDeceased entity = otherwiseDeceasedRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("OtherwiseDeceased not found, id: " + id));
        updateMutableFields(entity, dto);
        otherwiseDeceasedRepository.save(entity);
        changeRepository.save(createChange(entity, ChangeType.MODIFY, dto.getReasonForChange()));
        return dtoTransformer.createDTO(entity);
    }

    private void updateMutableFields(final OtherwiseDeceased entity, final OtherwiseDeceasedDTO dto) {
        entity.setSpecies(gameSpeciesService.requireByOfficialCode(dto.getGameSpeciesCode()));
        entity.setAge(dto.getAge());
        entity.setGender(dto.getGender());
        entity.setWeight(dto.getWeight());
        entity.setPointOfTime(dto.getPointOfTime().toDateTime(DEFAULT_TIMEZONE));
        entity.setNoExactLocation(dto.isNoExactLocation());
        entity.setGeoLocation(dto.getGeoLocation());
        entity.setMunicipality(gisQueryService.findMunicipality(dto.getGeoLocation()));

        final Riistanhoitoyhdistys rhy = gisQueryService.findRhyByLocation(dto.getGeoLocation());
        entity.setRhy(rhy);
        entity.setRka(rhy.getRiistakeskuksenAlue());
        entity.setCause(dto.getCause());
        entity.setCauseDescription(dto.getCauseDescription());
        entity.setSource(dto.getSource());
        entity.setSourceDescription(dto.getSourceDescription());
        entity.setDescription(dto.getDescription());
        entity.setAdditionalInfo(dto.getAdditionalInfo());
    }

    private OtherwiseDeceasedChange createChange(final OtherwiseDeceased entity,
                                                 final ChangeType changeType,
                                                 final String reasonForChange) {
        return new OtherwiseDeceasedChange(
                entity,
                DateUtil.now(),
                activeUserService.requireActiveUserId(),
                changeType,
                reasonForChange);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasPrivilege('MUUTOIN_KUOLLEET')")
    @Transactional
    public void reject(final long id) {
        final OtherwiseDeceased entity = otherwiseDeceasedRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("OtherwiseDeceased not found, id: " + id));
        if (!entity.isRejected()) {
            entity.setRejected(true);
            otherwiseDeceasedRepository.save(entity);
            changeRepository.save(createChange(entity, ChangeType.DELETE, null));
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasPrivilege('MUUTOIN_KUOLLEET')")
    @Transactional
    public void restore(final long id) {
        final OtherwiseDeceased entity = otherwiseDeceasedRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("OtherwiseDeceased not found, id: " + id));
        if (entity.isRejected()) {
            entity.setRejected(false);
            otherwiseDeceasedRepository.save(entity);
            changeRepository.save(createChange(entity, ChangeType.RESTORE, null));
        }
    }

}
