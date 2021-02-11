package fi.riista.api.pub;

import org.joda.time.LocalDate;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

public class PublicCarnivorePermitDTO {

    private final String permitNumber;
    private final int speciesCode;
    private final LocalDate decisionDate;
    private final String rkaCode;

    private PublicCarnivorePermitDTO(final @Nonnull String permitNumber,
                                     final int speciesCode,
                                     final @Nonnull LocalDate decisionDate,
                                     final @Nonnull String rkaCode) {
        this.permitNumber = requireNonNull(permitNumber);
        this.speciesCode = speciesCode;
        this.decisionDate = requireNonNull(decisionDate);
        this.rkaCode = requireNonNull(rkaCode);
    }

    public String getPermitNumber() {
        return permitNumber;
    }

    public int getSpeciesCode() {
        return speciesCode;
    }

    public LocalDate getDecisionDate() {
        return decisionDate;
    }

    public String getRkaCode() {
        return rkaCode;
    }


    public static final class Builder {
        private String permitNumber;
        private int speciesCode;
        private LocalDate decisionDate;
        private String rkaCode;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder withPermitNumber(String permitNumber) {
            this.permitNumber = permitNumber;
            return this;
        }

        public Builder withSpeciesCode(int speciesCode) {
            this.speciesCode = speciesCode;
            return this;
        }

        public Builder withDecisionDate(LocalDate decisionDate) {
            this.decisionDate = decisionDate;
            return this;
        }

        public Builder withRkaCode(String rkaCode) {
            this.rkaCode = rkaCode;
            return this;
        }

        public PublicCarnivorePermitDTO build() {
            return new PublicCarnivorePermitDTO(permitNumber, speciesCode, decisionDate, rkaCode);
        }
    }
}
