package fi.riista.feature.permit.application.carnivore.species;

import com.google.common.collect.Range;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

import static java.util.Objects.requireNonNull;

public class CarnivorePermitApplicationSpeciesAmountDTO {

    public static CarnivorePermitApplicationSpeciesAmountDTO create(final @Nonnull HarvestPermitApplication application) {
        final CarnivorePermitApplicationSpeciesAmountDTO dto =
                new CarnivorePermitApplicationSpeciesAmountDTO(application);
        final Range<LocalDate> range = CarnivorePermitSpecies.getPeriod(application);
        dto.setBegin(range.lowerEndpoint());
        dto.setEnd(range.upperEndpoint());
        return dto;
    }

    public static CarnivorePermitApplicationSpeciesAmountDTO create(final @Nonnull HarvestPermitApplication application,
                                                                    final @Nonnull HarvestPermitApplicationSpeciesAmount spa) {

        final CarnivorePermitApplicationSpeciesAmountDTO dto =
                new CarnivorePermitApplicationSpeciesAmountDTO(application);

        requireNonNull(spa);
        dto.setAmount(spa.getAmount());
        dto.setBegin(spa.getBeginDate());
        dto.setEnd(spa.getEndDate());
        return dto;
    }

    private int gameSpeciesCode;

    private float amount;

    @Valid
    @NotNull
    private LocalDate begin;

    @Valid
    @NotNull
    private LocalDate end;

    private LocalDate legalMinDate;

    private LocalDate legalMaxDate;

    public CarnivorePermitApplicationSpeciesAmountDTO() {
    }

    private CarnivorePermitApplicationSpeciesAmountDTO(final @Nonnull HarvestPermitApplication application) {
        final Range<LocalDate> period = CarnivorePermitSpecies.getPeriod(application);

        this.gameSpeciesCode = CarnivorePermitSpecies.getSpecies(application.getHarvestPermitCategory());
        this.legalMinDate = period.lowerEndpoint();
        this.legalMaxDate = period.upperEndpoint();
    }

    @AssertTrue
    public boolean isValidAmount() {
        return CarnivorePermitSpecies.isValidPermitAmount(amount);
    }

    @AssertTrue
    public boolean isValidSpecies() {
        return CarnivorePermitSpecies.isCarnivoreSpecies(gameSpeciesCode);
    }

    // Accessors -->

    public int getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public void setGameSpeciesCode(final int gameSpeciesCode) {
        this.gameSpeciesCode = gameSpeciesCode;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(final float amount) {
        this.amount = amount;
    }

    public LocalDate getBegin() {
        return begin;
    }

    public void setBegin(final LocalDate begin) {
        this.begin = begin;
    }

    public LocalDate getEnd() {
        return end;
    }

    public void setEnd(final LocalDate end) {
        this.end = end;
    }

    public LocalDate getLegalMinDate() {
        return legalMinDate;
    }

    public void setLegalMinDate(final LocalDate legalMinDate) {
        this.legalMinDate = legalMinDate;
    }

    public LocalDate getLegalMaxDate() {
        return legalMaxDate;
    }

    public void setLegalMaxDate(final LocalDate legalMaxDate) {
        this.legalMaxDate = legalMaxDate;
    }
}
