package fi.riista.feature.gamediary.observation.metadata;

import com.google.common.base.Preconditions;
import fi.riista.feature.common.entity.Required;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.observation.metadata.ObservationBaseFields;
import fi.riista.feature.gamediary.observation.metadata.ObservationContextSensitiveFields;
import fi.riista.feature.gamediary.observation.ObservationType;
import javax.annotation.Nonnull;
import java.util.Objects;

public class ObservationMetadata {

    private final int metadataVersion;
    private final ObservationBaseFields baseFields;
    private final ObservationContextSensitiveFields contextSensitiveFields;

    public ObservationMetadata(@Nonnull final ObservationBaseFields baseFields,
                               @Nonnull final ObservationContextSensitiveFields contextSensitiveFields) {

        this.baseFields = Objects.requireNonNull(baseFields);
        this.contextSensitiveFields = Objects.requireNonNull(contextSensitiveFields);

        Preconditions.checkArgument(
                baseFields.getSpecies().equals(contextSensitiveFields.getSpecies()), "Game species mismatch");

        Preconditions.checkArgument(
                baseFields.getMetadataVersion() == contextSensitiveFields.getMetadataVersion(),
                "Metadata version mismatch");

        this.metadataVersion = baseFields.getMetadataVersion();

        Preconditions.checkArgument(
                baseFields.getWithinMooseHunting().isAllowedField() || !contextSensitiveFields.isWithinMooseHunting(),
                "Conflicting metadata with regard to associability to moose hunting");
    }

    public GameSpecies getSpecies() {
        return baseFields.getSpecies();
    }

    public int getMetadataVersion() {
        return metadataVersion;
    }

    public ObservationBaseFields getBaseFields() {
        return baseFields;
    }

    public Boolean getWithinMooseHunting() {
        return baseFields.getWithinMooseHunting() != Required.NO ? contextSensitiveFields.isWithinMooseHunting() : null;
    }

    public ObservationContextSensitiveFields getContextSensitiveFields() {
        return contextSensitiveFields;
    }

    public ObservationType getObservationType() {
        return contextSensitiveFields.getObservationType();
    }

    public Required getAmount() {
        return contextSensitiveFields.getAmount();
    }

    public void setAmount(@Nonnull final Required amount) {
        Objects.requireNonNull(amount);
        contextSensitiveFields.setAmount(amount);
    }

}
