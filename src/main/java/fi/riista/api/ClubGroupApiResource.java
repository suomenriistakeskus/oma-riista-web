package fi.riista.api;

import fi.riista.config.web.CSVHttpResponse;
import fi.riista.feature.gamediary.HuntingDiaryEntryDTO;
import fi.riista.feature.gamediary.GameDiaryEntryType;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountDTO;
import fi.riista.feature.huntingclub.copy.HuntingClubGroupCopyDTO;
import fi.riista.feature.huntingclub.group.GroupPermitFeature;
import fi.riista.feature.huntingclub.group.HuntingClubGroupCrudFeature;
import fi.riista.feature.huntingclub.group.HuntingClubGroupDTO;
import fi.riista.feature.huntingclub.group.excel.GroupMHCsvFeature;
import fi.riista.feature.huntingclub.hunting.GroupHuntingDiaryFeature;
import fi.riista.feature.huntingclub.hunting.GroupHuntingStatusDTO;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDayCrudFeature;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDayDTO;
import fi.riista.feature.huntingclub.hunting.excel.GroupHuntingDaysExcelFeature;
import fi.riista.feature.huntingclub.hunting.rejection.RejectClubDiaryEntryDTO;
import fi.riista.util.MediaTypeExtras;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.geojson.FeatureCollection;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1/club/{clubId:\\d+}/group", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class ClubGroupApiResource {

    @Resource
    private HuntingClubGroupCrudFeature crudFeature;

    @Resource
    private GroupHuntingDayCrudFeature groupHuntingDayCrudFeature;

    @Resource
    private GroupPermitFeature groupPermitFeature;

    @Resource
    private GroupHuntingDiaryFeature huntingFeature;

    @Resource
    private GroupHuntingDaysExcelFeature groupHuntingDaysExcelFeature;

    @Resource
    private GroupMHCsvFeature groupExcelFeature;

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
    @RequestMapping(value = "/{id:\\d+}/huntingArea", method = RequestMethod.GET, produces = MediaTypeExtras.APPLICATION_GEOJSON_VALUE)
    public FeatureCollection huntingAreaGeoJSON(@PathVariable @SuppressWarnings("unused") final long clubId,
                                                @PathVariable final long id) {
        return huntingFeature.huntingAreaGeoJSON(id);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/{id:\\d+}/status", method = RequestMethod.GET)
    public GroupHuntingStatusDTO getHuntingStatus(@PathVariable @SuppressWarnings("unused") final long clubId,
                                                  @PathVariable final long id) {
        return huntingFeature.getGroupHuntingStatus(id);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/{id:\\d+}/diary", method = RequestMethod.GET)
    public List<HuntingDiaryEntryDTO> getDiaryOfMembers(@PathVariable @SuppressWarnings("unused") final long clubId,
                                                        @PathVariable final long id) {
        return huntingFeature.getDiaryOfGroupMembers(id);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/{id:\\d+}/huntingdays", method = RequestMethod.GET)
    public List<GroupHuntingDayDTO> getHuntingDays(@PathVariable @SuppressWarnings("unused") final long clubId,
                                                   @PathVariable final long id) {
        return groupHuntingDayCrudFeature.findByClubGroup(id);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/{id:\\d+}/rejected", method = RequestMethod.GET)
    public Map<GameDiaryEntryType, List<Long>> listRejected(@PathVariable @SuppressWarnings("unused") final long clubId,
                                                            @PathVariable final long id) {
        return huntingFeature.listRejected(id);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{groupId:\\d+}/rejectentry", method = RequestMethod.POST)
    public void rejectEntryFromHuntingGroup(
            @PathVariable @SuppressWarnings("unused") long clubId,
            @PathVariable final long groupId,
            @RequestBody @Validated RejectClubDiaryEntryDTO dto) {
        dto.setGroupId(groupId);

        huntingFeature.rejectDiaryEntryFromHuntingGroup(dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/{id:\\d+}/export-hunting-days", method = RequestMethod.POST)
    public ModelAndView exportHuntingDays(@PathVariable @SuppressWarnings("unused") final long clubId,
                                          @PathVariable final long id) {

        return new ModelAndView(groupHuntingDaysExcelFeature.export(id));
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/export-groups",
            method = RequestMethod.POST,
            produces = MediaTypeExtras.TEXT_CSV_VALUE)
    public CSVHttpResponse exportGroups(@PathVariable final long clubId,
                                        @RequestParam final int year,
                                        @RequestParam(required = false) final Integer speciesCode) {

        return groupExcelFeature.export(clubId, year, speciesCode);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/{id:\\d+}/permit-species-amount", method = RequestMethod.GET)
    public HarvestPermitSpeciesAmountDTO groupSpeciesAmount(@PathVariable final long id) {
        return groupPermitFeature.getGroupPermitSpeciesAmount(id);
    }
}
