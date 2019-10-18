package fi.riista.feature.announcement.show;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.announcement.AnnouncementRepository;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.person.Person;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Objects;

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

    @Transactional(readOnly = true)
    public Slice<ListAnnouncementDTO> listMine(final Pageable pageRequest) {
        if (activeUserService.isModeratorOrAdmin()) {
            return emptySlice();
        }

        final Person person = activeUserService.requireActivePerson();

        final ListAnnouncementFilter filter = new ListAnnouncementFilter()
                .withActiveUser(activeUserService.getActiveUserInfoOrNull())
                .withActivePersonOccupations(occupationRepository.findActiveByPerson(person))
                .withActivePersonRhy(person.getRhyMembership());

        return filterAnnouncements(filter, pageRequest);
    }

    @Transactional(readOnly = true)
    public Slice<ListAnnouncementDTO> listForOrganisation(final @Nonnull OrganisationType organisationType,
                                                          final @Nonnull String officialCode,
                                                          final @Nonnull Pageable pageRequest) {
        Objects.requireNonNull(organisationType);
        Objects.requireNonNull(officialCode);
        Objects.requireNonNull(pageRequest);

        final Organisation organisation = organisationRepository.findByTypeAndOfficialCode(organisationType, officialCode);

        if (organisation == null) {
            throw new IllegalArgumentException("Organisation filter is required");
        }

        final ListAnnouncementFilter filter = new ListAnnouncementFilter()
                .withActiveUser(activeUserService.getActiveUserInfoOrNull())
                .withOrganisation(organisation);

        if (!activeUserService.isModeratorOrAdmin()) {
            final Person person = activeUserService.requireActivePerson();

            filter.withActivePersonOccupations(occupationRepository.findActiveByPerson(person));
            filter.withActivePersonRhy(person.getRhyMembership());
        }

        return filterAnnouncements(filter, pageRequest);
    }

    private Slice<ListAnnouncementDTO> filterAnnouncements(final ListAnnouncementFilter filter,
                                                           final Pageable pageRequest) {
        return filter.buildPredicate()
                .map(predicate -> announcementRepository.findAllAsSlice(predicate, pageRequest))
                .map(slice -> announcementDTOTransformer.apply(slice, pageRequest))
                .orElseGet(ListAnnouncementFeature::emptySlice);
    }

    private static <T> SliceImpl<T> emptySlice() {
        return new SliceImpl<>(emptyList());
    }
}
