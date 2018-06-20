package fi.riista.feature.huntingclub.permit.summary;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.feature.harvestpermit.endofhunting.MooseHarvestReportDTO;
import fi.riista.feature.harvestpermit.endofhunting.MooseHarvestReportFeature;
import fi.riista.feature.harvestpermit.endofhunting.MooseHarvestReportRepository;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.HuntingClubRepository;
import fi.riista.feature.huntingclub.permit.HuntingClubPermitService;
import fi.riista.feature.huntingclub.permit.basicsummary.BasicClubHuntingSummary;
import fi.riista.feature.huntingclub.permit.basicsummary.BasicClubHuntingSummaryCrudFeature;
import fi.riista.feature.huntingclub.permit.basicsummary.BasicClubHuntingSummaryDTO;
import fi.riista.feature.huntingclub.permit.basicsummary.BasicClubHuntingSummaryDTOTransformer;
import fi.riista.feature.huntingclub.permit.basicsummary.BasicClubHuntingSummaryRepository;
import fi.riista.feature.huntingclub.permit.basicsummary.BasicClubHuntingSummary_;
import fi.riista.feature.organization.Organisation_;
import fi.riista.util.F;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import static fi.riista.util.Collect.idSet;
import static fi.riista.util.jpa.JpaSpecs.conjunction;
import static fi.riista.util.jpa.JpaSpecs.equal;
import static fi.riista.util.jpa.JpaSpecs.fetch;
import static fi.riista.util.jpa.JpaSpecs.inIdCollection;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.springframework.data.jpa.domain.Specifications.where;

@Service
public class ClubHuntingSummaryService {

    private static final Logger LOG = LoggerFactory.getLogger(ClubHuntingSummaryService.class);

    @Resource
    private MooseHuntingSummaryRepository mooseHuntingSummaryRepository;

    @Resource
    private HuntingClubRepository huntingClubRepository;

    @Resource
    private MooseHarvestReportRepository mooseHarvestReportRepository;

    @Resource
    private HarvestPermitSpeciesAmountRepository harvestPermitSpeciesAmountRepository;

    @Resource
    private BasicClubHuntingSummaryRepository basicHuntingSummaryRepository;

    @Resource
    private UserRepository userRepository;

    @Resource
    private BasicClubHuntingSummaryCrudFeature basicHuntingSummaryCrudFeature;

    @Resource
    private MooseHarvestReportFeature mooseHarvestReportCrudFeature;

    @Resource
    private BasicClubHuntingSummaryDTOTransformer basicSummaryTransformer;

    @Resource
    private HuntingClubPermitService huntingPermitService;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<BasicClubHuntingSummaryDTO> getHuntingSummariesForModeration(
            final HarvestPermit permit, final int speciesCode) {

        Objects.requireNonNull(permit, "permit is null");

        final HarvestPermitSpeciesAmount speciesAmount =
                harvestPermitSpeciesAmountRepository.getOneByHarvestPermitAndSpeciesCode(permit, speciesCode);

        final boolean isMoose = GameSpecies.isMoose(speciesCode);

        final List<BasicClubHuntingSummary> existingBasicSummaries = findRelatedBasicSummaries(speciesAmount, isMoose);
        final Set<Long> moderatedClubIds = existingBasicSummaries.stream()
                .filter(BasicClubHuntingSummary::isModeratorOverride)
                .map(BasicClubHuntingSummary::getClub)
                .collect(idSet());

        final Set<Long> allPartnerIds = F.getUniqueIds(permit.getPermitPartners());
        final Set<Long> idsOfNonModeratedPartners = allPartnerIds.stream()
                .filter(partnerId -> !moderatedClubIds.contains(partnerId))
                .collect(toSet());

        final Map<Long, BasicClubHuntingSummaryDTO> resultsIndexedByClubId =
                F.index(basicSummaryTransformer.apply(existingBasicSummaries), BasicClubHuntingSummaryDTO::getClubId);

        final Function<HuntingClub, BasicClubHuntingSummaryDTO> clubToDto = club -> {
            final BasicClubHuntingSummaryDTO dto = new BasicClubHuntingSummaryDTO();

            dto.setHarvestPermitId(permit.getId());
            dto.setGameSpeciesCode(speciesCode);
            dto.setPermitAreaSize(Objects.requireNonNull(permit.getPermitAreaSize(), "permitAreaSize is null"));

            dto.setClubId(club.getId());
            dto.setNameFI(club.getNameFinnish());
            dto.setNameSV(club.getNameSwedish());

            return dto;
        };

        if (isMoose && !idsOfNonModeratedPartners.isEmpty()) {
            final Specification<MooseHuntingSummary> mooseSummarySpec =
                    where(equal(MooseHuntingSummary_.harvestPermit, permit))
                            .and(inIdCollection(MooseHuntingSummary_.club, Organisation_.id, idsOfNonModeratedPartners))
                            .and(fetch(MooseHuntingSummary_.club));

            // Transform moose hunting summaries to result DTOs.
            mooseHuntingSummaryRepository.findAll(mooseSummarySpec).forEach(mooseSummary -> {
                final Integer permitArea = mooseSummary.getHarvestPermit().getPermitAreaSize();
                Objects.requireNonNull(permitArea, "Permit area is null, permit id:" + permit.getId());
                final AreaSizeAndRemainingPopulation fixedAreaSizeAndPopulation = mooseSummary.getAreaSizeAndPopulation()
                        .calculateMissingValues(mooseSummary.getEffectiveHuntingAreaPercentage(), permitArea);
                final BasicClubHuntingSummaryDTO dto = clubToDto.apply(mooseSummary.getClub())
                        .withAreaSizeAndRemainingPopulation(fixedAreaSizeAndPopulation);
                dto.setHuntingFinished(mooseSummary.isHuntingFinished());
                dto.setHuntingEndDate(mooseSummary.getHuntingEndDate());

                resultsIndexedByClubId.put(dto.getClubId(), dto);
            });
        }

        final Set<Long> idsOfPartnersWithoutHuntingSummary = allPartnerIds.stream()
                .filter(partnerId -> !resultsIndexedByClubId.containsKey(partnerId))
                .collect(toSet());

        if (!idsOfPartnersWithoutHuntingSummary.isEmpty()) {
            huntingClubRepository.findAll(idsOfPartnersWithoutHuntingSummary)
                    .forEach(club -> resultsIndexedByClubId.put(club.getId(), clubToDto.apply(club)));
        }

        // Populate harvest amounts for partners not yet assigned a moderated summary.
        huntingPermitService.calculateHarvests(permit, speciesCode, idsOfNonModeratedPartners)
                .forEach((clubId, harvestCounts) -> {
                    final BasicClubHuntingSummaryDTO dto = resultsIndexedByClubId.get(clubId);
                    dto.setNumberOfAdultMales(harvestCounts.getNumberOfAdultMales());
                    dto.setNumberOfAdultFemales(harvestCounts.getNumberOfAdultFemales());
                    dto.setNumberOfYoungMales(harvestCounts.getNumberOfYoungMales());
                    dto.setNumberOfYoungFemales(harvestCounts.getNumberOfYoungFemales());
                    dto.setNumberOfNonEdibleAdults(harvestCounts.getNumberOfNonEdibleAdults());
                    dto.setNumberOfNonEdibleYoungs(harvestCounts.getNumberOfNonEdibleYoungs());
                });

        return resultsIndexedByClubId.values().stream()
                .sorted(comparing(BasicClubHuntingSummaryDTO::getNameFI))
                .collect(toList());
    }

    private List<BasicClubHuntingSummary> findRelatedBasicSummaries(
            final HarvestPermitSpeciesAmount speciesAmount, final boolean filterModeratorOverridden) {

        return basicHuntingSummaryRepository.findAll(
                where(equal(BasicClubHuntingSummary_.speciesAmount, speciesAmount))
                        // additional integrity check for checking moderator override for moose species
                        .and(filterModeratorOverridden
                                ? equal(BasicClubHuntingSummary_.moderatorOverride, true)
                                : conjunction())
                        .and(fetch(BasicClubHuntingSummary_.club)));
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void processModeratorOverriddenHuntingSummaries(
            final long permitId,
            final int gameSpeciesCode,
            final boolean completeHuntingOfPermit,
            @Nonnull final List<BasicClubHuntingSummaryDTO> summaries) {

        Objects.requireNonNull(summaries);

        // Check integrity/consistency of DTO parameters with other method parameters and
        // flag as created within moderation.
        summaries.forEach(dto -> {
            if (dto.getHarvestPermitId() != permitId) {
                throw new IllegalArgumentException("DTO is not consistent with permitId parameter");
            }
            if (dto.getGameSpeciesCode() != gameSpeciesCode) {
                throw new IllegalArgumentException("DTO is not consistent with gameSpeciesCode parameter");
            }

            // Required by CRUD feature methods.
            dto.setCreatedWithinModeration(true);
        });

        final HarvestPermitSpeciesAmount speciesAmount = getOneSpeciesAmount(permitId, gameSpeciesCode);
        final HarvestPermit permit = speciesAmount.getHarvestPermit();

        mooseHarvestReportRepository.assertMooseHarvestReportNotDoneOrModeratorOverriden(speciesAmount);

        final List<BasicClubHuntingSummaryDTO> overriddenSummaries = summaries.stream()
                .filter(BasicClubHuntingSummaryDTO::isModeratorOverridden)
                .collect(toList());

        final LocalDate lastPermittedHuntingDate = speciesAmount.getLastDate();

        // Will be mutated (items removed) within next DTO list traversal.
        final Set<Long> idsOfClubsNotOverridden = F.getUniqueIds(permit.getPermitPartners());

        overriddenSummaries.forEach(summary -> {
            idsOfClubsNotOverridden.remove(summary.getClubId());

            summary.setHuntingFinished(true);

            if (summary.getHuntingEndDate() == null) {
                summary.setHuntingEndDate(lastPermittedHuntingDate);
            }

            if (F.hasId(summary)) {
                basicHuntingSummaryCrudFeature.update(summary);
            } else {
                basicHuntingSummaryCrudFeature.create(summary);
            }
        });

        // Finish hunting for all clubs not yet having hunting finished.
        if (completeHuntingOfPermit && !idsOfClubsNotOverridden.isEmpty()) {
            final List<? extends MutableHuntingEndStatus> huntingFinishedTargets = GameSpecies.isMoose(gameSpeciesCode)
                    ? mooseHuntingSummaryRepository.findByHarvestPermit(permit)
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

        final Supplier<String> formatter = () -> String.format(
                "HarvestPermitSpeciesAmount { harvestPermitId: %d, speciesCode: %d }", permitId, gameSpeciesCode);

        if (completeHuntingOfPermit && !huntingPermitService.allPartnersFinishedHunting(speciesAmount.getHarvestPermit(), gameSpeciesCode)) {
            throw new IllegalStateException(
                    "There still remains partners not having finished their hunting even after moderator " +
                            "requested hunting completion for " + formatter.get());
        }

        if (completeHuntingOfPermit && !mooseHarvestReportRepository.isMooseHarvestReportDone(speciesAmount)) {
            mooseHarvestReportCrudFeature.create(MooseHarvestReportDTO.withModeratorOverride(permitId, gameSpeciesCode));
        }

        LOG.info("Moderator overrode {} club hunting summaries for {}", overriddenSummaries.size(), formatter.get());
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void revokeHuntingSummaryModeration(final long permitId, final int gameSpeciesCode) {
        final HarvestPermitSpeciesAmount speciesAmount = getOneSpeciesAmount(permitId, gameSpeciesCode);

        final List<BasicClubHuntingSummary> moderatedSummaries =
                basicHuntingSummaryRepository.findModeratorOverriddenHuntingSummaries(speciesAmount);

        if (GameSpecies.isMoose(gameSpeciesCode)) {
            basicHuntingSummaryRepository.deleteInBatch(moderatedSummaries);
        } else {
            moderatedSummaries.forEach(BasicClubHuntingSummary::revokeModeratorOverride);

            // Delete summaries that are empty and originally created by admin or moderator.
            final List<BasicClubHuntingSummary> emptySummaries = moderatedSummaries.stream()
                    .filter(s -> {
                        return s.getBasicInfo().isEmpty() && Optional
                                .ofNullable(s.getAuditFields().getCreatedByUserId())
                                .map(userRepository::findOne)
                                .map(SystemUser::isModeratorOrAdmin)
                                .orElse(false);
                    })
                    .collect(toList());
            basicHuntingSummaryRepository.deleteInBatch(emptySummaries);
        }
        mooseHarvestReportCrudFeature.delete(permitId, gameSpeciesCode);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public MooseHuntingSummary markUnfinished(final MooseHuntingSummary summary) {
        mooseHarvestReportRepository.assertMooseHarvestReportNotDone(getOneSpeciesAmount(summary.getHarvestPermit().getId(), GameSpecies.OFFICIAL_CODE_MOOSE));
        summary.setHuntingFinished(false);

        // Flush needed to have DTO's rev updated.
        return mooseHuntingSummaryRepository.saveAndFlush(summary);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public BasicClubHuntingSummary markUnfinished(final BasicClubHuntingSummary summary) {
        mooseHarvestReportRepository.assertMooseHarvestReportNotDone(summary.getSpeciesAmount());
        summary.setHuntingFinished(false);

        // Flush needed to have DTO's rev updated.
        return basicHuntingSummaryRepository.saveAndFlush(summary);
    }

    private HarvestPermitSpeciesAmount getOneSpeciesAmount(Long permitId, int speciesCode) {
        return harvestPermitSpeciesAmountRepository.getOneByHarvestPermitIdAndSpeciesCode(permitId, speciesCode);
    }
}
