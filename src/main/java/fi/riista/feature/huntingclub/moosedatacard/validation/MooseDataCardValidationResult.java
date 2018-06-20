package fi.riista.feature.huntingclub.moosedatacard.validation;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCard;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

public class MooseDataCardValidationResult {

    public final MooseDataCard mooseDataCard;

    public final String clubCode;

    public final GeoLocation clubCoordinates;

    public final long harvestPermitSpeciesAmountId;

    public final int huntingYear;

    public final long contactPersonId;

    public final DateTime timestamp;

    public final List<String> messages;

    public MooseDataCardValidationResult(@Nonnull final MooseDataCard mooseDataCard,
                                         @Nonnull final String clubCode,
                                         @Nonnull final GeoLocation clubCoordinates,
                                         @Nonnull final HarvestPermitSpeciesAmount speciesAmount,
                                         final int huntingYear,
                                         final long contactPersonId,
                                         @Nonnull final DateTime timestamp,
                                         @Nonnull final List<String> messages) {

        this.mooseDataCard = Objects.requireNonNull(mooseDataCard, "mooseDataCard is null");

        this.clubCode = Objects.requireNonNull(clubCode, "clubCode is null");
        this.clubCoordinates = Objects.requireNonNull(clubCoordinates, "clubCoordinates is null");

        Objects.requireNonNull(speciesAmount, "speciesAmount is null");
        this.harvestPermitSpeciesAmountId = speciesAmount.getId();

        this.huntingYear = huntingYear;
        this.contactPersonId = contactPersonId;

        this.timestamp = Objects.requireNonNull(timestamp, "timestamp is null");
        this.messages = Objects.requireNonNull(messages, "messages is null");
    }
}
