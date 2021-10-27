package fi.riista.api.mobile;

import fi.riista.feature.gamediary.harvest.HarvestExceptionMapper;
import fi.riista.feature.gamediary.mobile.MobileObservationDTO;
import fi.riista.feature.huntingclub.hunting.GroupHuntingDiaryFeature;
import fi.riista.feature.huntingclub.hunting.mobile.MobileGroupHarvestDTO;
import fi.riista.feature.huntingclub.hunting.mobile.MobileGroupHarvestFeature;
import fi.riista.feature.huntingclub.hunting.mobile.MobileGroupHuntingDiaryDTO;
import fi.riista.feature.huntingclub.hunting.mobile.MobileGroupHuntingDiaryFeature;
import fi.riista.feature.huntingclub.hunting.mobile.MobileGroupObservationDTO;
import fi.riista.feature.huntingclub.hunting.mobile.MobileGroupObservationFeature;
import fi.riista.feature.huntingclub.hunting.rejection.RejectClubDiaryEntryDTO;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping(value = "/api/mobile/v2/grouphunting")
public class MobileClubGroupDiaryEntryApiResource {

    @Resource
    private MobileGroupHuntingDiaryFeature mobileGroupHuntingDiaryFeature;

    @Resource
    private GroupHuntingDiaryFeature groupHuntingDiaryFeature;

    @Resource
    private MobileGroupHarvestFeature mobileGroupHarvestFeature;

    @Resource
    private MobileGroupObservationFeature mobileGroupObservationFeature;

    @Resource
    private HarvestExceptionMapper harvestExceptionMapper;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{groupId:\\d+}/diary", produces = MediaType.APPLICATION_JSON_VALUE)
    public MobileGroupHuntingDiaryDTO getDiaryOfMembers(@PathVariable final long groupId) {
        return mobileGroupHuntingDiaryFeature.getDiaryOfGroupMembers(groupId);
    }

    @PostMapping(value = "/harvest",
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createHarvest(@RequestBody @Valid final MobileGroupHarvestDTO dto) {
        // on create force id and rev to be null
        dto.setId(null);
        dto.setRev(null);
        try {
            return ResponseEntity.ok(mobileGroupHarvestFeature.createHarvest(dto));
        } catch (final RuntimeException e) {
            return harvestExceptionMapper.handleException(e);
        }
    }

    @PutMapping(value = "/harvest/{id:\\d+}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateHarvest(@PathVariable final long id,
                                           @RequestBody @Validated final MobileGroupHarvestDTO dto) {
        dto.setId(id);
        try {
            return ResponseEntity.ok(mobileGroupHarvestFeature.updateHarvest(dto));
        } catch (final RuntimeException e) {
            return harvestExceptionMapper.handleException(e);
        }
    }

    @PostMapping(value = "/observation",
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public MobileObservationDTO createObservation(@RequestBody @Validated final MobileGroupObservationDTO dto) {
        // on create force id to be null
        dto.setId(null);
        return mobileGroupObservationFeature.createObservation(dto);
    }

    @PutMapping(value = "/observation/{id:\\d+}",
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public MobileObservationDTO updateObservation(@PathVariable final long id,
                                                  @RequestBody @Validated final MobileGroupObservationDTO dto) {

        dto.setId(id);
        return mobileGroupObservationFeature.updateObservation(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{groupId:\\d+}/rejectentry")
    public void rejectEntryFromHuntingGroup(@PathVariable final long groupId,
                                            @RequestBody @Validated final RejectClubDiaryEntryDTO dto) {
        dto.setGroupId(groupId);
        groupHuntingDiaryFeature.rejectDiaryEntryFromHuntingGroup(dto);
    }
}
