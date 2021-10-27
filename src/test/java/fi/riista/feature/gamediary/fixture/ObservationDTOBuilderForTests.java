package fi.riista.feature.gamediary.fixture;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationDTO;
import fi.riista.feature.gamediary.observation.metadata.ObservationContextSensitiveFields;
import fi.riista.feature.gamediary.observation.metadata.ObservationMetadata;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
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

public class ObservationDTOBuilderForTests extends ObservationDTO.Builder<ObservationDTOBuilderForTests>
        implements ValueGeneratorMixin {

    public static ObservationDTOBuilderForTests create(@Nonnull final ObservationMetadata metadata) {
        return new ObservationDTOBuilderForTests(metadata)
                .withSomeGeoLocation()
                .withPointOfTime(localDateTime())
                .withDescription("description");
    }

    private final ObservationMetadata metadata;

    private boolean isCarnivoreAuthority = false;

    public ObservationDTOBuilderForTests(@Nonnull final ObservationMetadata metadata) {
        this.metadata = requireNonNull(metadata);

        applyMetadata();
    }

    @Override
    public NumberGenerator getNumberGenerator() {
        return NumberSequence.INSTANCE;
    }

    public ObservationDTOBuilderForTests withCarnivoreAuthority(final boolean enabled) {
        this.isCarnivoreAuthority = enabled;
        return self();
    }

    public ObservationDTOBuilderForTests populateWith(@Nonnull final Observation observation) {

        dto.setRhyId(F.getId(observation.getRhy()));
        dto.setHuntingDayId(F.getId(observation.getHuntingDayOfGroup()));
        dto.setPointOfTimeApprovedToHuntingDay(
                DateUtil.toLocalDateTimeNullSafe(observation.getPointOfTimeApprovedToHuntingDay()));

        dto.setModeratorOverride(observation.isModeratorOverride());
        dto.setUpdateableOnlyByCarnivoreAuthority(observation.isAnyLargeCarnivoreFieldPresent());

        final ObservationContextSensitiveFields contextSensitiveFields = metadata.getContextSensitiveFields();

        if (contextSensitiveFields.getDeerHuntingType().isDeerHuntingFieldAllowed()) {
            dto.setDeerHuntingType(observation.getDeerHuntingType());
        }
        if (contextSensitiveFields.getDeerHuntingTypeDescription().isDeerHuntingFieldAllowed()) {
            dto.setDeerHuntingTypeDescription(observation.getDeerHuntingTypeDescription());
        }

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

        final boolean isAmountLegal = isAmountLegal();

        return withIdAndRev(observation)

                .withGeoLocation(observation.getGeoLocation())
                .withPointOfTime(DateUtil.toLocalDateTimeNullSafe(observation.getPointOfTime()))

                .applyMetadata()

                .withAmount(isAmountLegal ? observation.getAmount() : null)
                .withMooselikeAmountsFrom(observation)

                .withDescription(observation.getDescription())

                .withSpecimens(isAmountLegal ? new ArrayList<>(0) : null);
    }

    public ObservationDTOBuilderForTests withSomeGeoLocation() {
        return withGeoLocation(geoLocation(GeoLocation.Source.MANUAL));
    }

    public ObservationDTOBuilderForTests withAmount(final int amount) {
        checkArgument(amount > 0, "amount must be positive");

        checkArgument(isAmountLegal(), "amount > 0 contradicts with illegal amount field");

        return super.withAmount(amount);
    }

    public ObservationDTOBuilderForTests withSpecimens(final int numSpecimens) {
        return withSpecimens(createList(numSpecimens, () -> {
            return metadata.newObservationSpecimenDTO(isCarnivoreAuthority);
        }));
    }

    public ObservationDTOBuilderForTests withAmountAndSpecimens(final int numSpecimens) {
        return withAmount(numSpecimens).withSpecimens(numSpecimens);
    }

    public ObservationDTOBuilderForTests mutate() {
        return withSomeGeoLocation()
                .withPointOfTime(ofNullable(dto.getPointOfTime()).map(ldt -> ldt.minusDays(1)).orElse(null))
                .withDescription(ofNullable(dto.getDescription()).orElse("") + "CHANGED")
                .mutateMooselikeAmountFields();
    }

    public ObservationDTOBuilderForTests mutateDeerHuntingTypeFields() {
        metadata.mutateDeerHuntingTypeFields(dto);
        return self();
    }

    public ObservationDTOBuilderForTests mutateMooselikeAmountFields() {
        metadata.mutateMooselikeAmountFields(dto);
        return self();
    }

    public ObservationDTOBuilderForTests withMooselikeAmountFieldsCleared() {
        metadata.withMooselikeAmountFieldsCleared(dto);
        return self();
    }

    public ObservationDTOBuilderForTests mutateLargeCarnivoreFields() {
        metadata.mutateLargeCarnivoreFields(dto);
        return self();
    }

    public ObservationDTOBuilderForTests mutateSpecimens() {
        dto.getSpecimens().forEach(specimen -> {
            metadata.mutateContent(specimen, isCarnivoreAuthority);
        });
        return self();
    }

    public ObservationDTOBuilderForTests linkToHuntingDay(@Nonnull final GroupHuntingDay huntingDay) {
        requireNonNull(huntingDay);

        dto.setHuntingDayId(F.getId(huntingDay));

        if (!huntingDay.containsInstant(dto.getPointOfTime())) {
            dto.setPointOfTime(huntingDay.getStartAsLocalDateTime());
        }

        return self();
    }

    @Override
    protected ObservationDTOBuilderForTests self() {
        return this;
    }

    protected ObservationDTOBuilderForTests applyMetadata() {
        return withGameSpeciesCode(metadata.getSpecies().getOfficialCode())
                .withObservationCategory(metadata.getObservationCategory())
                .withObservationType(metadata.getObservationType());
    }

    private boolean isAmountLegal() {
        return metadata.isAmountLegal(isCarnivoreAuthority);
    }
}
