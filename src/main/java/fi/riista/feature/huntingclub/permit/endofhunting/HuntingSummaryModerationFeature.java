package fi.riista.feature.huntingclub.permit.endofhunting;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.permit.HasHarvestCountsForPermit;
import fi.riista.feature.huntingclub.permit.endofhunting.basicsummary.BasicClubHuntingSummary;
import fi.riista.feature.huntingclub.permit.endofhunting.basicsummary.BasicClubHuntingSummaryDTO;
import fi.riista.feature.huntingclub.permit.endofhunting.basicsummary.BasicClubHuntingSummaryRepository;
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.MooseHuntingSummaryRepository;
import fi.riista.feature.huntingclub.permit.partner.ClubIsNotPermitPartnerException;
import fi.riista.feature.huntingclub.permit.partner.HarvestPermitPartnerAreaService;
import fi.riista.feature.huntingclub.permit.statistics.ClubHuntingSummaryBasicInfoDTO;
import fi.riista.feature.huntingclub.permit.statistics.ClubHuntingSummaryBasicInfoService;
import fi.riista.feature.huntingclub.permit.statistics.HarvestCountDTO;
import fi.riista.feature.huntingclub.permit.statistics.HarvestCountService;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

@Service
public class HuntingSummaryModerationFeature {
    private static final Logger LOG = LoggerFactory.getLogger(HuntingSummaryModerationFeature.class);

    @Resource
    private MooseHuntingSummaryRepository mooseHuntingSummaryRepository;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Resource
    private HarvestPermitSpeciesAmountRepository harvestPermitSpeciesAmountRepository;

    @Resource
    private BasicClubHuntingSummaryRepository basicHuntingSummaryRepository;

    @Resource
    private UserRepository userRepository;

    @Resource
    private HarvestCountService harvestCountService;

    @Resource
    private HarvestPermitPartnerAreaService harvestPermitPartnerAreaService;

    @Resource
    private ClubHuntingSummaryBasicInfoService clubHuntingSummaryBasicInfoService;

    @Resource
    private RequireEntityService requireEntityService;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    @Transactional(readOnly = true)
    public List<BasicClubHuntingSummaryDTO> getHuntingSummariesForModeration(final long permitId,
                                                                             final int speciesCode) {

        final HarvestPermit permit = requireEntityService.requireHarvestPermit(permitId, EntityPermission.READ);

        requireNonNull(permit, "permit is null");

        final Function<HuntingClub, Integer> permitAreaSizeLookup =
                harvestPermitPartnerAreaService.getPermitAreaSizeLookupWithFallback(permit);

        final Map<Long, HarvestCountDTO> harvestCounts =
                harvestCountService.countHarvestsGroupingByClubId(permit, speciesCode);

        final Map<Long, ClubHuntingSummaryBasicInfoDTO> huntingSummariesGroupedByClubId =
                clubHuntingSummaryBasicInfoService.getHuntingSummariesGroupedByClubId(permit, speciesCode);

        return permit.getPermitPartners().stream().sorted(comparing(HuntingClub::getNameFinnish)).map(club -> {
            final ClubHuntingSummaryBasicInfoDTO huntingBasicInfo = huntingSummariesGroupedByClubId.get(club.getId());
            final BasicClubHuntingSummaryDTO dto = new BasicClubHuntingSummaryDTO();
            dto.setId(huntingBasicInfo.getModeratorOverrideSummaryId());
            dto.setRev(huntingBasicInfo.getModeratorOverrideSummaryRevision());

            dto.setHarvestPermitId(permit.getId());
            dto.setGameSpeciesCode(speciesCode);
            dto.setPermitAreaSize(permitAreaSizeLookup.apply(club));

            dto.setClubId(club.getId());
            dto.setNameFI(club.getNameFinnish());
            dto.setNameSV(club.getNameSwedish());

            dto.setHarvestCounts(harvestCounts.get(club.getId()));
            dto.setHuntingFinished(huntingBasicInfo.isHuntingFinished());
            dto.setHuntingEndDate(huntingBasicInfo.getHuntingEndDate());
            dto.setModeratorOverridden(huntingBasicInfo.isHuntingFinishedByModeration());

            dto.setTotalHuntingArea(huntingBasicInfo.getTotalHuntingArea());
            dto.setEffectiveHuntingArea(huntingBasicInfo.getEffectiveHuntingArea());

            dto.setRemainingPopulationInTotalArea(huntingBasicInfo.getRemainingPopulationInTotalArea());
            dto.setRemainingPopulationInEffectiveArea(huntingBasicInfo.getRemainingPopulationInEffectiveArea());

            return dto;
        }).collect(toList());
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    @Transactional
    public void processModeratorOverriddenHuntingSummaries(final long permitId,
                                                           final int gameSpeciesCode,
                                                           final boolean completeNonModeratedSummaries,
                                                           @Nonnull final List<BasicClubHuntingSummaryDTO> summaries) {

        requireNonNull(summaries);

        // Check integrity/consistency of DTO parameters with other method parameters and
        // flag as created within moderation.
        summaries.forEach(dto -> {
            if (dto.getHarvestPermitId() != permitId) {
                throw new IllegalArgumentException("DTO is not consistent with permitId parameter");
            }
            if (dto.getGameSpeciesCode() != gameSpeciesCode) {
                throw new IllegalArgumentException("DTO is not consistent with gameSpeciesCode parameter");
            }
        });

        final HarvestPermit harvestPermit = harvestPermitRepository.getOne(permitId);
        final HarvestPermitSpeciesAmount speciesAmount = harvestPermitSpeciesAmountRepository
                .getOneByHarvestPermitAndSpeciesCode(harvestPermit, gameSpeciesCode);

        if (!speciesAmount.isHuntingFinishedByModerator()) {
            speciesAmount.assertMooselikeHuntingNotFinished();
        }

        final List<BasicClubHuntingSummaryDTO> overriddenSummaries = summaries.stream()
                .filter(BasicClubHuntingSummaryDTO::isModeratorOverridden)
                .collect(toList());

        final LocalDate lastPermittedHuntingDate = speciesAmount.getLastDate();

        // Will be mutated (items removed) within next DTO list traversal.
        final Set<Long> idsOfClubsNotOverridden = F.getUniqueIds(harvestPermit.getPermitPartners());
        final Map<Long, HuntingClub> partnerClubIndex = F.indexById(harvestPermit.getPermitPartners());

        final Function<HuntingClub, Integer> partnerAreaSizeLookup =
                harvestPermitPartnerAreaService.getPermitAreaSizeLookupWithFallback(harvestPermit);

        overriddenSummaries.forEach(summaryDTO -> {
            final long clubId = summaryDTO.getClubId();
            final HuntingClub club = partnerClubIndex.get(clubId);

            if (club == null) {
                throw ClubIsNotPermitPartnerException.create(clubId, permitId);
            }

            idsOfClubsNotOverridden.remove(clubId);

            final Integer partnerAreaSize = partnerAreaSizeLookup.apply(club);
            final AreaSizeAndRemainingPopulation areaSizeAndRemainingPopulation = summaryDTO.getAreaSizeAndRemainingPopulation();
            final HasHarvestCountsForPermit harvestCounts = summaryDTO.getHarvestCounts();
            final LocalDate huntingEndDate = Optional
                    .ofNullable(summaryDTO.getHuntingEndDate())
                    .orElse(lastPermittedHuntingDate);

            InvalidHuntingEndDateException.assertDateValid(speciesAmount, huntingEndDate);

            AreaSizeAssertionHelper.assertGivenAreaSizeToPermitAreaSize(
                    partnerAreaSize,
                    areaSizeAndRemainingPopulation.getTotalHuntingArea(),
                    areaSizeAndRemainingPopulation.getEffectiveHuntingArea());

            final BasicClubHuntingSummary overrideBasicSummary = summaryDTO.getId() != null
                    ? basicHuntingSummaryRepository.getOne(summaryDTO.getId())
                    : new BasicClubHuntingSummary(club, speciesAmount);

            overrideBasicSummary.doModeratorOverride(huntingEndDate, areaSizeAndRemainingPopulation, harvestCounts);

            basicHuntingSummaryRepository.save(overrideBasicSummary);
        });

        // Finish hunting for all clubs not yet having hunting finished.
        if (completeNonModeratedSummaries && !idsOfClubsNotOverridden.isEmpty()) {
            final List<? extends MutableHuntingEndStatus> huntingFinishedTargets = GameSpecies.isMoose(gameSpeciesCode)
                    ? mooseHuntingSummaryRepository.findByHarvestPermit(harvestPermit)
                    : basicHuntingSummaryRepository.findBySpeciesAmount(speciesAmount);

            huntingFinishedTargets.forEach(summary -> {

                if (!summary.isHuntingFinished() && idsOfClubsNotOverridden.contains(summary.getClubId())) {
                    summary.setHuntingFinished(true);

                    if (summary.getHuntingEndDate() == null) {
                        summary.setHuntingEndDate(lastPermittedHuntingDate);
                    }
                }
            });
        }

        LOG.info("Moderator overrode {} club hunting summaries for {}", overriddenSummaries.size(), String.format(
                "HarvestPermitSpeciesAmount { harvestPermitId: %d, speciesCode: %d }", permitId, gameSpeciesCode));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    @Transactional
    public void revokeHuntingSummaryModeration(final long permitId, final int gameSpeciesCode) {
        final HarvestPermit harvestPermit = harvestPermitRepository.getOne(permitId);
        final HarvestPermitSpeciesAmount speciesAmount = harvestPermitSpeciesAmountRepository
                .getOneByHarvestPermitAndSpeciesCode(harvestPermit, gameSpeciesCode);

        speciesAmount.setMooselikeHuntingFinished(false);

        final List<BasicClubHuntingSummary> moderatedSummaries =
                basicHuntingSummaryRepository.findModeratorOverriddenHuntingSummaries(speciesAmount);

        if (GameSpecies.isMoose(gameSpeciesCode)) {
            basicHuntingSummaryRepository.deleteInBatch(moderatedSummaries);
        } else {
            moderatedSummaries.forEach(BasicClubHuntingSummary::revokeModeratorOverride);

            // Delete summaries that are empty and originally created by admin or moderator.
            final List<BasicClubHuntingSummary> emptySummaries = moderatedSummaries.stream()
                    .filter(s -> s.isEmpty() && isCreatedByModerator(s))
                    .collect(toList());

            basicHuntingSummaryRepository.deleteInBatch(emptySummaries);
        }
    }

    private Boolean isCreatedByModerator(final BasicClubHuntingSummary s) {
        return Optional
                .ofNullable(s.getAuditFields().getCreatedByUserId())
                .flatMap(userRepository::findById)
                .map(SystemUser::isModeratorOrAdmin)
                .orElse(false);
    }
}
