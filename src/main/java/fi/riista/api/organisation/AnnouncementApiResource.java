package fi.riista.api.organisation;

import fi.riista.feature.announcement.crud.AnnouncementCrudFeature;
import fi.riista.feature.announcement.crud.AnnouncementDTO;
import fi.riista.feature.announcement.show.ListAnnouncementDTO;
import fi.riista.feature.announcement.show.ListAnnouncementFeature;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.occupation.OccupationType;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/announcements")
public class AnnouncementApiResource {

    @Resource
    private AnnouncementCrudFeature announcementCrudFeature;

    @Resource
    private ListAnnouncementFeature listAnnouncementFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "occupationTypes", method = RequestMethod.GET)
    public HashMap<OrganisationType, Set<OccupationType>> getOccupationTypes(
            @RequestParam OrganisationType fromOrganisationType) {
        return AnnouncementCrudFeature.listSubscriberOccupationTypes(fromOrganisationType);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(method = RequestMethod.GET)
    public Slice<ListAnnouncementDTO> listAnnouncements(@RequestParam final OrganisationType organisationType,
                                                        @RequestParam final String officialCode,
                                                        Pageable pageRequest) {
        return listAnnouncementFeature.listForOrganisation(organisationType, officialCode, pageRequest);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(method = RequestMethod.POST)
    public void createAnnouncement(@RequestBody @Valid AnnouncementDTO dto) {
        announcementCrudFeature.createAnnouncement(dto);
    }

    @RequestMapping(value = "{id:\\d+}", method = RequestMethod.GET)
    public AnnouncementDTO readAnnouncement(@PathVariable long id) {
        return announcementCrudFeature.readAnnouncement(id);
    }

    @RequestMapping(value = "{id:\\d+}", method = RequestMethod.PUT)
    public void updateAnnouncement(@PathVariable long id, @RequestBody @Valid AnnouncementDTO dto) {
        dto.setId(id);
        announcementCrudFeature.updateAnnouncement(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = "{id:\\d+}", method = RequestMethod.DELETE)
    public void removeAnnouncement(@PathVariable long id) {
        announcementCrudFeature.removeAnnouncement(id);
    }
}
