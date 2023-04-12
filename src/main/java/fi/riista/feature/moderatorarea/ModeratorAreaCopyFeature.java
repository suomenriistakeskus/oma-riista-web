package fi.riista.feature.moderatorarea;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.area.PersonalArea;
import fi.riista.feature.account.area.PersonalAreaRepository;
import fi.riista.feature.account.area.union.PersonalAreaUnion;
import fi.riista.feature.account.area.union.PersonalAreaUnionRepository;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.huntingclub.area.HuntingClubAreaRepository;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.area.HarvestPermitAreaRepository;
import fi.riista.security.EntityPermission;
import fi.riista.util.RandomStringUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static fi.riista.feature.moderatorarea.ModeratorAreaImportDTO.ModeratorAreaImportType.CLUB;
import static fi.riista.feature.moderatorarea.ModeratorAreaImportDTO.ModeratorAreaImportType.MODERATOR_AREA;
import static fi.riista.feature.moderatorarea.ModeratorAreaImportDTO.ModeratorAreaImportType.PERSONAL;
import static fi.riista.feature.moderatorarea.ModeratorAreaImportDTO.ModeratorAreaImportType.PERSONAL_AREA_UNION;

@Service
public class ModeratorAreaCopyFeature {

    @Resource
    private ModeratorAreaDTOTransformer dtoTransformer;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private GISZoneRepository gisZoneRepository;

    @Resource
    private SecureRandom secureRandom;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private PersonalAreaRepository personalAreaRepository;

    @Resource
    private HuntingClubAreaRepository huntingClubAreaRepository;

    @Resource
    private PersonalAreaUnionRepository personalAreaUnionRepository;

    @Resource
    private HarvestPermitAreaRepository harvestPermitAreaRepository;

    @Resource
    private ModeratorAreaRepository moderatorAreaRepository;

    @Transactional
    public ModeratorAreaDTO copy(final long areaId, final int year) {
        final ModeratorArea originalArea = requireEntityService.requireModeratorArea(areaId, EntityPermission.CREATE);

        final ModeratorArea area = new ModeratorArea();
        area.setModerator(activeUserService.requireActiveUser());
        area.setName(originalArea.getName());
        area.setYear(year);
        area.setRka(originalArea.getRka());

        area.generateAndStoreExternalId(secureRandom);

        Optional.ofNullable(originalArea.getZone())
                .map(originalZone -> gisZoneRepository.copyZone(originalZone, new GISZone()))
                .ifPresent(area::setZone);

        moderatorAreaRepository.saveAndFlush(area);

        return dtoTransformer.apply(area);
    }

    @Transactional
    public ModeratorAreaDTO importArea(final long moderatorAreaId, final ModeratorAreaImportDTO dto) {
        final ModeratorArea moderatorArea = requireEntityService.requireModeratorArea(
                moderatorAreaId, EntityPermission.UPDATE);

        if (dto.getType() == PERSONAL) {
            personalAreaRepository.findById(dto.getAreaId())
                    .map(PersonalArea::getZone)
                    .map(personalAreaZone -> gisZoneRepository.copyZone(personalAreaZone,
                            Optional.ofNullable(moderatorArea.getZone()).orElseGet(GISZone::new)))
                    .ifPresent(moderatorArea::setZone);
        } else if (dto.getType() == CLUB) {
            huntingClubAreaRepository.findById(dto.getAreaId())
                    .map(HuntingClubArea::getZone)
                    .map(clubAreaZone -> gisZoneRepository.copyZone(clubAreaZone,
                            Optional.ofNullable(moderatorArea.getZone()).orElseGet(GISZone::new)))
                    .ifPresent(moderatorArea::setZone);
        }

        return dtoTransformer.apply(moderatorArea);
    }

    @Transactional(readOnly = true)
    public ModeratorAreaImportDTO findByExternalId(final String externalId) {
        if (externalId.length() < RandomStringUtil.EXTERNAL_ID_LENGTH) {
            return null;
        }

        final Optional<PersonalArea> personalAreaOptional = personalAreaRepository.findByExternalId(externalId);
        if (personalAreaOptional.isPresent()) {
            return ModeratorAreaImportDTO.createFromPersonalArea(personalAreaOptional.get());
        }

        final Optional<HuntingClubArea> clubAreaOptional = huntingClubAreaRepository.findByExternalId(externalId);
        return clubAreaOptional.map(ModeratorAreaImportDTO::createFromClubArea).orElse(null);

    }

    @Transactional(readOnly = true)
    public List<ModeratorAreaImportDTO> findByExternalIds(final List<String> externalIds) {
        return externalIds.stream()
                .map(externalId -> {
                    final Optional<PersonalArea> personalAreaOptional = personalAreaRepository.findByExternalId(externalId);
                    if (personalAreaOptional.isPresent()) {
                        return ModeratorAreaImportDTO.createFromPersonalArea(personalAreaOptional.get());
                    }

                    final Optional<HuntingClubArea> clubAreaOptional = huntingClubAreaRepository.findByExternalId(externalId);
                    if (clubAreaOptional.isPresent()) {
                        return ModeratorAreaImportDTO.createFromClubArea(clubAreaOptional.get());
                    }

                    final Optional<PersonalAreaUnion> personalAreaUnionOptional = personalAreaUnionRepository.findByExternalId(externalId);
                    if (personalAreaUnionOptional.isPresent()){
                        return personalAreaUnionOptional
                                .map(area -> ModeratorAreaImportDTO.createFromPersonalAreaUnion(area, externalId))
                                .orElseGet(() -> new ModeratorAreaImportDTO(null, null, externalId, null, null));
                    }
                    final Optional<ModeratorArea> moderatorAreaOptional = moderatorAreaRepository.findByExternalId(externalId);
                    return ModeratorAreaImportDTO.createFromModeratorArea(moderatorAreaOptional.get());
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public ModeratorAreaDTO addAreas(final long moderatorAreaId, final List<ModeratorAreaImportDTO> areaList) {
        final ModeratorArea moderatorArea = requireEntityService.requireModeratorArea(
                moderatorAreaId, EntityPermission.UPDATE);

        areaList.forEach(area -> {
            if (area.getType() == PERSONAL) {
                personalAreaRepository.findById(area.getAreaId())
                        .map(PersonalArea::getZone)
                        .map(personalAreaZone -> gisZoneRepository.addAreas(personalAreaZone,
                                Optional.ofNullable(moderatorArea.getZone()).orElseGet(GISZone::new)))
                        .ifPresent(moderatorArea::setZone);
            } else if (area.getType() == CLUB) {
                huntingClubAreaRepository.findById(area.getAreaId())
                        .map(HuntingClubArea::getZone)
                        .map(clubAreaZone -> gisZoneRepository.addAreas(clubAreaZone,
                                Optional.ofNullable(moderatorArea.getZone()).orElseGet(GISZone::new)))
                        .ifPresent(moderatorArea::setZone);
            } else if (area.getType() == PERSONAL_AREA_UNION) {
                harvestPermitAreaRepository.findByExternalId(area.getExternalId())
                        .map(HarvestPermitArea::getZone)
                        .map(harvestPermitAreaZone -> gisZoneRepository.addAreas(harvestPermitAreaZone,
                                Optional.ofNullable(moderatorArea.getZone()).orElseGet(GISZone::new)))
                        .ifPresent(moderatorArea::setZone);
            } else if (area.getType() == MODERATOR_AREA) {
                moderatorAreaRepository.findByExternalId(area.getExternalId())
                        .map(ModeratorArea::getZone)
                        .map(areaZone-> gisZoneRepository.addAreas(areaZone,
                                Optional.ofNullable(moderatorArea.getZone()).orElseGet(GISZone::new)))
                        .ifPresent(moderatorArea::setZone);
            }
        });

        return dtoTransformer.apply(moderatorArea);
    }
}
