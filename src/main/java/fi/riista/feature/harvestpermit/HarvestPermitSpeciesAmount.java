package fi.riista.feature.harvestpermit;

import com.querydsl.core.annotations.QueryDelegate;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.jpa.JPAExpressions;
import fi.riista.feature.common.entity.Has2BeginEndDates;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.harvestpermit.endofhunting.MooselikeHuntingFinishedException;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.joda.time.LocalDate;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;

@Entity
@Access(AccessType.FIELD)
public class HarvestPermitSpeciesAmount extends LifecycleEntity<Long> implements Has2BeginEndDates {

    public static final String ID_COLUMN_NAME = "harvest_permit_species_amount_id";

    public enum RestrictionType {
        /**
         * Aikuisia enintään
         */
        AE,

        /**
         * Aikuisia uroksia enintään
         */
        AU;

        public static RestrictionType ofNullable(String str) {
            return str == null ? null : valueOf(str);
        }
    }

    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HarvestPermit harvestPermit;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private GameSpecies gameSpecies;

    @Column(name = "amount")
    private Float specimenAmount;

    @Column
    private Integer nestAmount;

    @Column
    private Integer eggAmount;

    @Column
    private Integer constructionAmount;

    @NotNull
    @Column(nullable = false)
    private LocalDate beginDate;

    @NotNull
    @Column(nullable = false)
    private LocalDate endDate;

    @Column
    private LocalDate beginDate2;

    @Column
    private LocalDate endDate2;

    @Enumerated(EnumType.STRING)
    @Column
    private RestrictionType restrictionType;

    @Column
    private Float restrictionAmount;

    // Permit holder has finished hunting
    @Column(nullable = false)
    private boolean mooselikeHuntingFinished;

    // Hunting has been finished by moderator
    @Column(nullable = false)
    private boolean huntingFinishedByModerator;

    public HarvestPermitSpeciesAmount() {
    }

    public static HarvestPermitSpeciesAmount createForHarvest(final HarvestPermit harvestPermit,
                                                              final GameSpecies gameSpecies,
                                                              final float amount,
                                                              final RestrictionType restrictionType,
                                                              final Float restrictionAmount,
                                                              final LocalDate beginDate,
                                                              final LocalDate endDate) {
        final HarvestPermitSpeciesAmount spa = new HarvestPermitSpeciesAmount(harvestPermit,
                gameSpecies,
                restrictionType,
                restrictionAmount,
                beginDate,
                endDate);
        spa.setSpecimenAmount(amount);
        return spa;
    }

    public static HarvestPermitSpeciesAmount createForNestRemoval(final HarvestPermit harvestPermit,
                                                                  final GameSpecies gameSpecies,
                                                                  final Integer nestAmount,
                                                                  final Integer eggAmount,
                                                                  final Integer constructionAmount,
                                                                  final RestrictionType restrictionType,
                                                                  final Float restrictionAmount,
                                                                  final LocalDate beginDate,
                                                                  final LocalDate endDate) {
        final HarvestPermitSpeciesAmount spa = new HarvestPermitSpeciesAmount(harvestPermit,
                gameSpecies,
                restrictionType,
                restrictionAmount,
                beginDate,
                endDate);
        spa.setNestAmount(nestAmount);
        spa.setEggAmount(eggAmount);
        spa.setConstructionAmount(constructionAmount);
        return spa;
    }

    public static HarvestPermitSpeciesAmount createWithSpecimenOrEggs(final HarvestPermit harvestPermit,
                                                                      final GameSpecies gameSpecies,
                                                                      final Float specimenAmount,
                                                                      final Integer eggAmount,
                                                                      final LocalDate beginDate,
                                                                      final LocalDate endDate) {

        checkArgument(F.firstNonNull(specimenAmount, eggAmount) != null);

        final HarvestPermitSpeciesAmount spa = new HarvestPermitSpeciesAmount(harvestPermit,
                gameSpecies,
                null,
                null,
                beginDate,
                endDate);
        spa.setSpecimenAmount(specimenAmount);
        spa.setEggAmount(eggAmount);
        return spa;
    }

    private HarvestPermitSpeciesAmount(final HarvestPermit harvestPermit,
                                       final GameSpecies gameSpecies,
                                       final RestrictionType restrictionType,
                                       final Float restrictionAmount,
                                       final LocalDate beginDate,
                                       final LocalDate endDate) {
        this.harvestPermit = harvestPermit;
        this.gameSpecies = gameSpecies;
        this.restrictionType = restrictionType;
        this.restrictionAmount = restrictionAmount;
        this.beginDate = beginDate;
        this.endDate = endDate;
    }

    @AssertTrue
    public boolean isValidAmount() {
        final List<Integer> nestRemovalAmounts = Stream.of(nestAmount, eggAmount, constructionAmount)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return specimenAmount != null || !nestRemovalAmounts.isEmpty();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = ID_COLUMN_NAME, nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public HarvestPermit getHarvestPermit() {
        return harvestPermit;
    }

    public void setHarvestPermit(HarvestPermit harvestPermit) {
        this.harvestPermit = harvestPermit;
    }

    public GameSpecies getGameSpecies() {
        return gameSpecies;
    }

    public void setGameSpecies(GameSpecies gameSpecies) {
        this.gameSpecies = gameSpecies;
    }

    public Float getSpecimenAmount() {
        return specimenAmount;
    }

    public void setSpecimenAmount(Float harvestAmount) {
        this.specimenAmount = harvestAmount;
    }

    public Integer getNestAmount() {
        return nestAmount;
    }

    public void setNestAmount(final Integer nestAmount) {
        this.nestAmount = nestAmount;
    }

    public Integer getEggAmount() {
        return eggAmount;
    }

    public void setEggAmount(final Integer eggAmount) {
        this.eggAmount = eggAmount;
    }

    public Integer getConstructionAmount() {
        return constructionAmount;
    }

    public void setConstructionAmount(final Integer constructionAmount) {
        this.constructionAmount = constructionAmount;
    }

    public RestrictionType getRestrictionType() {
        return restrictionType;
    }

    public void setRestrictionType(RestrictionType restrictionType) {
        this.restrictionType = restrictionType;
    }

    public Float getRestrictionAmount() {
        return restrictionAmount;
    }

    public void setRestrictionAmount(Float restrictionAmount) {
        this.restrictionAmount = restrictionAmount;
    }

    @Override
    public LocalDate getBeginDate() {
        return beginDate;
    }

    @Override
    public void setBeginDate(final LocalDate beginDate) {
        this.beginDate = beginDate;
    }

    @Override
    public LocalDate getEndDate() {
        return endDate;
    }

    @Override
    public void setEndDate(final LocalDate endDate) {
        this.endDate = endDate;
    }

    @Override
    public LocalDate getBeginDate2() {
        return beginDate2;
    }

    @Override
    public void setBeginDate2(final LocalDate beginDate2) {
        this.beginDate2 = beginDate2;
    }

    @Override
    public LocalDate getEndDate2() {
        return endDate2;
    }

    @Override
    public void setEndDate2(final LocalDate endDate2) {
        this.endDate2 = endDate2;
    }

    public boolean isMooselikeHuntingFinished() {
        return mooselikeHuntingFinished;
    }

    public void setMooselikeHuntingFinished(final boolean mooselikeHuntingFinished) {
        this.mooselikeHuntingFinished = mooselikeHuntingFinished;
    }

    public boolean isHuntingFinishedByModerator() {
        return huntingFinishedByModerator;
    }

    public void setHuntingFinishedByModerator(final boolean huntingFinishedByModerator) {
        this.huntingFinishedByModerator = huntingFinishedByModerator;
    }

    public boolean matches(final int gameSpeciesCode, final LocalDate date) {
        return this.gameSpecies != null &&
                this.gameSpecies.getOfficialCode() == gameSpeciesCode &&
                containsDate(date);
    }

    public LocalDate getDueDate() {
        return Optional.ofNullable(F.firstNonNull(getEndDate2(), getEndDate()))
                .map(end -> end.plusDays(7))
                .orElse(null);
    }

    public void assertMooselikeHuntingNotFinished() {
        if (isMooselikeHuntingFinished()) {
            throw new MooselikeHuntingFinishedException();
        }
    }

    // Querydsl delegates -->

    @QueryDelegate(HarvestPermitSpeciesAmount.class)
    public static BooleanExpression matchesSpecies(QHarvestPermitSpeciesAmount spa,
                                                   Path<HarvestPermit> permit,
                                                   int gameSpeciesCode) {
        final QGameSpecies gameSpecies = new QGameSpecies("gameSpecies_matchesSpeciesAndHuntingYear");
        return JPAExpressions.selectFrom(spa)
                .join(spa.gameSpecies, gameSpecies)
                .where(spa.harvestPermit.eq(permit)
                        .and(gameSpecies.officialCode.eq(gameSpeciesCode))
                ).exists();
    }

    @QueryDelegate(HarvestPermitSpeciesAmount.class)
    public static BooleanExpression validOnHuntingYear(QHarvestPermitSpeciesAmount spa, int year) {
        final LocalDate begin = DateUtil.huntingYearBeginDate(year);
        final LocalDate end = DateUtil.huntingYearEndDate(year);
        return validBetween(spa.beginDate, spa.endDate, begin, end)
                .or(validBetween(spa.beginDate2, spa.endDate2, begin, end));
    }

    private static BooleanExpression validBetween(DatePath<LocalDate> beginDate, DatePath<LocalDate> endDate, LocalDate begin, LocalDate end) {
        return beginDate.goe(begin).and(endDate.loe(end));
    }
}
