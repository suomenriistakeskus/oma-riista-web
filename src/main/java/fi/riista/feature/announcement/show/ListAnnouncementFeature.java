package fi.riista.feature.announcement.show;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.announcement.Announcement;
import fi.riista.feature.announcement.AnnouncementRepository;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.OrganisationRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Optional;

import static java.util.Collections.emptyList;

@Component
public class ListAnnouncementFeature {
    @Resource
    private OrganisationRepository organisationRepository;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private AnnouncementRepository announcementRepository;

    @Resource
    private ListAnnouncementDTOTransformer announcementDTOTransformer;

    private Optional<Organisation> resolveOrganisation(final ListAnnouncementRequest request) {
        if (request.getOrganisationType() != null && request.getOfficialCode() != null) {
            return Optional.ofNullable(
                    organisationRepository.findByTypeAndOfficialCode(
                            request.getOrganisationType(),
                            request.getOfficialCode()));
        }
        return Optional.empty();
    }

    @Transactional(readOnly = true)
    public Slice<ListAnnouncementDTO> list(final ListAnnouncementRequest request, final Pageable pageRequest) {
        final ListAnnouncementFilter filter = new ListAnnouncementFilter(occupationRepository);

        if (!activeUserService.isModeratorOrAdmin()) {
            filter.withSubscriberPerson(activeUserService.requireActivePerson());
        }

        final Optional<Organisation> maybeOrganisation = resolveOrganisation(request);

        switch (request.getDirection()) {
            case SENT:
                maybeOrganisation.ifPresent(filter::withFromOrganisation);
                break;
            case RECEIVED:
                maybeOrganisation.ifPresent(filter::withSubscriberOrganisation);
                break;
            default:
                throw new IllegalArgumentException("Invalid direction");
        }

        return filter.buildPredicate()
                .map(predicate -> announcementRepository.findAllAsSlice(predicate, pageRequest))
                .map(slice -> transform(pageRequest, slice))
                .orElseGet(() -> new SliceImpl<>(emptyList()));
    }

    private Slice<ListAnnouncementDTO> transform(final Pageable pageRequest, final Slice<Announcement> slice) {
        return new SliceImpl<>(announcementDTOTransformer.transform(slice.getContent()), pageRequest, slice.hasNext());
    }
}
