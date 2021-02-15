package fi.riista.api.club;

import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountDTO;
import fi.riista.feature.huntingclub.copy.HuntingClubGroupCopyDTO;
import fi.riista.feature.huntingclub.group.GroupPermitFeature;
import fi.riista.feature.huntingclub.group.HuntingClubGroupCrudFeature;
import fi.riista.feature.huntingclub.group.HuntingClubGroupDTO;
import fi.riista.feature.huntingclub.group.excel.HuntingClubGroupMemberExportFeature;
import fi.riista.feature.huntingclub.hunting.GroupHuntingAreaDTO;
import fi.riista.feature.huntingclub.hunting.GroupHuntingDiaryFeature;
import fi.riista.feature.huntingclub.hunting.GroupHuntingStatusDTO;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/club/{clubId:\\d+}/group", produces = MediaType.APPLICATION_JSON_VALUE)
public class ClubGroupApiResource {

    @Resource
    private HuntingClubGroupCrudFeature crudFeature;

    @Resource
    private GroupPermitFeature groupPermitFeature;

    @Resource
    private GroupHuntingDiaryFeature huntingFeature;

    @Resource
    private HuntingClubGroupMemberExportFeature groupExcelFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(method = RequestMethod.GET)
    public List<HuntingClubGroupDTO> listAll(@PathVariable final long clubId) {
        return crudFeature.listByClub(clubId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/{id:\\d+}", method = RequestMethod.GET)
    public HuntingClubGroupDTO read(@PathVariable final long id) {
        return crudFeature.read(id);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/huntingyears", method = RequestMethod.GET)
    public List<Integer> listHuntingYears(@PathVariable final long clubId) {
        return crudFeature.listHuntingYears(clubId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/permits", method = RequestMethod.GET)
    public List<HuntingClubGroupDTO.PermitDTO> listAvailablePermits(@PathVariable final long clubId,
                                                                    @RequestParam final int gameSpeciesCode,
                                                                    @RequestParam final int huntingYear) {

        return groupPermitFeature.listAvailablePermits(clubId, gameSpeciesCode, huntingYear);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public HuntingClubGroupDTO createGroup(@PathVariable final long clubId,
                                           @RequestBody @Validated final HuntingClubGroupDTO dto) {

        dto.setClubId(clubId);
        return crudFeature.create(dto);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "/{id:\\d+}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public HuntingClubGroupDTO updateGroup(@PathVariable final long clubId,
                                           @PathVariable final long id,
                                           @RequestBody @Validated HuntingClubGroupDTO dto) {

        dto.setId(id);
        dto.setClubId(clubId);
        return crudFeature.update(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{id:\\d+}", method = RequestMethod.DELETE)
    public void deleteGroup(@PathVariable Long id) {
        crudFeature.delete(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "/{id:\\d+}/copy", method = RequestMethod.POST)
    public HuntingClubGroupDTO copyGroup(@PathVariable Long id, @RequestBody @Validated HuntingClubGroupCopyDTO dto) {
        return crudFeature.copy(id, dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/{id:\\d+}/huntingArea", method = RequestMethod.GET)
    public ResponseEntity<GroupHuntingAreaDTO> huntingArea(@PathVariable @SuppressWarnings("unused") final long clubId,
                                                           @PathVariable final long id) {
        final GroupHuntingAreaDTO dto = huntingFeature.groupHuntingArea(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.noContent().build();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/{id:\\d+}/status", method = RequestMethod.GET)
    public GroupHuntingStatusDTO getHuntingStatus(@PathVariable @SuppressWarnings("unused") final long clubId,
                                                  @PathVariable final long id) {
        return huntingFeature.getGroupHuntingStatus(id);
    }

    @PostMapping("/export-groups")
    public ModelAndView exportGroups(@PathVariable final long clubId,
                                     @RequestParam final int year,
                                     @RequestParam(required = false) final Integer speciesCode) {
        return new ModelAndView(groupExcelFeature.export(clubId, year, speciesCode));
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/{id:\\d+}/permit-species-amount", method = RequestMethod.GET)
    public HarvestPermitSpeciesAmountDTO groupSpeciesAmount(@PathVariable final long id) {
        return groupPermitFeature.getGroupPermitSpeciesAmount(id);
    }
}
