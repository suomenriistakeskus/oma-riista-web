package fi.riista.api.club;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameDiaryEntryType;
import fi.riista.feature.gamediary.GameDiaryMetadataFeature;
import fi.riista.feature.gamediary.HuntingDiaryEntryDTO;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestFieldsDTO;
import fi.riista.feature.huntingclub.hunting.GroupHuntingDiaryFeature;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDayCrudFeature;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDayDTO;
import fi.riista.feature.huntingclub.hunting.rejection.AcceptClubDiaryObservationDTO;
import fi.riista.feature.huntingclub.hunting.rejection.RejectClubDiaryEntryDTO;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1/club/group")
public class ClubGroupDiaryEntryApiResource {

    @Resource
    private GroupHuntingDiaryFeature diaryFeature;

    @Resource
    private GroupHuntingDayCrudFeature groupHuntingDayCrudFeature;

    @Resource
    private GameDiaryMetadataFeature gameDiaryMetadataFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{groupId:\\d+}/diary", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<HuntingDiaryEntryDTO> getDiaryOfMembers(@PathVariable final long groupId) {
        return diaryFeature.getDiaryOfGroupMembers(groupId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{groupId:\\d+}/rejected", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<GameDiaryEntryType, List<Long>> listRejected(@PathVariable final long groupId) {
        return diaryFeature.listRejected(groupId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{groupId:\\d+}/rejectentry")
    public void rejectEntryFromHuntingGroup(@PathVariable final long groupId,
                                            @RequestBody @Validated final RejectClubDiaryEntryDTO dto) {
        dto.setGroupId(groupId);
        diaryFeature.rejectDiaryEntryFromHuntingGroup(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{groupId:\\d+}/acceptobservationwithindeerhunting")
    public void acceptObservationFromHuntingGroup(@PathVariable final long groupId,
                                                  @RequestBody @Validated final AcceptClubDiaryObservationDTO dto) {
        dto.setGroupId(groupId);
        groupHuntingDayCrudFeature.acceptClubDiaryObservationToHuntingDay(dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{groupId:\\d+}/huntingdays", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<GroupHuntingDayDTO> getHuntingDays(@PathVariable final long groupId) {
        return groupHuntingDayCrudFeature.findByClubGroup(groupId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/harvest/fields", produces = MediaType.APPLICATION_JSON_VALUE)
    public RequiredHarvestFieldsDTO getRequiredHarvestFieldsForHuntingGroup(@RequestParam final long huntingGroupId) {
        return gameDiaryMetadataFeature
                .getRequiredHarvestFieldsForHuntingGroup(huntingGroupId, HarvestSpecVersion.CURRENTLY_SUPPORTED);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/harvest/legally-mandatory-fields", produces = MediaType.APPLICATION_JSON_VALUE)
    public RequiredHarvestFieldsDTO getLegallyMandatoryHarvestFieldsForHuntingGroup(@RequestParam final long huntingGroupId) {
        return gameDiaryMetadataFeature
                .getLegallyMandatoryHarvestFieldsForHuntingGroup(huntingGroupId, HarvestSpecVersion.CURRENTLY_SUPPORTED);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/harvest/{harvestId:\\d+}/editlocation")
    public void editHarvestLocation(@PathVariable final long harvestId,
                                    @RequestBody @Valid final GeoLocation location) {

        diaryFeature.editHarvestGeolocation(harvestId, location);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/observation/{observationId:\\d+}/editlocation")
    public void editObservationLocation(@PathVariable final long observationId,
                                        @RequestBody @Valid final GeoLocation location) {

        diaryFeature.editObservationLocation(observationId, location);
    }
}
