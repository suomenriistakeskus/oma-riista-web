package fi.riista.feature.announcement.show;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.announcement.AnnouncementRepository;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDateTime;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    public List<MobileAnnouncementDTO> listMobileAnnouncements(final LocalDateTime since,
                                                               final Pageable pageRequest) {
        final ListAnnouncementFilter filter = new ListAnnouncementFilter(occupationRepository)
                .withSubscriberPerson(activeUserService.requireActivePerson());

        if (since != null) {
            filter.withCreatedAfter(DateUtil.toDateNullSafe(since));
        } else {
            filter.withCreatedAfterStartOfHuntingYearDate();
        }

        return filter
                .buildPredicate()
                .map(predicate -> announcementRepository.findAllAsSlice(predicate, pageRequest))
                .map(slice -> mobileAnnouncementDTOTransformer.transform(slice.getContent()))
                .orElseGet(Collections::emptyList);
    }
}
