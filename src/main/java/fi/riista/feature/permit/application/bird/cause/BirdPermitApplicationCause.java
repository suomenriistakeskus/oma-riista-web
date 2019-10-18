package fi.riista.feature.permit.application.bird.cause;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

@Embeddable
@Access(AccessType.FIELD)
public class BirdPermitApplicationCause {

    @Column(nullable = false)
    private boolean causePublicHealth = false;

    @Column(nullable = false)
    private boolean causePublicSafety = false;

    @Column(nullable = false)
    private boolean causeAviationSafety = false;

    @Column(nullable = false)
    private boolean causeCropsDamage = false;

    @Column(nullable = false)
    private boolean causeDomesticPets = false;

    @Column(nullable = false)
    private boolean causeForestDamage = false;

    @Column(nullable = false)
    private boolean causeFishing = false;

    @Column(nullable = false)
    private boolean causeWaterSystem = false;

    @Column(nullable = false)
    private boolean causeFlora = false;

    @Column(nullable = false)
    private boolean causeFauna = false;

    @Column(nullable = false)
    private boolean causeResearch = false;

    public boolean isCausePublicHealth() {
        return causePublicHealth;
    }

    public void setCausePublicHealth(boolean causePublicHealth) {
        this.causePublicHealth = causePublicHealth;
    }

    public boolean isCausePublicSafety() {
        return causePublicSafety;
    }

    public void setCausePublicSafety(boolean causePublicSafety) {
        this.causePublicSafety = causePublicSafety;
    }

    public boolean isCauseAviationSafety() {
        return causeAviationSafety;
    }

    public void setCauseAviationSafety(boolean causeAviationSafety) {
        this.causeAviationSafety = causeAviationSafety;
    }

    public boolean isCauseCropsDamage() {
        return causeCropsDamage;
    }

    public void setCauseCropsDamage(boolean causeCropsDamage) {
        this.causeCropsDamage = causeCropsDamage;
    }

    public boolean isCauseDomesticPets() {
        return causeDomesticPets;
    }

    public void setCauseDomesticPets(boolean causeDomesticPets) {
        this.causeDomesticPets = causeDomesticPets;
    }

    public boolean isCauseForestDamage() {
        return causeForestDamage;
    }

    public void setCauseForestDamage(boolean causeForestDamage) {
        this.causeForestDamage = causeForestDamage;
    }

    public boolean isCauseFishing() {
        return causeFishing;
    }

    public void setCauseFishing(boolean causeFishing) {
        this.causeFishing = causeFishing;
    }

    public boolean isCauseWaterSystem() {
        return causeWaterSystem;
    }

    public void setCauseWaterSystem(boolean causeWaterSystem) {
        this.causeWaterSystem = causeWaterSystem;
    }

    public boolean isCauseFlora() {
        return causeFlora;
    }

    public void setCauseFlora(boolean causeFlora) {
        this.causeFlora = causeFlora;
    }

    public boolean isCauseFauna() {
        return causeFauna;
    }

    public void setCauseFauna(boolean causeFauna) {
        this.causeFauna = causeFauna;
    }

    public boolean isCauseResearch() {
        return causeResearch;
    }

    public void setCauseResearch(boolean causeResearch) {
        this.causeResearch = causeResearch;
    }

    @Transient
    public boolean hasOnlyCausesAllowingLimitlessPermit() {
        return causeResearch == false &&
                causeFauna == false &&
                causeFlora == false;
    }
}
