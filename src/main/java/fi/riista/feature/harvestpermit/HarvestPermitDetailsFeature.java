package fi.riista.feature.harvestpermit;

import com.google.common.base.Preconditions;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.common.decision.GrantStatus;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.harvest.HarvestDTOTransformer;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.harvestpermit.statistics.HarvestPermitUsageDTO;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.permit.HuntingClubPermitDTO;
import fi.riista.feature.huntingclub.permit.HuntingClubPermitDTOFactory;
import fi.riista.feature.huntingclub.permit.statistics.MooselikePermitObservationService;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.PermitDecisionRepository;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static fi.riista.feature.harvestpermit.HarvestPermitAuthorization.Permission.VIEW_OBSERVATION_SUMMARY;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;
import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.toList;

@Component
public class HarvestPermitDetailsFeature {

    @Resource
    private UserRepository userRepository;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private HuntingClubPermitDTOFactory huntingClubPermitDTOFactory;

    @Resource
    private HarvestPermitSpeciesAmountRepository harvestPermitSpeciesAmountRepository;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Resource
    private HarvestDTOTransformer harvestDTOTransformer;

    @Resource
    private PermitDecisionRepository permitDecisionRepository;

    @Resource
    private MooselikePermitObservationService mooselikePermitObservationService;

    @Transactional(readOnly = true)
    public OrganisationNameDTO getRhyCode(long permitId) {
        final HarvestPermit permit = requireEntityService.requireHarvestPermit(permitId, EntityPermission.READ);
        return OrganisationNameDTO.createWithOfficialCode(permit.getRhy());
    }

    @Transactional(readOnly = true)
    public HarvestPermitDTO getPermit(final long harvestPermitId) {
        final SystemUser activeUser = activeUserService.requireActiveUser();
        final HarvestPermit harvestPermit = requireEntityService.requireHarvestPermit(harvestPermitId, EntityPermission.READ);

        final List<HarvestPermit> amendmentPermits = harvestPermit.isMooselikePermitType()
                ? harvestPermitRepository.findAmendmentPermits(harvestPermit) : emptyList();

        final List<HarvestPermitSpeciesAmount> amendmentPermitSpeciesAmounts = harvestPermit.isMooselikePermitType()
                ? harvestPermitSpeciesAmountRepository.getAmendmentPermitSpeciesAmounts(harvestPermit) : emptyList();

        final Set<String> amendmentPermitNumbers = F.mapNonNullsToSet(amendmentPermits, HarvestPermit::getPermitNumber);
        final List<String> cancelledAndIgnoredPermitNumbers = harvestPermit.isMooselikePermitType() ?
                permitDecisionRepository.findCancelledAndIgnoredPermitNumbersByOriginalPermit(harvestPermit) : emptyList();
        amendmentPermitNumbers.addAll(cancelledAndIgnoredPermitNumbers);

        final PermitDecision decision = harvestPermit.getPermitDecision();

        final GrantStatus grantStatus = Optional.ofNullable(decision)
                .map(PermitDecision::getGrantStatus)
                .orElse(null);


        return HarvestPermitDTO.create(harvestPermit, harvestPermit.getSpeciesAmounts(),
                amendmentPermitNumbers, amendmentPermitSpeciesAmounts,
                activeUser, grantStatus, decision);
    }

    @Transactional(readOnly = true)
    public List<HarvestPermitUsageDTO> getSpeciesAmountUsage(final long harvestPermitId) {
        final HarvestPermit harvestPermit = requireEntityService.requireHarvestPermit(harvestPermitId, EntityPermission.READ);

        return HarvestPermitUsageDTO.createUsage(harvestPermit.getSpeciesAmounts(),
                harvestPermit.getAcceptedHarvestForEndOfHuntingReport());
    }

    @Transactional(readOnly = true)
    public List<HarvestDTO> getHarvestForPermit(final long harvestPermitId) {
        final HarvestPermit harvestPermit = requireEntityService.requireHarvestPermit(harvestPermitId, EntityPermission.READ);

        if (harvestPermit.isMooselikePermitType() || harvestPermit.isAmendmentPermit()) {
            throw new IllegalArgumentException("Cannot fetch harvest for moose permit");
        }

        final List<Harvest> sortedHarvests = harvestPermit.getHarvests().stream()
                .sorted(comparing(Harvest::getStateAcceptedToHarvestPermit, nullsLast(naturalOrder()))
                        .thenComparing(Harvest::getPointOfTime, reverseOrder()))
                .collect(toList());

        final HarvestSpecVersion specVersion = HarvestSpecVersion.CURRENTLY_SUPPORTED;

        return resolveHarvestCreators(harvestPermit, harvestDTOTransformer.apply(sortedHarvests, specVersion));
    }

    private List<HarvestDTO> resolveHarvestCreators(final HarvestPermit permit, final List<HarvestDTO> harvests) {
        if (harvests.isEmpty()) {
            return harvests;
        }

        final Map<Long, String> moderatorIndex = userRepository.getModeratorFullNames(permit.getHarvests());
        final Map<Long, Long> harvestCreator = permit.getHarvests().stream()
                .collect(Collectors.toMap(Harvest::getId, Harvest::getCreatedByUserId));

        for (final HarvestDTO h : harvests) {
            h.setModeratorFullName(Optional.of(h.getId())
                    .map(harvestCreator::get)
                    .map(moderatorIndex::get)
                    .orElse(null));
        }

        return harvests;
    }

    @Transactional(readOnly = true)
    public HuntingClubPermitDTO getClubPermit(final long huntingClubId, final long harvestPermitId, final int speciesCode) {
        final HuntingClub club = requireEntityService.requireHuntingClub(huntingClubId, EntityPermission.READ);
        final HarvestPermit permit = requireEntityService.requireHarvestPermit(harvestPermitId, EntityPermission.READ);

        Preconditions.checkArgument(permit.isPermitHolderOrPartner(club), "Club is not permits holder or partner");

        return huntingClubPermitDTOFactory.getDTO(permit, speciesCode, huntingClubId);
    }

    @Transactional(readOnly = true)
    public HuntingClubPermitDTO getClubPermit(long permitId, int speciesCode) {
        final HarvestPermit permit = requireEntityService.requireHarvestPermit(permitId, EntityPermission.READ);

        return huntingClubPermitDTOFactory.getDTO(permit, speciesCode, null);
    }

    @Transactional(readOnly = true)
    public MooselikePermitObservationSummaryDTO getObservationSummary(final long permitId, final int speciesCode) {
        final HarvestPermit permit = requireEntityService.requireHarvestPermit(permitId, VIEW_OBSERVATION_SUMMARY);
        Preconditions.checkArgument(permit.isMooselikePermitType(), "Mooselike permit requided");

        return mooselikePermitObservationService.getObservationSummaryDTO(permit, speciesCode);
    }
}
