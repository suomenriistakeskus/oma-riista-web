package fi.riista.feature.announcement.show;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.announcement.AnnouncementRepository;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.person.Person;
import org.joda.time.DateTime;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

@Component
public class MobileAnnouncementFeature {

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private AnnouncementRepository announcementRepository;

    @Resource
    private MobileAnnouncementDTOTransformer mobileAnnouncementDTOTransformer;

    @Transactional(readOnly = true)
    public List<MobileAnnouncementDTO> listMobileAnnouncements(final @Nonnull DateTime since,
                                                               final @Nonnull Pageable pageRequest) {
        final Person person = activeUserService.requireActivePerson();

        final ListAnnouncementFilter filter = new ListAnnouncementFilter()
                .withActiveUser(activeUserService.getActiveUserInfoOrNull())
                .withActivePersonOccupations(occupationRepository.findActiveByPerson(person))
                .withActivePersonRhy(person.getRhyMembership())
                .withCreatedAfter(since);

        return filterAnnouncements(filter, pageRequest);
    }

    private List<MobileAnnouncementDTO> filterAnnouncements(final ListAnnouncementFilter filter,
                                                            final Pageable pageRequest) {
        return filter.buildPredicate()
                .map(predicate -> announcementRepository.findAllAsSlice(predicate, pageRequest))
                .map(slice -> mobileAnnouncementDTOTransformer.transform(slice.getContent()))
                .orElseGet(Collections::emptyList);
    }
}
