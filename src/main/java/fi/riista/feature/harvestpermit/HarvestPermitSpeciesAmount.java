package fi.riista.feature.harvestpermit;

import com.querydsl.core.annotations.QueryDelegate;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.jpa.JPAExpressions;
import fi.riista.feature.common.entity.CreditorReference;
import fi.riista.feature.common.entity.Has2BeginEndDates;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.harvestpermit.report.HarvestReport;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.joda.time.LocalDate;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.summingInt;

@Entity
@Access(AccessType.FIELD)
public class HarvestPermitSpeciesAmount extends LifecycleEntity<Long> implements Has2BeginEndDates {

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

    @Column(nullable = false)
    private float amount;

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

    @Embedded
    private CreditorReference creditorReference;

    public HarvestPermitSpeciesAmount() {
    }

    public HarvestPermitSpeciesAmount(final HarvestPermit harvestPermit,
                                      final GameSpecies gameSpecies,
                                      final float amount,
                                      final RestrictionType restrictionType,
                                      final Float restrictionAmount,
                                      final LocalDate beginDate,
                                      final LocalDate endDate,
                                      final CreditorReference creditorReference) {
        this.harvestPermit = harvestPermit;
        this.gameSpecies = gameSpecies;
        this.amount = amount;
        this.restrictionType = restrictionType;
        this.restrictionAmount = restrictionAmount;
        this.beginDate = beginDate;
        this.endDate = endDate;
        this.creditorReference = creditorReference;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "harvest_permit_species_amount_id", nullable = false)
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

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
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

    public CreditorReference getCreditorReference() {
        return creditorReference;
    }

    public void setCreditorReference(CreditorReference creditorReference) {
        this.creditorReference = creditorReference;
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

    public boolean matches(final int gameSpeciesCode, final LocalDate date) {
        return this.gameSpecies != null &&
                this.gameSpecies.getOfficialCode() == gameSpeciesCode &&
                containsDate(date);
    }

    public boolean isEndOfHuntingReportRequired(final Set<HarvestReport> harvestReports) {
        final EnumSet<HarvestReport.State> states =
                EnumSet.complementOf(EnumSet.of(HarvestReport.State.DELETED, HarvestReport.State.REJECTED));

        final int cumulatedHarvestAmount = harvestReports.stream()
                .filter(report -> states.contains(report.getState()))
                .flatMap(report -> report.getHarvests().stream())
                .filter(harvest -> harvest.getSpecies().equals(this.gameSpecies))
                .collect(summingInt(Harvest::getAmount));

        return cumulatedHarvestAmount < amount;
    }


    public LocalDate getDueDate() {
        return Optional.ofNullable(F.firstNonNull(getEndDate2(), getEndDate()))
                .map(end -> end.plusDays(7))
                .orElse(null);
    }

    // Querydsl delegates -->

    @QueryDelegate(HarvestPermitSpeciesAmount.class)
    public static BooleanExpression matchesSpeciesAndHuntingYear(QHarvestPermitSpeciesAmount spa,
                                                                 Path<HarvestPermit> permit,
                                                                 int gameSpeciesCode,
                                                                 int year) {
        final QGameSpecies gameSpecies = new QGameSpecies("gameSpecies_matchesSpeciesAndHuntingYear");
        return JPAExpressions.selectFrom(spa)
                .join(spa.gameSpecies, gameSpecies)
                .where(spa.harvestPermit.eq(permit)
                        .and(gameSpecies.officialCode.eq(gameSpeciesCode))
                        .and(validOnHuntingYear(spa, year))
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
