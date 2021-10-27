package fi.riista.feature.huntingclub.hunting.mobile;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.observation.Observation;
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

import static fi.riista.util.DateUtil.localDateTime;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

public class MobileGroupObservationDTOBuilderForTests
        extends MobileGroupObservationDTO.Builder<MobileGroupObservationDTO, MobileGroupObservationDTOBuilderForTests> implements ValueGeneratorMixin {

    public static MobileGroupObservationDTOBuilderForTests create(@Nonnull final ObservationMetadata metadata) {
        return new MobileGroupObservationDTOBuilderForTests(metadata)
                .withSomeMobileClientRefId()
                .withSomeGeoLocation()
                .withPointOfTime(localDateTime())
                .withDescription("description");
    }

    private final ObservationMetadata metadata;

    private boolean isCarnivoreAuthority = false;

    public MobileGroupObservationDTOBuilderForTests(@Nonnull final ObservationMetadata metadata) {
        super(requireNonNull(metadata, "metadata is null").getBaseFields());

        this.metadata = metadata;

        applyMetadata();
    }

    @Override
    protected MobileGroupObservationDTO createDTO() {
        return new MobileGroupObservationDTO();
    }

    @Override
    public NumberGenerator getNumberGenerator() {
        return NumberSequence.INSTANCE;
    }

    public MobileGroupObservationDTOBuilderForTests withCarnivoreAuthority(final boolean enabled) {
        this.isCarnivoreAuthority = enabled;
        return self();
    }

    public MobileGroupObservationDTOBuilderForTests populateWith(@Nonnull final Observation observation) {

        dto.setLinkedToGroupHuntingDay(observation.getHuntingDayOfGroup() != null);

        final ObservationContextSensitiveFields contextSensitiveFields = metadata.getContextSensitiveFields();

        if (metadata.getSpecVersion().supportsDeerHuntingType()) {
            if (contextSensitiveFields.getDeerHuntingType().isDeerHuntingFieldAllowed()) {
                dto.setDeerHuntingType(observation.getDeerHuntingType());
            }
            if (contextSensitiveFields.getDeerHuntingTypeDescription().isDeerHuntingFieldAllowed()) {
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

    public MobileGroupObservationDTOBuilderForTests withSomeMobileClientRefId() {
        return withMobileClientRefId(getNumberGenerator().nextLong());
    }

    public MobileGroupObservationDTOBuilderForTests withSomeGeoLocation() {
        return withGeoLocation(geoLocation(GeoLocation.Source.GPS_DEVICE));
    }

    public MobileGroupObservationDTOBuilderForTests mutate() {
        return withSomeGeoLocation()
                .withPointOfTime(ofNullable(dto.getPointOfTime()).map(ldt -> ldt.minusDays(1)).orElse(null))
                .withDescription(ofNullable(dto.getDescription()).orElse("") + "CHANGED")
                .mutateMooselikeAmountFields();
    }

    public MobileGroupObservationDTOBuilderForTests mutateMooselikeAmountFields() {
        metadata.mutateMooselikeAmountFields(dto);
        return self();
    }

    public MobileGroupObservationDTOBuilderForTests mutateLargeCarnivoreFields() {
        metadata.mutateLargeCarnivoreFields(dto);
        return self();
    }

    public MobileGroupObservationDTOBuilderForTests mutateSpecimens() {
        dto.getSpecimens().forEach(specimen -> {
            metadata.mutateContent(specimen, isCarnivoreAuthority);
        });
        return self();
    }

    public MobileGroupObservationDTOBuilderForTests linkToHuntingDay(@Nonnull final GroupHuntingDay huntingDay) {
        requireNonNull(huntingDay);

        dto.setHuntingDayId(F.getId(huntingDay));

        if (!huntingDay.containsInstant(dto.getPointOfTime())) {
            dto.setPointOfTime(huntingDay.getStartAsLocalDateTime());
        }

        return self();

    }

    @Override
    protected MobileGroupObservationDTOBuilderForTests self() {
        return this;
    }

    protected MobileGroupObservationDTOBuilderForTests applyMetadata() {
        return withGameSpeciesCode(metadata.getSpecies().getOfficialCode())
                .withObservationBaseFields(metadata.getBaseFields())
                .withObservationCategory(metadata.getObservationCategory())
                .withObservationType(metadata.getObservationType());
    }

    private boolean isAmountLegal() {
        return metadata.isAmountLegal(isCarnivoreAuthority);
    }
}
