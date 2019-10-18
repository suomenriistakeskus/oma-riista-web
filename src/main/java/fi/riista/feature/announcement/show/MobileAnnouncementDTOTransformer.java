package fi.riista.feature.announcement.show;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.announcement.Announcement;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.Riistakeskus;
import fi.riista.util.DateUtil;
import fi.riista.util.ListTransformer;
import fi.riista.util.LocalisedString;
import fi.riista.util.jpa.CriteriaUtils;
import org.joda.time.LocalDateTime;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class MobileAnnouncementDTOTransformer extends ListTransformer<Announcement, MobileAnnouncementDTO> {

    @Resource
    private OrganisationRepository organisationRepository;

    @Resource
    private UserRepository userRepository;

    @Resource
    private EnumLocaliser enumLocaliser;

    @Nonnull
    @Override
    protected List<MobileAnnouncementDTO> transform(@Nonnull final List<Announcement> list) {
        if (list.isEmpty()) {
            return Collections.emptyList();
        }

        final Function<Announcement, Organisation> fromOrganisationMapping = getFromOrganisationMapping(list);
        final Function<Announcement, SystemUser> fromUserMapping = getFromUserMapping(list);
        final Organisation riistakeskus = organisationRepository.findByTypeAndOfficialCode(OrganisationType.RK, Riistakeskus.OFFICIAL_CODE);

        return list.stream().map(announcement -> {
            final Organisation fromOrganisation = fromOrganisationMapping.apply(announcement);
            final SystemUser fromUser = fromUserMapping.apply(announcement);

            return createDTO(announcement, fromOrganisation, fromUser, riistakeskus);

        }).collect(Collectors.toList());
    }

    @Nonnull
    private MobileAnnouncementDTO createDTO(final Announcement announcement,
                                            final Organisation fromOrganisation,
                                            final SystemUser fromUser,
                                            final Organisation riistakeskus) {
        Objects.requireNonNull(announcement);
        Objects.requireNonNull(fromOrganisation);

        final LocalisedString senderTypeLocalisation = enumLocaliser.getLocalisedString(announcement.getSenderType());
        final String senderFullName;
        final LocalisedString organisationName;

        switch (announcement.getSenderType()) {
            case TOIMINNANOHJAAJA:
            case SEURAN_YHDYSHENKILO:
                senderFullName = resolveSenderFullName(fromUser);
                organisationName = fromOrganisation.getNameLocalisation();
                break;
            case RIISTAKESKUS:
            default:
                // Exclude full sender name from RK
                senderFullName = "";
                organisationName = riistakeskus != null ? riistakeskus.getNameLocalisation() : LocalisedString.EMPTY;
                break;
        }

        final MobileAnnouncementSenderDTO senderDto = new MobileAnnouncementSenderDTO(
                organisationName.asMap(), senderTypeLocalisation.asMap(), senderFullName);

        final Date creationTime = announcement.getLifecycleFields().getCreationTime();
        final LocalDateTime pointOfTime = DateUtil.toLocalDateTimeNullSafe(creationTime);

        return new MobileAnnouncementDTO(
                announcement.getId(),
                announcement.getConsistencyVersion(),
                pointOfTime,
                senderDto,
                announcement.getSubject(),
                announcement.getBody());
    }

    private static String resolveSenderFullName(final SystemUser fromUser) {
        return fromUser.isModeratorOrAdmin() || fromUser.getPerson() == null ? "" : fromUser.getPerson().getFullName();
    }

    private Function<Announcement, Organisation> getFromOrganisationMapping(final Iterable<Announcement> announcements) {
        return CriteriaUtils.singleQueryFunction(announcements, Announcement::getFromOrganisation, organisationRepository, false);
    }

    private Function<Announcement, SystemUser> getFromUserMapping(final Iterable<Announcement> announcements) {
        return CriteriaUtils.singleQueryFunction(announcements, Announcement::getFromUser, userRepository, false);
    }
}
