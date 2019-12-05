package fi.riista.feature.permit.decision.species;

import com.google.common.base.Preconditions;
import fi.riista.feature.common.entity.Has2BeginEndDates;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.amendment.AmendmentApplicationDataRepository;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.util.F;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

@Service
public class PermitDecisionSpeciesAmountService {

    @Nonnull
    static Generator createGenerator(final @Nonnull PermitDecision decision) {
        final HarvestPermitApplication application = requireNonNull(decision.getApplication());
        return new Generator(decision, application.getSubmitDate().toLocalDate(), application.getSpeciesAmounts());
    }

    @Resource
    private AmendmentApplicationDataRepository amendmentApplicationDataRepository;

    @Resource
    private HarvestPermitSpeciesAmountRepository harvestPermitSpeciesAmountRepository;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<PermitDecisionSpeciesAmount> createSpecies(final PermitDecision decision) {

        final HarvestPermitApplication application = decision.getApplication();

        switch (application.getHarvestPermitCategory()) {
            case MOOSELIKE:
                return createGenerator(decision).createAllForMooselike();

            case MOOSELIKE_NEW:
                return createGenerator(decision).createAllForAmendment(createAmendmentApplicationLookup(application));

            case BIRD:
                return createGenerator(decision).createForAllYears();
            case LARGE_CARNIVORE_BEAR:
            case LARGE_CARNIVORE_LYNX:
            case LARGE_CARNIVORE_LYNX_PORONHOITO:
            case LARGE_CARNIVORE_WOLF:
                return createGenerator(decision).createForCarnivore();
            case MAMMAL:
                return createGenerator(decision).createForAllYears();
            default:
                throw new IllegalArgumentException("Unsupported application category:" + application.getHarvestPermitCategory());
        }
    }

    private Function<Integer, Has2BeginEndDates> createAmendmentApplicationLookup(final HarvestPermitApplication application) {
        final HarvestPermit originalPermit =
                amendmentApplicationDataRepository.getByApplication(application).getOriginalPermit();

        return speciesCode -> harvestPermitSpeciesAmountRepository.getOneByHarvestPermitAndSpeciesCode(originalPermit
                , speciesCode);
    }

    public static class Generator {
        private final PermitDecision decision;
        private final LocalDate applicationDate;
        private final List<HarvestPermitApplicationSpeciesAmount> applicationSpeciesAmounts;

        Generator(final @Nonnull PermitDecision decision,
                  final @Nonnull LocalDate applicationDate,
                  final @Nonnull List<HarvestPermitApplicationSpeciesAmount> applicationSpeciesAmounts) {
            this.decision = requireNonNull(decision);
            this.applicationDate = requireNonNull(applicationDate);
            this.applicationSpeciesAmounts = requireNonNull(applicationSpeciesAmounts);
        }

        public List<PermitDecisionSpeciesAmount> createAllForMooselike() {
            return F.mapNonNullsToList(applicationSpeciesAmounts, this::createMooselike);
        }

        public List<PermitDecisionSpeciesAmount> createForCarnivore() {
            Preconditions.checkState(applicationSpeciesAmounts.size() == 1,
                    "Carnivore application should have exactly one species");
            return F.mapNonNullsToList(applicationSpeciesAmounts, this::createCarnivore);
        }

        public List<PermitDecisionSpeciesAmount> createAllForAmendment(final Function<Integer, Has2BeginEndDates> lookupOriginalAmount) {
            return F.mapNonNullsToList(applicationSpeciesAmounts, source -> {
                final int speciesCode = source.getGameSpecies().getOfficialCode();
                return createAmendment(lookupOriginalAmount.apply(speciesCode), source);
            });
        }

        public List<PermitDecisionSpeciesAmount> createForAllYears() {
            return applicationSpeciesAmounts.stream()
                    .flatMap(source -> streamValidityYears(source).mapToObj(year -> createForYear(source, year)))
                    .collect(toList());
        }

        private PermitDecisionSpeciesAmount createCommon(final HarvestPermitApplicationSpeciesAmount source) {
            final PermitDecisionSpeciesAmount target = new PermitDecisionSpeciesAmount();
            target.setPermitDecision(decision);
            target.setGameSpecies(source.getGameSpecies());
            target.setAmount(source.getAmount());
            return target;
        }

        // MOOSE

        @Nonnull
        private PermitDecisionSpeciesAmount createMooselike(final HarvestPermitApplicationSpeciesAmount source) {
            final PermitDecisionSpeciesAmount target = createCommon(source);
            target.setBeginDate(PermitDecisionSpeciesAmount.getDefaultMooselikeBeginDate(decision.getDecisionYear()));
            target.setEndDate(PermitDecisionSpeciesAmount.getDefaultMooselikeEndDate(decision.getDecisionYear()));

            return target;
        }

        @Nonnull
        private PermitDecisionSpeciesAmount createAmendment(final Has2BeginEndDates originalPermitValidity,
                                                            final HarvestPermitApplicationSpeciesAmount source) {
            final PermitDecisionSpeciesAmount target = createCommon(source);
            target.copyDatesFrom(originalPermitValidity);
            return target;
        }

        // MULTI-YEAR APPLICATIONS ( bird, mammal)

        @Nonnull
        private PermitDecisionSpeciesAmount createForYear(final HarvestPermitApplicationSpeciesAmount source,
                                                          final int year) {
            final PermitDecisionSpeciesAmount target = createCommon(source);
            target.setBeginDate(getBeginDateForYear(source, year));
            target.setEndDate(getEndDateForYear(source, year));

            return target;
        }

        @Nonnull
        private LocalDate getBeginDateForYear(final HarvestPermitApplicationSpeciesAmount source, final int year) {
            return year == 0 ? getFirstYearBeginDate(source) : source.getBeginDate().plusYears(year);
        }

        @Nonnull
        private LocalDate getEndDateForYear(final HarvestPermitApplicationSpeciesAmount source, final int year) {
            return source.getEndDate().plusYears(year);
        }

        @Nonnull
        private LocalDate getFirstYearBeginDate(final HarvestPermitApplicationSpeciesAmount source) {
            if (applicationDate.isAfter(source.getBeginDate()) && source.getEndDate().isAfter(applicationDate)) {
                return applicationDate;
            }

            return source.getBeginDate();
        }

        @Nonnull
        private static IntStream streamValidityYears(final HarvestPermitApplicationSpeciesAmount source) {
            return source.getValidityYears() > 0
                    ? IntStream.range(0, source.getValidityYears())
                    : IntStream.of(0);
        }

        // CARNIVORE

        @Nonnull
        private PermitDecisionSpeciesAmount createCarnivore(final HarvestPermitApplicationSpeciesAmount source) {
            final PermitDecisionSpeciesAmount target = createCommon(source);
            target.setBeginDate(requireNonNull(source.getBeginDate()));
            target.setEndDate(requireNonNull(source.getEndDate()));

            return target;
        }
    }
}
