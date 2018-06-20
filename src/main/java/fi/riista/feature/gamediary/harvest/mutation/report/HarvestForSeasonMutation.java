package fi.riista.feature.gamediary.harvest.mutation.report;

import com.google.common.base.Preconditions;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestDTOBase;
import fi.riista.feature.gamediary.harvest.HarvestReportingType;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.HuntingAreaType;
import fi.riista.feature.gamediary.harvest.HuntingMethod;
import fi.riista.feature.gamediary.harvest.mutation.HarvestMutationRole;
import fi.riista.feature.gamediary.harvest.mutation.exception.HarvestSeasonChangeForbiddenException;
import fi.riista.feature.harvestpermit.season.HarvestQuota;
import fi.riista.feature.harvestpermit.season.HarvestSeason;

import javax.annotation.Nonnull;
import java.util.Objects;

public class HarvestForSeasonMutation implements HarvestMutationForReportType {
    private final HarvestMutationRole mutationRole;
    private final HarvestSeason harvestSeason;
    private final HarvestQuota harvestQuota;
    private final Boolean feedingPlace;
    private final Boolean taigaBeanGoose;
    private final HuntingMethod huntingMethod;
    private final Double huntingAreaSize;
    private final String huntingParty;
    private final Boolean reportedWithPhoneCall;
    private final HuntingAreaType huntingAreaType;

    public HarvestForSeasonMutation(@Nonnull final HarvestDTOBase dto,
                                    @Nonnull final HarvestMutationRole mutationRole,
                                    @Nonnull final HarvestSeason harvestSeason,
                                    final HarvestQuota harvestQuota) {
        Objects.requireNonNull(dto);
        Preconditions.checkArgument(dto.getHarvestSpecVersion().supportsHarvestReport());
        this.mutationRole = Objects.requireNonNull(mutationRole);

        // Fields calculated based on location and harvest date
        this.harvestSeason = Objects.requireNonNull(harvestSeason);
        this.harvestQuota = harvestQuota;

        // Fields provided by the user
        this.feedingPlace = dto.getFeedingPlace();
        this.taigaBeanGoose = dto.getTaigaBeanGoose();
        this.huntingMethod = dto.getHuntingMethod();
        this.huntingAreaSize = dto.getHuntingAreaSize();
        this.huntingParty = dto.getHuntingParty();
        this.reportedWithPhoneCall = dto.getReportedWithPhoneCall();
        this.huntingAreaType = dto.getHuntingAreaType();
    }

    @Override
    public void accept(final Harvest harvest) {
        final boolean roleCanChangeSeason = mutationRole == HarvestMutationRole.AUTHOR_OR_ACTOR;
        final boolean seasonChanged = harvest.getHarvestSeason() != null
                && !harvest.getHarvestSeason().equals(harvestSeason);

        if (seasonChanged && !roleCanChangeSeason) {
            throw new HarvestSeasonChangeForbiddenException(mutationRole);
        }

        clearPermitFields(harvest);

        harvest.setHarvestReportRequired(true);
        harvest.setHarvestSeason(harvestSeason);
        harvest.setHarvestQuota(harvestQuota);
        harvest.setFeedingPlace(feedingPlace);
        harvest.setTaigaBeanGoose(taigaBeanGoose);
        harvest.setHuntingMethod(huntingMethod);
        harvest.setHuntingAreaSize(huntingAreaSize);
        harvest.setHuntingParty(huntingParty);
        harvest.setReportedWithPhoneCall(reportedWithPhoneCall);
        harvest.setHuntingAreaType(huntingAreaType);
    }

    @Override
    public HarvestReportingType getReportingType() {
        return HarvestReportingType.SEASON;
    }
}
