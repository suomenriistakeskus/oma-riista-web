package fi.riista.feature.harvestpermit;

import com.google.common.collect.ImmutableList;
import fi.riista.feature.permit.application.HarvestPermitApplicationName;
import fi.riista.feature.permit.decision.PermitDecisionName;
import fi.riista.util.LocalisedString;

import java.util.List;

public enum HarvestPermitCategory {

    MOOSELIKE(PermitDecisionName.MOOSELIKE),
    MOOSELIKE_NEW(PermitDecisionName.MOOSELIKE_AMENDMENT),
    BIRD(HarvestPermitApplicationName.BIRD, PermitDecisionName.BIRD),
    LARGE_CARNIVORE_BEAR(HarvestPermitApplicationName.BEAR, PermitDecisionName.LARGE_CARNIVORE),
    LARGE_CARNIVORE_LYNX(HarvestPermitApplicationName.LYNX, PermitDecisionName.LARGE_CARNIVORE),
    LARGE_CARNIVORE_LYNX_PORONHOITO(HarvestPermitApplicationName.LYNX_PORONHOITO, PermitDecisionName.LARGE_CARNIVORE),
    LARGE_CARNIVORE_WOLF(HarvestPermitApplicationName.WOLF, PermitDecisionName.LARGE_CARNIVORE),
    LARGE_CARNIVORE_WOLF_PORONHOITO(HarvestPermitApplicationName.WOLF, PermitDecisionName.LARGE_CARNIVORE),
    MAMMAL(HarvestPermitApplicationName.MAMMAL, PermitDecisionName.MAMMAL),
    NEST_REMOVAL(HarvestPermitApplicationName.NEST_REMOVAL, PermitDecisionName.NEST_REMOVAL),

    // New applications not created, split into EUROPEAN_BEAVER and PARTRIDGE
    LAW_SECTION_TEN(HarvestPermitApplicationName.LAW_SECTION_TEN, PermitDecisionName.LAW_SECTION_TEN),
    WEAPON_TRANSPORTATION(HarvestPermitApplicationName.WEAPON_TRANSPORTATION, PermitDecisionName.WEAPON_TRANSPORTATION),
    DISABILITY(HarvestPermitApplicationName.DISABILITY, PermitDecisionName.DISABILITY),
    DOG_UNLEASH(HarvestPermitApplicationName.DOG_UNLEASH, PermitDecisionName.DOG_UNLEASH),
    DOG_DISTURBANCE(HarvestPermitApplicationName.DOG_DISTURBANCE, PermitDecisionName.DOG_DISTURBANCE),
    DEPORTATION(HarvestPermitApplicationName.DEPORTATION, PermitDecisionName.DEPORTATION),
    RESEARCH(HarvestPermitApplicationName.RESEARCH, PermitDecisionName.RESEARCH),
    IMPORTING(HarvestPermitApplicationName.IMPORTING, PermitDecisionName.IMPORTING),
    GAME_MANAGEMENT(HarvestPermitApplicationName.GAME_MANAGEMENT, PermitDecisionName.GAME_MANAGEMENT),
    EUROPEAN_BEAVER(HarvestPermitApplicationName.LAW_SECTION_TEN, PermitDecisionName.LAW_SECTION_TEN),
    PARTRIDGE(HarvestPermitApplicationName.LAW_SECTION_TEN, PermitDecisionName.LAW_SECTION_TEN);

    public static final String LARGE_CARNIVORE_CATEGORIES = "LARGE_CARNIVORE";

    public static List<HarvestPermitCategory> getLargeCarnivoreCategories() {
        return ImmutableList.of(
                LARGE_CARNIVORE_BEAR,
                LARGE_CARNIVORE_LYNX,
                LARGE_CARNIVORE_LYNX_PORONHOITO,
                LARGE_CARNIVORE_WOLF,
                LARGE_CARNIVORE_WOLF_PORONHOITO);
    }

    private final LocalisedString applicationName;
    private final LocalisedString decisionName;

    HarvestPermitCategory(final LocalisedString decisionName) {
        this(decisionName, decisionName);
    }

    HarvestPermitCategory(final LocalisedString applicationName,
                          final LocalisedString decisionName) {
        this.applicationName = applicationName;
        this.decisionName = decisionName;
    }

    public LocalisedString getApplicationName() {
        return applicationName;
    }

    public LocalisedString getDecisionName() {
        return decisionName;
    }

    public boolean isMooselike() {
        return this == MOOSELIKE;
    }

    public boolean isBird() {
        return this == BIRD;
    }

    public boolean isAmendment() {
        return this == MOOSELIKE_NEW;
    }

    public boolean isLargeCarnivore() {
        switch (this) {
            case LARGE_CARNIVORE_BEAR:
            case LARGE_CARNIVORE_LYNX:
            case LARGE_CARNIVORE_LYNX_PORONHOITO:
            case LARGE_CARNIVORE_WOLF:
            case LARGE_CARNIVORE_WOLF_PORONHOITO:
                return true;
            default:
                return false;
        }
    }

    public boolean isDerogation() {
        switch (this) {
            case BIRD:
            case LARGE_CARNIVORE_BEAR:
            case LARGE_CARNIVORE_LYNX:
            case LARGE_CARNIVORE_LYNX_PORONHOITO:
            case LARGE_CARNIVORE_WOLF:
            case LARGE_CARNIVORE_WOLF_PORONHOITO:
            case MAMMAL:
            case NEST_REMOVAL:
                return true;
            default:
                return false;
        }
    }

    public boolean hasSpeciesAmount() {
        switch (this) {
            case DISABILITY:
            case DOG_DISTURBANCE:
            case DOG_UNLEASH:
            case WEAPON_TRANSPORTATION:
                return false;
            default:
                return true;
        }
    }

    public boolean hasNatura() {
        switch (this) {
            case DOG_DISTURBANCE:
            case DOG_UNLEASH:
            case BIRD:
                return true;
            default:
                return false;
        }
    }

    public boolean isAreaSizeRequired() {
        switch (this){
            case BIRD:
            case LARGE_CARNIVORE_BEAR:
            case LARGE_CARNIVORE_LYNX:
            case LARGE_CARNIVORE_LYNX_PORONHOITO:
            case LARGE_CARNIVORE_WOLF:
            case LARGE_CARNIVORE_WOLF_PORONHOITO:
            case MAMMAL:
            case NEST_REMOVAL:
            case LAW_SECTION_TEN:
            case DOG_DISTURBANCE:
            case DOG_UNLEASH:
                return true;
            default:
                return false;
        }
    }
}
