package fi.riista.feature.gamediary.fixture;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.mobile.MobileObservationDTO;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.metadata.ObservationContextSensitiveFields;
import fi.riista.feature.gamediary.observation.metadata.ObservationMetadata;
import fi.riista.util.DateUtil;
import fi.riista.util.NumberGenerator;
import fi.riista.util.NumberSequence;
import fi.riista.util.ValueGeneratorMixin;

import javax.annotation.Nonnull;
import java.util.ArrayList;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.riista.test.TestUtils.createList;
import static fi.riista.util.DateUtil.localDateTime;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

public class MobileObservationDTOBuilderForTests
        extends MobileObservationDTO.Builder<MobileObservationDTOBuilderForTests> implements ValueGeneratorMixin {

    public static MobileObservationDTOBuilderForTests create(@Nonnull final ObservationMetadata metadata) {
        return new MobileObservationDTOBuilderForTests(metadata)
                .withSomeMobileClientRefId()
                .withSomeGeoLocation()
                .withPointOfTime(localDateTime())
                .withDescription("description");
    }

    private final ObservationMetadata metadata;

    private boolean isCarnivoreAuthority = false;
    private boolean isDeerPilotEnabled = false;

    public MobileObservationDTOBuilderForTests(@Nonnull final ObservationMetadata metadata) {
        super(requireNonNull(metadata, "metadata is null").getBaseFields());

        this.metadata = metadata;

        applyMetadata();
    }

    @Override
    public NumberGenerator getNumberGenerator() {
        return NumberSequence.INSTANCE;
    }

    public MobileObservationDTOBuilderForTests withCarnivoreAuthority(final boolean enabled) {
        this.isCarnivoreAuthority = enabled;
        return self();
    }

    public MobileObservationDTOBuilderForTests withDeerPilotEnabled() {
        this.isDeerPilotEnabled = true;
        return self();
    }

    public MobileObservationDTOBuilderForTests populateWith(@Nonnull final Observation observation) {

        dto.setLinkedToGroupHuntingDay(observation.getHuntingDayOfGroup() != null);

        final ObservationContextSensitiveFields contextSensitiveFields = metadata.getContextSensitiveFields();

        if (metadata.getSpecVersion().supportsDeerHuntingType()) {
            if (contextSensitiveFields.getDeerHuntingType().isDeerHuntingFieldAllowed(isDeerPilotEnabled)) {
                dto.setDeerHuntingType(observation.getDeerHuntingType());
            }
            if (contextSensitiveFields.getDeerHuntingTypeDescription().isDeerHuntingFieldAllowed(isDeerPilotEnabled)) {
                dto.setDeerHuntingTypeDescription(observation.getDeerHuntingTypeDescription());
            }
        }

        if (metadata.getSpecVersion().supportsLargeCarnivoreFields()) {
            if (contextSensitiveFields.getVerifiedByCarnivoreAuthority().isCarnivoreFieldAllowed(isCarnivoreAuthority)) {
                dto.setVerifiedByCarnivoreAuthority(observation.getVerifiedByCarnivoreAuthority());
            }
            if (contextSensitiveFields.getObserverName().isCarnivoreFieldAllowed(isCarnivoreAuthority)) {
                dto.setObserverName(observation.getObserverName());
            }
            if (contextSensitiveFields.getObserverPhoneNumber().isCarnivoreFieldAllowed(isCarnivoreAuthority)) {
                dto.setObserverPhoneNumber(observation.getObserverPhoneNumber());
            }
            if (contextSensitiveFields.getOfficialAdditionalInfo().isCarnivoreFieldAllowed(isCarnivoreAuthority)) {
                dto.setOfficialAdditionalInfo(observation.getOfficialAdditionalInfo());
            }
        }

        final boolean isAmountLegal = isAmountLegal();

        return withIdAndRev(observation)
                .withMobileClientRefId(observation.getMobileClientRefId())

                .withGeoLocation(observation.getGeoLocation())
                .withPointOfTime(DateUtil.toLocalDateTimeNullSafe(observation.getPointOfTime()))

                .applyMetadata()

                .withAmount(isAmountLegal ? observation.getAmount() : null)
                .withMooselikeAmountsFrom(observation)

                .withDescription(observation.getDescription())

                .withSpecimens(isAmountLegal ? new ArrayList<>(0) : null);
    }

    public MobileObservationDTOBuilderForTests withSomeMobileClientRefId() {
        return withMobileClientRefId(getNumberGenerator().nextLong());
    }

    public MobileObservationDTOBuilderForTests withSomeGeoLocation() {
        return withGeoLocation(geoLocation(GeoLocation.Source.GPS_DEVICE));
    }

    public MobileObservationDTOBuilderForTests withAmount(final int amount) {
        checkArgument(amount > 0, "amount must be positive");

        checkArgument(isAmountLegal(), "amount > 0 contradicts with illegal amount field");

        return super.withAmount(amount);
    }

    public MobileObservationDTOBuilderForTests withSpecimens(final int numSpecimens) {
        return withSpecimens(createList(numSpecimens, () -> {
            return metadata.newObservationSpecimenDTO(isCarnivoreAuthority);
        }));
    }

    public MobileObservationDTOBuilderForTests withAmountAndSpecimens(final int numSpecimens) {
        return withAmount(numSpecimens).withSpecimens(numSpecimens);
    }

    public MobileObservationDTOBuilderForTests mutate() {
        return withSomeGeoLocation()
                .withPointOfTime(ofNullable(dto.getPointOfTime()).map(ldt -> ldt.minusDays(1)).orElse(null))
                .withDescription(ofNullable(dto.getDescription()).orElse("") + "CHANGED")
                .mutateMooselikeAmountFields();
    }

    public MobileObservationDTOBuilderForTests mutateDeerHuntingTypeFields() {
        metadata.mutateDeerHuntingTypeFields(dto);
        return self();
    }

    public MobileObservationDTOBuilderForTests mutateMooselikeAmountFields() {
        metadata.mutateMooselikeAmountFields(dto);
        return self();
    }

    public MobileObservationDTOBuilderForTests withMooselikeAmountFieldsCleared() {
        metadata.withMooselikeAmountFieldsCleared(dto);
        return self();
    }

    public MobileObservationDTOBuilderForTests mutateLargeCarnivoreFields() {
        metadata.mutateLargeCarnivoreFields(dto);
        return self();
    }

    public MobileObservationDTOBuilderForTests mutateSpecimens() {
        dto.getSpecimens().forEach(specimen -> {
            metadata.mutateContent(specimen, isCarnivoreAuthority);
        });
        return self();
    }

    @Override
    protected MobileObservationDTOBuilderForTests self() {
        return this;
    }

    protected MobileObservationDTOBuilderForTests applyMetadata() {
        return withGameSpeciesCode(metadata.getSpecies().getOfficialCode())
                .withObservationBaseFields(metadata.getBaseFields())
                .withObservationCategory(metadata.getObservationCategory())
                .withObservationType(metadata.getObservationType());
    }

    private boolean isAmountLegal() {
        return metadata.isAmountLegal(isCarnivoreAuthority);
    }
}
