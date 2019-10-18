package fi.riista.feature.permit.application.mooselike;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.HuntingClubDTO;
import fi.riista.feature.huntingclub.HuntingClubRepository;
import fi.riista.feature.huntingclub.QHuntingClub;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.util.F;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MooselikePermitApplicationHolderFeature {

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private HuntingClubRepository huntingClubRepository;

    @Resource
    private OccupationRepository occupationRepository;

    @Transactional(readOnly = true)
    public List<HuntingClubDTO> listAvailablePermitHolders(final long applicationId) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.updateApplication(applicationId);

        return F.mapNonNullsToList(findClubsAvailableForApplicationPermitHolder(application),
                c -> HuntingClubDTO.create(c, true, null, null));
    }

    private List<HuntingClub> findClubsAvailableForApplicationPermitHolder(final HarvestPermitApplication application) {
        final List<Long> contactPersonClubIds = occupationRepository.findActiveByPersonAndOrganisationTypes(
                application.getContactPerson(), EnumSet.of(OrganisationType.CLUB)).stream()
                .filter(occ -> occ.getOccupationType() == OccupationType.SEURAN_YHDYSHENKILO)
                .map(Occupation::getOrganisation)
                .map(Organisation::getId)
                .collect(Collectors.toList());

        final QHuntingClub CLUB = QHuntingClub.huntingClub;

        final Long currentPermitHolderId = F.getId(application.getHuntingClub());

        final Predicate predicate = new BooleanBuilder()
                // Club is a person and equal to application contact person
                .or(CLUB.clubPerson.eq(application.getContactPerson()))
                // Application contact person is club contact person
                .or(CLUB.id.in(contactPersonClubIds))
                // Always include currently selected permitHolder
                .or(currentPermitHolderId != null ? CLUB.id.eq(currentPermitHolderId) : null);

        return huntingClubRepository.findAllAsList(predicate);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public HuntingClubDTO findClubByOfficialCode(final String officialCode) {
        final HuntingClub club = huntingClubRepository.findByOfficialCode(officialCode);
        if (club == null) {
            throw new NotFoundException("Club not found by officialCode:" + officialCode);
        }
        return HuntingClubDTO.create(club, true, null, null);
    }

    @Transactional
    public void updateHuntingClub(final long applicationId, final HuntingClubDTO permitHolder) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.updateApplication(applicationId);

        final HuntingClub club = permitHolder == null
                ? null
                : huntingClubRepository.getOne(permitHolder.getId());

        if (club != null) {
            application.setHuntingClub(club);
            application.setPermitHolder(PermitHolder.createHolderForClub(club));
        } else {
            application.setHuntingClub(null);
            application.setPermitHolder(PermitHolder.createHolderForPerson(application.getContactPerson()));

        }
    }
}
