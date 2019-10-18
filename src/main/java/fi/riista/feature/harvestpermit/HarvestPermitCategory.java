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
    LARGE_CARNIVORE_WOLF(HarvestPermitApplicationName.WOLF, PermitDecisionName.LARGE_CARNIVORE);

    public static final String LARGE_CARNIVORE_CATEGORIES = "LARGE_CARNIVORE";

    public static List<HarvestPermitCategory> getLargeCarnivoreCategories() {
        return ImmutableList.of(
                LARGE_CARNIVORE_BEAR,
                LARGE_CARNIVORE_LYNX,
                LARGE_CARNIVORE_LYNX_PORONHOITO,
                LARGE_CARNIVORE_WOLF);
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
                return true;
            default:
                return false;
        }
    }

}
