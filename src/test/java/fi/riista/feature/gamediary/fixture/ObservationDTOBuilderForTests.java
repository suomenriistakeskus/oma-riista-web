package fi.riista.feature.gamediary.fixture;

import com.google.common.base.Preconditions;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationDTO;
import fi.riista.feature.gamediary.observation.metadata.ObservationContextParameters;
import fi.riista.feature.gamediary.observation.metadata.ObservationMetadata;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.util.F;
import fi.riista.util.NumberGenerator;
import fi.riista.util.NumberSequence;
import fi.riista.util.ValueGeneratorMixin;
import org.hamcrest.Matcher;

import javax.annotation.Nonnull;
import java.util.Objects;

import static fi.riista.test.TestUtils.createList;
import static fi.riista.util.DateUtil.localDateTime;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.junit.Assert.assertThat;

public class ObservationDTOBuilderForTests extends ObservationDTO.Builder<ObservationDTOBuilderForTests>
        implements ValueGeneratorMixin {

    public static ObservationDTOBuilderForTests create(@Nonnull final ObservationMetadata metadata,
                                                       @Nonnull final ObservationContextParameters params) {

        return new ObservationDTOBuilderForTests(metadata, params)
                .withSomeGeoLocation()
                .withPointOfTime(localDateTime())
                .withDescription("description")
                .withAmountAndSpecimens(0);
    }

    private final ObservationMetadata metadata;
    private final ObservationContextParameters params;

    public ObservationDTOBuilderForTests(@Nonnull final ObservationMetadata metadata,
                                         @Nonnull final ObservationContextParameters params) {

        this.metadata = Objects.requireNonNull(metadata, "metadata is null");
        this.params = Objects.requireNonNull(params, "params is null");

        applyMetadata();
    }

    @Override
    public NumberGenerator getNumberGenerator() {
        return NumberSequence.INSTANCE;
    }

    public ObservationDTOBuilderForTests populateWith(@Nonnull final Observation observation) {
        return super.populateWith(observation, true).applyMetadata();
    }

    public ObservationDTOBuilderForTests populateWith(@Nonnull final ObservationDTO replacement) {
        this.dto = Objects.requireNonNull(replacement);
        return applyMetadata();
    }

    public ObservationDTOBuilderForTests withSomeGeoLocation() {
        return withGeoLocation(geoLocation(GeoLocation.Source.MANUAL));
    }

    public ObservationDTOBuilderForTests withSpecimens(final int numSpecimens) {
        return withSpecimens(createList(numSpecimens, () -> {
            return metadata.newObservationSpecimenDTO(params.isUserAssignedCarnivoreAuthority());
        }));
    }

    public ObservationDTOBuilderForTests withAmountAndSpecimens(final int numSpecimens) {
        Preconditions.checkArgument(numSpecimens >= 0, "numSpecimens must not be negative");

        final boolean isAmountLegal = metadata.isAmountLegal(params.isUserAssignedCarnivoreAuthority());

        if (numSpecimens > 0) {
            Preconditions.checkArgument(isAmountLegal, "numSpecimens > 0 contradicts with illegal amount field");
            return withAmount(numSpecimens).withSpecimens(numSpecimens);
        } else if (isAmountLegal) {
            if (dto.getAmount() == null) {
                withAmount(1);
            }
            return withSpecimens(emptyList());
        }

        return withAmount(null).withSpecimens(null);
    }

    public ObservationDTOBuilderForTests mutate() {
        return withSomeGeoLocation()
                .withPointOfTime(ofNullable(dto.getPointOfTime()).map(ldt -> ldt.minusDays(1)).orElse(null))
                .withDescription(ofNullable(dto.getDescription()).orElse("") + "CHANGED")
                .mutateMooselikeAmountFields()
                .mutateLargeCarnivoreFields();
    }

    public ObservationDTOBuilderForTests mutateMooselikeAmountFields() {
        metadata.mutateMooselikeAmountFields(dto);
        return metadata.isAmountLegal(params.isUserAssignedCarnivoreAuthority()) ? self() : withAmount(null);
    }

    public ObservationDTOBuilderForTests mutateLargeCarnivoreFields() {
        metadata.mutateLargeCarnivoreFields(dto, params.isUserAssignedCarnivoreAuthority());
        return self();
    }

    public ObservationDTOBuilderForTests mutateLargeCarnivoreFieldsAsserting(@Nonnull final Matcher<Object> matcher) {
        Objects.requireNonNull(matcher);

        mutateLargeCarnivoreFields();

        assertThat("verifiedByCarnivoreAuthority", dto.getVerifiedByCarnivoreAuthority(), matcher);
        assertThat("observerName", dto.getObserverName(), matcher);
        assertThat("observerPhoneNumber", dto.getObserverPhoneNumber(), matcher);
        assertThat("officialAdditionalInfo", dto.getOfficialAdditionalInfo(), matcher);

        return self();
    }

    public ObservationDTOBuilderForTests mutateSpecimens() {
        dto.getSpecimens().forEach(specimen -> {
            metadata.mutateContent(specimen, params.isUserAssignedCarnivoreAuthority());
        });
        return self();
    }

    public ObservationDTOBuilderForTests linkToHuntingDay(@Nonnull final GroupHuntingDay huntingDay) {
        Objects.requireNonNull(huntingDay);
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
        return populateWith(metadata.getSpecies())
                .withWithinMooseHunting(metadata.getWithinMooseHunting())
                .withObservationType(metadata.getObservationType());
    }
}
