package fi.riista.feature.announcement.crud;

import com.google.common.base.Preconditions;
import fi.riista.feature.announcement.Announcement;
import fi.riista.feature.announcement.AnnouncementSubscriber;
import fi.riista.feature.announcement.AnnouncementSubscriberRepository;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.util.F;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

@Service
public class AnnouncementSubscriberService {

    @Resource
    private AnnouncementSubscriberRepository announcementSubscriberRepository;

    @Resource
    private OrganisationRepository organisationRepository;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<AnnouncementSubscriber> listAll(final Announcement announcement) {
        return announcementSubscriberRepository.findByAnnouncement(announcement);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void create(final Announcement announcement, final AnnouncementDTO dto) {
        announcementSubscriberRepository.save(createSubscribers(dto, announcement));
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void update(final Announcement announcement, final AnnouncementDTO dto) {
        announcementSubscriberRepository.deleteByAnnouncement(announcement);
        announcementSubscriberRepository.save(createSubscribers(dto, announcement));
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void deleteAll(final Announcement announcement) {
        announcementSubscriberRepository.deleteByAnnouncement(announcement);
    }

    private List<AnnouncementSubscriber> createSubscribers(final AnnouncementDTO dto,
                                                           final Announcement announcement) {
        if (dto.isVisibleToAll() || dto.isVisibleToRhyMembers()) {
            return emptyList();
        }

        if (F.isNullOrEmpty(dto.getSubscriberOrganisations())) {
            return createSubscribers(announcement,
                    dto.getOccupationTypes(),
                    singletonList(announcement.getFromOrganisation()));
        }

        return createSubscribers(announcement,
                dto.getOccupationTypes(),
                loadOrganisations(dto.getSubscriberOrganisations()));
    }

    private List<Organisation> loadOrganisations(final Set<AnnouncementDTO.OrganisationDTO> dtoList) {
        Preconditions.checkArgument(!dtoList.isEmpty());

        return F.mapNonNullsToList(dtoList,
                dto -> organisationRepository.findByTypeAndOfficialCode(
                        dto.getOrganisationType(), dto.getOfficialCode()));
    }

    private static List<AnnouncementSubscriber> createSubscribers(final Announcement announcement,
                                                                  final Set<OccupationType> occupationTypes,
                                                                  final List<Organisation> organisationList) {
        Preconditions.checkArgument(!occupationTypes.isEmpty());
        Preconditions.checkArgument(!organisationList.isEmpty());

        return organisationList.stream().flatMap(organisation -> occupationTypes.stream()
                .map(occupationType -> new AnnouncementSubscriber(announcement, organisation, occupationType)))
                .collect(Collectors.toList());
    }

}
