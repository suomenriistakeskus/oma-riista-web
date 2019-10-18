package fi.riista.feature.permit.application.bird.cause;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

public class BirdPermitApplicationCauseDTO {
    private boolean causePublicHealth;
    private boolean causePublicSafety;
    private boolean causeAviationSafety;
    private boolean causeCropsDamage;
    private boolean causeDomesticPets;
    private boolean causeForestDamage;
    private boolean causeFishing;
    private boolean causeWaterSystem;
    private boolean causeFlora;
    private boolean causeFauna;
    private boolean causeResearch;

    public BirdPermitApplicationCauseDTO() {
    }

    public BirdPermitApplicationCauseDTO(final Builder builder) {
        this.causePublicHealth = builder.causePublicHealth;
        this.causePublicSafety = builder.causePublicSafety;
        this.causeAviationSafety = builder.causeAviationSafety;
        this.causeCropsDamage = builder.causeCropsDamage;
        this.causeDomesticPets = builder.causeDomesticPets;
        this.causeForestDamage = builder.causeForestDamage;
        this.causeFishing = builder.causeFishing;
        this.causeWaterSystem = builder.causeWaterSystem;
        this.causeFlora = builder.causeFlora;
        this.causeFauna = builder.causeFauna;
        this.causeResearch = builder.causeResearch;
    }

    public static BirdPermitApplicationCauseDTO createFrom(final @Nonnull BirdPermitApplicationCause info) {
        requireNonNull(info);

        return new Builder()
                .withCausePublicHealth(info.isCausePublicHealth())
                .withCausePublicSafety(info.isCausePublicSafety())
                .withCauseAviationSafety(info.isCauseAviationSafety())
                .withCauseCropsDamage(info.isCauseCropsDamage())
                .withCauseDomesticPets(info.isCauseDomesticPets())
                .withCauseForestDamage(info.isCauseForestDamage())
                .withCauseFishing(info.isCauseFishing())
                .withCauseWaterSystem(info.isCauseWaterSystem())
                .withCauseFlora(info.isCauseFlora())
                .withCauseFauna(info.isCauseFauna())
                .withCauseResearch(info.isCauseResearch())
                .build();
    }

    public BirdPermitApplicationCause toEntity() {
        final BirdPermitApplicationCause info = new BirdPermitApplicationCause();
        info.setCausePublicHealth(causePublicHealth);
        info.setCausePublicSafety(causePublicSafety);
        info.setCauseAviationSafety(causeAviationSafety);
        info.setCauseCropsDamage(causeCropsDamage);
        info.setCauseDomesticPets(causeDomesticPets);
        info.setCauseForestDamage(causeForestDamage);
        info.setCauseFishing(causeFishing);
        info.setCauseWaterSystem(causeWaterSystem);
        info.setCauseFlora(causeFlora);
        info.setCauseFauna(causeFauna);
        info.setCauseResearch(causeResearch);

        return info;
    }

    public boolean isCausePublicHealth() {
        return causePublicHealth;
    }

    public boolean isCausePublicSafety() {
        return causePublicSafety;
    }

    public boolean isCauseAviationSafety() {
        return causeAviationSafety;
    }

    public boolean isCauseCropsDamage() {
        return causeCropsDamage;
    }

    public boolean isCauseDomesticPets() {
        return causeDomesticPets;
    }

    public boolean isCauseForestDamage() {
        return causeForestDamage;
    }

    public boolean isCauseFishing() {
        return causeFishing;
    }

    public boolean isCauseWaterSystem() {
        return causeWaterSystem;
    }

    public boolean isCauseFlora() {
        return causeFlora;
    }

    public boolean isCauseFauna() {
        return causeFauna;
    }

    public boolean isCauseResearch() {
        return causeResearch;
    }

    public static class Builder {
        private boolean causePublicHealth;
        private boolean causePublicSafety;
        private boolean causeAviationSafety;
        private boolean causeCropsDamage;
        private boolean causeDomesticPets;
        private boolean causeForestDamage;
        private boolean causeFishing;
        private boolean causeWaterSystem;
        private boolean causeFlora;
        private boolean causeFauna;
        private boolean causeResearch;

        public Builder withCausePublicHealth(boolean causePublicHealth) {
            this.causePublicHealth = causePublicHealth;
            return this;
        }

        public Builder withCausePublicSafety(boolean causePublicSafety) {
            this.causePublicSafety = causePublicSafety;
            return this;
        }

        public Builder withCauseAviationSafety(boolean causeAviationSafety) {
            this.causeAviationSafety = causeAviationSafety;
            return this;
        }

        public Builder withCauseCropsDamage(boolean causeCropsDamage) {
            this.causeCropsDamage = causeCropsDamage;
            return this;
        }

        public Builder withCauseDomesticPets(boolean causeDomesticPets) {
            this.causeDomesticPets = causeDomesticPets;
            return this;
        }

        public Builder withCauseForestDamage(boolean causeForestDamage) {
            this.causeForestDamage = causeForestDamage;
            return this;
        }

        public Builder withCauseFishing(boolean causeFishing) {
            this.causeFishing = causeFishing;
            return this;
        }

        public Builder withCauseWaterSystem(boolean causeWaterSystem) {
            this.causeWaterSystem = causeWaterSystem;
            return this;
        }

        public Builder withCauseFlora(boolean causeFlora) {
            this.causeFlora = causeFlora;
            return this;
        }

        public Builder withCauseFauna(boolean causeFauna) {
            this.causeFauna = causeFauna;
            return this;
        }

        public Builder withCauseResearch(boolean causeResearch) {
            this.causeResearch = causeResearch;
            return this;
        }

        public BirdPermitApplicationCauseDTO build() {
            return new BirdPermitApplicationCauseDTO(this);
        }
    }
}
