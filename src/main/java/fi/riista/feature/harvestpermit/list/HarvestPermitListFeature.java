package fi.riista.feature.harvestpermit.list;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpecs;
import fi.riista.feature.harvestpermit.HarvestPermit_;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.permit.HuntingClubPermitDTOFactory;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.security.EntityPermission;
import fi.riista.util.jpa.JpaSubQuery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Locale;

import static fi.riista.util.jpa.JpaSpecs.equal;
import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.toList;
import static org.springframework.data.jpa.domain.Specifications.where;

@Component
public class HarvestPermitListFeature {

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Resource
    private HuntingClubPermitDTOFactory huntingClubPermitDTOFactory;

    @Resource
    private PersonRepository personRepository;

    // PERSON

    @Transactional(readOnly = true)
    public List<ListHarvestPermitDTO> listPermitsForPerson(final Long personId) {
        final Person person = activeUserService.isModeratorOrAdmin()
                ? personRepository.getOne(personId)
                : activeUserService.requireActivePerson();

        return harvestPermitRepository.findAll(where(HarvestPermitSpecs.isPermitContactPerson(person))
                .and(HarvestPermitSpecs.IS_NOT_MOOSELIKE_AMENDMENT_PERMIT)).stream()
                .map(ListHarvestPermitDTO::create)
                .sorted(comparingLong(ListHarvestPermitDTO::getId).reversed())
                .collect(toList());
    }

    // CLUB

    @Transactional(readOnly = true)
    public List<MooselikePermitListDTO> listClubPermits(final long huntingClubId, final int huntingYear, final int speciesCode) {
        final HuntingClub club = requireEntityService.requireHuntingClub(huntingClubId, EntityPermission.READ);

        return harvestPermitRepository.findAll(spec(club, huntingYear, speciesCode))
                .stream()
                .map(p -> huntingClubPermitDTOFactory.getListDTO(p, speciesCode, huntingClubId))
                .collect(toList());
    }

    private static Specifications<HarvestPermit> spec(final HuntingClub club, final int huntingYear, int speciesCode) {
        return clubPredicate(club)
                .and(HarvestPermitSpecs.validWithinHuntingYear(huntingYear))
                .and(HarvestPermitSpecs.IS_MOOSELIKE_PERMIT)
                .and(HarvestPermitSpecs.withSpeciesCode(speciesCode));
    }

    private static Specifications<HarvestPermit> clubPredicate(final HuntingClub club) {
        final Specification<HarvestPermit> clubIsPermitHolder = equal(HarvestPermit_.huntingClub, club);
        final Specification<HarvestPermit> clubIsPermitPartner =
                JpaSubQuery.of(HarvestPermit_.permitPartners).exists((root, cb) -> cb.equal(root, club));

        return Specifications.where(clubIsPermitHolder).or(clubIsPermitPartner);
    }

    @Transactional(readOnly = true)
    public List<MooselikeHuntingYearDTO> listClubPermitHuntingYears(final long huntingClubId) {
        final HuntingClub club = requireEntityService.requireHuntingClub(huntingClubId, EntityPermission.READ);

        return MooselikeHuntingYearDTO.create(harvestPermitRepository.findAll(clubPredicate(club)));
    }

    // RHY

    @Transactional(readOnly = true)
    public List<MooselikePermitListDTO> listRhyMooselikePermits(final long rhyId,
                                                                final int year,
                                                                final int mooselikeSpeciesCode,
                                                                final Locale locale) {
        userAuthorizationHelper.assertCoordinatorOrModerator(rhyId);

        return harvestPermitRepository.findMooselikePermits(rhyId, year, mooselikeSpeciesCode).stream()
                .map(p -> {
                    final MooselikePermitListDTO dto =
                            huntingClubPermitDTOFactory.getListDTO(p, mooselikeSpeciesCode, null);
                    dto.setCurrentlyViewedRhyIsRelated(rhyId != p.getRhy().getId());
                    return dto;
                })
                .sorted(comparing(MooselikePermitListDTO::isCurrentlyViewedRhyIsRelated)
                        .thenComparing(p -> p.getPermitHolder().getNameLocalisation().getAnyTranslation(locale)))
                .collect(toList());
    }

    @Transactional(readOnly = true)
    public List<MooselikeHuntingYearDTO> listRhyMooselikeHuntingYears(final long rhyId) {
        userAuthorizationHelper.assertCoordinatorOrModerator(rhyId);

        return harvestPermitRepository.listRhyMooselikeHuntingYears(rhyId);
    }

}
