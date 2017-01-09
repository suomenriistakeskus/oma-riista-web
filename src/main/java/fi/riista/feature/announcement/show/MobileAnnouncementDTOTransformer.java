package fi.riista.feature.announcement.show;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.announcement.Announcement;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.util.ListTransformer;
import fi.riista.util.jpa.CriteriaUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
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

        return list.stream().map(announcement -> {
            final Organisation fromOrganisation = fromOrganisationMapping.apply(announcement);
            final SystemUser fromUser = fromUserMapping.apply(announcement);

            return MobileAnnouncementDTO.create(announcement, fromOrganisation, fromUser, enumLocaliser);

        }).collect(Collectors.toList());
    }

    private Function<Announcement, Organisation> getFromOrganisationMapping(final Iterable<Announcement> announcements) {
        return CriteriaUtils.singleQueryFunction(announcements, Announcement::getFromOrganisation, organisationRepository, false);
    }

    private Function<Announcement, SystemUser> getFromUserMapping(final Iterable<Announcement> announcements) {
        return CriteriaUtils.singleQueryFunction(announcements, Announcement::getFromUser, userRepository, false);
    }
}
