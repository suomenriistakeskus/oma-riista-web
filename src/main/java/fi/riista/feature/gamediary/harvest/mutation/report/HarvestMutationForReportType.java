package fi.riista.feature.gamediary.harvest.mutation.report;

import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestReportingType;
import fi.riista.feature.gamediary.harvest.mutation.HarvestMutation;

public interface HarvestMutationForReportType extends HarvestMutation {
    HarvestReportingType getReportingType();

    default boolean isHarvestReportRequired() {
        return getReportingType() == HarvestReportingType.PERMIT || getReportingType() == HarvestReportingType.SEASON;
    }

    default void clearSeasonFields(final Harvest harvest) {
        harvest.setHarvestQuota(null);
        harvest.setHarvestSeason(null);
        harvest.setFeedingPlace(null);
        harvest.setTaigaBeanGoose(null);
        harvest.setHuntingMethod(null);
        harvest.setHuntingAreaSize(null);
        harvest.setHuntingParty(null);
        harvest.setReportedWithPhoneCall(null);
        harvest.setHuntingAreaType(null);
    }

    default void clearPermitFields(final Harvest harvest) {
        harvest.setHarvestPermit(null);
        harvest.setStateAcceptedToHarvestPermit(null);
        harvest.setPermittedMethod(null);
    }
}
