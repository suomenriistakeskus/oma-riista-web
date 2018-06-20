package fi.riista.feature.harvestpermit;

import com.google.common.base.Preconditions;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.harvest.HarvestDTOTransformer;
import fi.riista.feature.harvestpermit.endofhunting.MooseHarvestReportDTO;
import fi.riista.feature.harvestpermit.endofhunting.MooseHarvestReportRepository;
import fi.riista.feature.harvestpermit.statistics.HarvestPermitUsageDTO;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.permit.HuntingClubPermitDTO;
import fi.riista.feature.huntingclub.permit.HuntingClubPermitDTOFactory;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.security.EntityPermission;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    private MooseHarvestReportRepository mooseHarvestReportRepository;

    @Resource
    private HarvestPermitSpeciesAmountRepository speciesAmountRepo;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private HuntingClubPermitDTOFactory huntingClubPermitDTOFactory;

    @Resource
    private HarvestDTOTransformer harvestDTOTransformer;

    @Transactional(readOnly = true)
    public OrganisationNameDTO getRhyCode(long permitId) {
        final HarvestPermit permit = requireEntityService.requireHarvestPermit(permitId, EntityPermission.READ);
        return OrganisationNameDTO.createWithOfficialCode(permit.getRhy());
    }

    @Transactional(readOnly = true)
    public HarvestPermitDTO getPermit(final long harvestPermitId) {
        final SystemUser activeUser = activeUserService.requireActiveUser();
        final HarvestPermit harvestPermit = requireEntityService.requireHarvestPermit(harvestPermitId, EntityPermission.READ);

        final Set<Integer> gameSpeciesCodes = harvestPermit.getSpeciesAmounts().stream()
                .map(HarvestPermitSpeciesAmount::getGameSpecies)
                .map(GameSpecies::getOfficialCode)
                .collect(Collectors.toSet());

        return new HarvestPermitDTO(harvestPermit, gameSpeciesCodes,
                harvestPermit.canAddHarvest(activeUser),
                harvestPermit.canCreateEndOfHuntingReport(activeUser));
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

        if (harvestPermit.isMooselikePermitType()) {
            throw new IllegalArgumentException("Cannot fetch harvest for moose permit");
        }

        final List<Harvest> sortedHarvests = harvestPermit.getHarvests().stream()
                .sorted(comparing(Harvest::getStateAcceptedToHarvestPermit, nullsLast(naturalOrder()))
                        .thenComparing(Harvest::getPointOfTime, reverseOrder()))
                .collect(toList());

        return resolveHarvestCreators(harvestPermit, harvestDTOTransformer.apply(sortedHarvests));
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

        final HuntingClubPermitDTO dto = huntingClubPermitDTOFactory.getPermitWithoutAuthorization(permit, speciesCode, huntingClubId);
        final HarvestPermitSpeciesAmount hpsa =
                speciesAmountRepo.getOneByHarvestPermitIdAndSpeciesCode(harvestPermitId, speciesCode);

        dto.setMooseHarvestReport(MooseHarvestReportDTO.create(mooseHarvestReportRepository.findBySpeciesAmount(hpsa)));
        return dto;
    }

    @Transactional(readOnly = true)
    public HuntingClubPermitDTO getClubPermit(long permitId, int officialCodeMoose) {
        final HarvestPermit permit = requireEntityService.requireHarvestPermit(permitId, EntityPermission.READ);
        return huntingClubPermitDTOFactory.getPermitWithoutAuthorization(permit, officialCodeMoose, null);
    }
}
