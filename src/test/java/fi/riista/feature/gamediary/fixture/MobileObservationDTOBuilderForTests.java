package fi.riista.feature.gamediary.fixture;

import com.google.common.base.Preconditions;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.mobile.MobileObservationDTO;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.metadata.ObservationContextParameters;
import fi.riista.feature.gamediary.observation.metadata.ObservationMetadata;
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

public class MobileObservationDTOBuilderForTests
        extends MobileObservationDTO.Builder<MobileObservationDTOBuilderForTests> implements ValueGeneratorMixin {

    public static MobileObservationDTOBuilderForTests create(@Nonnull final ObservationMetadata metadata,
                                                             @Nonnull final ObservationContextParameters params) {

        return new MobileObservationDTOBuilderForTests(metadata, params)
                .withSomeMobileClientRefId()
                .withSomeGeoLocation()
                .withPointOfTime(localDateTime())
                .withDescription("description")
                .withAmountAndSpecimens(0);
    }

    private final ObservationMetadata metadata;
    private final ObservationContextParameters params;

    public MobileObservationDTOBuilderForTests(@Nonnull final ObservationMetadata metadata,
                                               @Nonnull final ObservationContextParameters params) {

        super(Objects.requireNonNull(metadata, "metadata is null").getSpecVersion());

        this.metadata = metadata;
        this.params = Objects.requireNonNull(params, "params is null");

        applyMetadata();
    }

    @Override
    public NumberGenerator getNumberGenerator() {
        return NumberSequence.INSTANCE;
    }

    public MobileObservationDTOBuilderForTests populateWith(@Nonnull final Observation observation) {
        final boolean populateLargeCarnivoreFields = metadata.getSpecVersion().supportsLargeCarnivoreFields();
        return super.populateWith(observation, populateLargeCarnivoreFields).applyMetadata();
    }

    public MobileObservationDTOBuilderForTests populateWith(@Nonnull final MobileObservationDTO replacement) {
        this.dto = Objects.requireNonNull(replacement);
        return applyMetadata();
    }

    public MobileObservationDTOBuilderForTests withSomeMobileClientRefId() {
        return withMobileClientRefId(getNumberGenerator().nextLong());
    }

    public MobileObservationDTOBuilderForTests withSomeGeoLocation() {
        return withGeoLocation(geoLocation(GeoLocation.Source.GPS_DEVICE));
    }

    public MobileObservationDTOBuilderForTests withSpecimens(final int numSpecimens) {
        return withSpecimens(createList(numSpecimens, () -> {
            return metadata.newObservationSpecimenDTO(params.isUserAssignedCarnivoreAuthority());
        }));
    }

    public MobileObservationDTOBuilderForTests withAmountAndSpecimens(final int numSpecimens) {
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

    public MobileObservationDTOBuilderForTests mutate() {
        return withSomeGeoLocation()
                .withPointOfTime(ofNullable(dto.getPointOfTime()).map(ldt -> ldt.minusDays(1)).orElse(null))
                .withDescription(ofNullable(dto.getDescription()).orElse("") + "CHANGED")
                .mutateMooselikeAmountFields()
                .mutateLargeCarnivoreFields();
    }

    public MobileObservationDTOBuilderForTests mutateMooselikeAmountFields() {
        metadata.mutateMooselikeAmountFields(dto);
        return metadata.isAmountLegal(params.isUserAssignedCarnivoreAuthority()) ? self() : withAmount(null);
    }

    public MobileObservationDTOBuilderForTests mutateLargeCarnivoreFields() {
        metadata.mutateLargeCarnivoreFields(dto, params.isUserAssignedCarnivoreAuthority());
        return self();
    }

    public MobileObservationDTOBuilderForTests mutateLargeCarnivoreFieldsAsserting(
            @Nonnull final Matcher<Object> matcher) {

        Objects.requireNonNull(matcher);

        mutateLargeCarnivoreFields();

        assertThat("verifiedByCarnivoreAuthority", dto.getVerifiedByCarnivoreAuthority(), matcher);
        assertThat("observerName", dto.getObserverName(), matcher);
        assertThat("observerPhoneNumber", dto.getObserverPhoneNumber(), matcher);
        assertThat("officialAdditionalInfo", dto.getOfficialAdditionalInfo(), matcher);

        return self();
    }

    public MobileObservationDTOBuilderForTests mutateSpecimens() {
        dto.getSpecimens().forEach(specimen -> {
            metadata.mutateContent(specimen, params.isUserAssignedCarnivoreAuthority());
        });
        return self();
    }

    @Override
    protected MobileObservationDTOBuilderForTests self() {
        return this;
    }

    protected MobileObservationDTOBuilderForTests applyMetadata() {
        return withSpecVersion(metadata.getSpecVersion())
                .populateWith(metadata.getSpecies())
                .withWithinMooseHunting(metadata.getWithinMooseHunting())
                .withObservationType(metadata.getObservationType());
    }
}
