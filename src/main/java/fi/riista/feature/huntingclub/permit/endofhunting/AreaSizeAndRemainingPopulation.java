package fi.riista.feature.huntingclub.permit.endofhunting;

import fi.riista.util.F;
import io.vavr.Tuple;
import io.vavr.Tuple4;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.Min;
import java.io.Serializable;

@Embeddable
@Access(AccessType.FIELD)
public class AreaSizeAndRemainingPopulation implements Serializable {

    // In full hectares
    @Min(0)
    @Column
    private Integer totalHuntingArea;

    // In full hectares
    @Min(0)
    @Column
    private Integer effectiveHuntingArea;

    @Min(0)
    @Column
    private Integer remainingPopulationInTotalArea;

    @Min(0)
    @Column
    private Integer remainingPopulationInEffectiveArea;

    public AreaSizeAndRemainingPopulation() {
    }

    public AreaSizeAndRemainingPopulation(final AreaSizeAndRemainingPopulation a) {
        this.totalHuntingArea = a.getTotalHuntingArea();
        this.effectiveHuntingArea = a.getEffectiveHuntingArea();
        this.remainingPopulationInTotalArea = a.getRemainingPopulationInTotalArea();
        this.remainingPopulationInEffectiveArea = a.getRemainingPopulationInEffectiveArea();
    }

    public boolean isHuntingAreaAndRemainingPopulationPresent() {
        return totalHuntingArea != null && remainingPopulationInTotalArea != null ||
                effectiveHuntingArea != null && remainingPopulationInEffectiveArea != null;
    }

    public boolean isEmpty() {
        return F.allNull(
                totalHuntingArea,
                effectiveHuntingArea,
                remainingPopulationInTotalArea,
                remainingPopulationInEffectiveArea);
    }

    // Builders -->

    public AreaSizeAndRemainingPopulation withTotalHuntingArea(final Integer totalHuntingArea) {
        setTotalHuntingArea(totalHuntingArea);
        return this;
    }

    public AreaSizeAndRemainingPopulation withEffectiveHuntingArea(final Integer effectiveHuntingArea) {
        setEffectiveHuntingArea(effectiveHuntingArea);
        return this;
    }

    public AreaSizeAndRemainingPopulation withRemainingPopulationInTotalArea(final Integer remainingPopulation) {
        setRemainingPopulationInTotalArea(remainingPopulation);
        return this;
    }

    public AreaSizeAndRemainingPopulation withRemainingPopulationInEffectiveArea(final Integer remainingPopulation) {
        setRemainingPopulationInEffectiveArea(remainingPopulation);
        return this;
    }

    public Tuple4<Integer, Integer, Integer, Integer> asTuple() {
        return Tuple.of(totalHuntingArea, effectiveHuntingArea, remainingPopulationInTotalArea,
                remainingPopulationInEffectiveArea);
    }

    // Accessors -->

    public Integer getTotalHuntingArea() {
        return totalHuntingArea;
    }

    public void setTotalHuntingArea(final Integer totalHuntingArea) {
        this.totalHuntingArea = totalHuntingArea;
    }

    public Integer getEffectiveHuntingArea() {
        return effectiveHuntingArea;
    }

    public void setEffectiveHuntingArea(final Integer effectiveHuntingArea) {
        this.effectiveHuntingArea = effectiveHuntingArea;
    }

    public Integer getRemainingPopulationInTotalArea() {
        return remainingPopulationInTotalArea;
    }

    public void setRemainingPopulationInTotalArea(final Integer remainingPopulationInTotalArea) {
        this.remainingPopulationInTotalArea = remainingPopulationInTotalArea;
    }

    public Integer getRemainingPopulationInEffectiveArea() {
        return remainingPopulationInEffectiveArea;
    }

    public void setRemainingPopulationInEffectiveArea(final Integer remainingPopulationInEffectiveArea) {
        this.remainingPopulationInEffectiveArea = remainingPopulationInEffectiveArea;
    }
}
