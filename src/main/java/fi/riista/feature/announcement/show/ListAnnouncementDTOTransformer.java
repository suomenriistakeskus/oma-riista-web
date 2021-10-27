package fi.riista.feature.announcement.show;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.announcement.Announcement;
import fi.riista.feature.announcement.AnnouncementSubscriber;
import fi.riista.feature.announcement.AnnouncementSubscriberRepository;
import fi.riista.feature.announcement.AnnouncementSubscriber_;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.util.ListTransformer;
import fi.riista.util.jpa.CriteriaUtils;
import fi.riista.util.jpa.JpaGroupingUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ListAnnouncementDTOTransformer extends ListTransformer<Announcement, ListAnnouncementDTO> {
    @Resource
    private OrganisationRepository organisationRepository;

    @Resource
    private UserRepository userRepository;

    @Resource
    private AnnouncementSubscriberRepository announcementSubscriberRepository;

    @Nonnull
    @Override
    protected List<ListAnnouncementDTO> transform(@Nonnull final List<Announcement> list) {
        if (list.isEmpty()) {
            return Collections.emptyList();
        }

        final Function<Announcement, Organisation> fromOrganisationMapping = getFromOrganisationMapping(list);
        final Function<Announcement, SystemUser> fromUserMapping = getFromUserMapping(list);
        final Map<Announcement, List<AnnouncementSubscriber>> subscriberMapping = getSubscribersGroupedByAnnouncement(list);
        final Function<Announcement, Organisation> rhySubscriberMapping = getRhySubscriberMapping(list);

        return list.stream().map(announcement -> {
            final Organisation fromOrganisation = fromOrganisationMapping.apply(announcement);
            final SystemUser fromUser = fromUserMapping.apply(announcement);
            final List<AnnouncementSubscriber> subscribers = subscriberMapping.get(announcement);
            final Organisation rhyMemberSubscriber = rhySubscriberMapping.apply(announcement);
            return ListAnnouncementDTO.create(announcement, subscribers, fromOrganisation, rhyMemberSubscriber, fromUser);

        }).collect(Collectors.toList());
    }

    private Function<Announcement, Organisation> getFromOrganisationMapping(final Iterable<Announcement> announcements) {
        return CriteriaUtils.singleQueryFunction(announcements, Announcement::getFromOrganisation, organisationRepository, false);
    }

    private Function<Announcement, Organisation> getRhySubscriberMapping(final Iterable<Announcement> announcements) {
        return CriteriaUtils.singleQueryFunction(announcements, Announcement::getRhyMembershipSubscriber, organisationRepository, false);
    }

    private Function<Announcement, SystemUser> getFromUserMapping(final Iterable<Announcement> announcements) {
        return CriteriaUtils.singleQueryFunction(announcements, Announcement::getFromUser, userRepository, false);
    }

    private Map<Announcement, List<AnnouncementSubscriber>> getSubscribersGroupedByAnnouncement(
            final Collection<Announcement> announcements) {
        return JpaGroupingUtils.groupRelations(announcements, AnnouncementSubscriber_.announcement, announcementSubscriberRepository);
    }
}
