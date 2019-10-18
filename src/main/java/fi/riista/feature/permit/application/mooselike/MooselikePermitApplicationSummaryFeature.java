package fi.riista.feature.permit.application.mooselike;

import com.google.common.base.Preconditions;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.huntingclub.members.HuntingClubContactService;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class MooselikePermitApplicationSummaryFeature {

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private HuntingClubContactService huntingClubContactService;

    @Resource
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    public MooselikePermitApplicationSummaryDTO getAllDetails(final long applicationId) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.readApplication(applicationId);

        Preconditions.checkState(application.getHarvestPermitCategory() == HarvestPermitCategory.MOOSELIKE ||
                        application.getHarvestPermitCategory() == HarvestPermitCategory.MOOSELIKE_NEW,
                "Only mooselike application is supported");

        final Map<Long, List<Person>> contactPersonMapping =
                huntingClubContactService.getContactPersonsSorted(application.getPermitPartners());

        final Map<Long, String> moderatorIndex = userRepository.getModeratorFullNames(Collections.singletonList(application));
        final String moderatorName = moderatorIndex.get(application.getCreatedByUserId());
        return MooselikePermitApplicationSummaryDTO.create(application, contactPersonMapping, moderatorName);
    }

}
