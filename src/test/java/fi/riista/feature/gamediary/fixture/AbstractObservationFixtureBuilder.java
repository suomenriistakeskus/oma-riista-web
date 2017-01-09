package fi.riista.feature.gamediary.fixture;

import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.feature.gamediary.observation.metadata.ObservationMetadata;

import javax.annotation.Nonnull;

import java.util.Objects;
import java.util.function.Consumer;

abstract class AbstractObservationFixtureBuilder<META extends ObservationMetadata, FIXTURE> {

    protected final META metadata;
    protected final EntitySupplier entitySupplier;
    protected final boolean forMobileAPI;

    protected AbstractObservationFixtureBuilder(@Nonnull final META metadata,
                                                @Nonnull final EntitySupplier entitySupplier,
                                                final boolean forMobileAPI) {

        this.metadata = Objects.requireNonNull(metadata, "metadata is null");
        this.entitySupplier = Objects.requireNonNull(entitySupplier, "entitySupplier is null");
        this.forMobileAPI = forMobileAPI;
    }

    public abstract FIXTURE build();

    public void consumeBy(@Nonnull final Consumer<FIXTURE> consumer) {
        Objects.requireNonNull(consumer);
        consumer.accept(build());
    }

}
