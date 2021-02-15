package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.Required;
import fi.riista.feature.error.ProhibitedFieldFound;
import fi.riista.feature.error.RequiredFieldMissing;
import fi.riista.feature.gamediary.DeerHuntingType;
import fi.riista.feature.gamediary.fixture.ObservationFixtureMixin;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.junit.Before;
import org.junit.Test;

import static fi.riista.feature.gamediary.DeerHuntingType.OTHER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER;
import static fi.riista.feature.gamediary.observation.ObservationCategory.DEER_HUNTING;
import static fi.riista.feature.gamediary.observation.ObservationSpecVersion.MOST_RECENT;
import static fi.riista.feature.gamediary.observation.ObservationTestAsserts.assertLargeCarnivoreFieldsAreNull;
import static fi.riista.feature.gamediary.observation.ObservationTestAsserts.assertMooselikeAmountFieldsNotNull;
import static fi.riista.feature.gamediary.observation.ObservationType.NAKO;
import static fi.riista.feature.gamediary.observation.metadata.DynamicObservationFieldPresence.VOLUNTARY_DEER_PILOT;
import static fi.riista.feature.gamediary.observation.metadata.DynamicObservationFieldPresence.YES_DEER_PILOT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class MobileObservationFeature_DeerPilotTest extends MobileObservationFeatureTestBase
        implements HuntingGroupFixtureMixin, ObservationFixtureMixin {

    private HuntingGroupFixture fixture;

    @Before
    public void setup() {
        fixture = new HuntingGroupFixture(model());
    }

    @Test
    public void testCreateDeerPilotObservationWithPilotUser() {
        enableDeerPilot();
        testCreateDeerPilotObservation(OTHER, "DeerHuntingTypeDescription");
    }

    @Test(expected = ProhibitedFieldFound.class)
    public void testCreateDeerPilotObservationWithNonPilotUser() {
        testCreateDeerPilotObservation(OTHER, "DeerHuntingTypeDescription");
    }

    @Test
    public void testCreateDeerPilotObservationWithoutVoluntaryField() {
        enableDeerPilot();
        testCreateDeerPilotObservation(OTHER, null);
    }

    @Test(expected = RequiredFieldMissing.class)
    public void testCreateDeerPilotObservationWithoutRequiredField() {
        enableDeerPilot();
        testCreateDeerPilotObservation(null, "DeerHuntingTypeDescription");
    }

    private void testCreateDeerPilotObservation(final DeerHuntingType deerHuntingType,
                                                final String deerHuntingTypeDescription) {

        final Person author = fixture.groupLeader;

        createObservationMetaF(OFFICIAL_CODE_WHITE_TAILED_DEER, MOST_RECENT, DEER_HUNTING, NAKO)
                .forMobile()
                .withDeerHuntingTypeFieldsAs(YES_DEER_PILOT, VOLUNTARY_DEER_PILOT)
                .withMooselikeAmountFieldsAs(Required.YES)
                .consumeBy(obsMeta -> {

                    onSavedAndAuthenticated(createUser(author), () -> {

                        final MobileObservationDTO inputDto = obsMeta.dtoBuilder()
                                .withDeerHuntingType(deerHuntingType)
                                .withDeerHuntingTypeDescription(deerHuntingTypeDescription)
                                .mutateMooselikeAmountFields()
                                .withDescription("Some Description")
                                .build();

                        final MobileObservationDTO outputDto = invokeCreateObservation(inputDto);

                        runInTransaction(() -> {
                            final Observation observation = observationRepo.getOne(outputDto.getId());

                            assertVersion(observation, 0);
                            assertThat(observation.isFromMobile(), is(true));

                            final Long mobileClientRefId = observation.getMobileClientRefId();
                            assertThat(mobileClientRefId, is(notNullValue()));
                            assertThat(mobileClientRefId, equalTo(inputDto.getMobileClientRefId()));

                            final GeoLocation geoLocation = observation.getGeoLocation();
                            assertThat(geoLocation, equalTo(inputDto.getGeoLocation()));
                            assertThat(geoLocation.getSource(), equalTo(GeoLocation.Source.GPS_DEVICE));

                            assertThat(observation.getPointOfTime(), equalTo(DateUtil.toDateTimeNullSafe(inputDto.getPointOfTime())));

                            assertThat(observation.getSpecies().getOfficialCode(), equalTo(OFFICIAL_CODE_WHITE_TAILED_DEER));
                            assertThat(observation.getObservationCategory(), equalTo(DEER_HUNTING));
                            assertThat(observation.getObservationType(), equalTo(NAKO));

                            assertThat(observation.getDeerHuntingType(), equalTo(deerHuntingType));
                            assertThat(observation.getDeerHuntingTypeDescription(), equalTo(deerHuntingTypeDescription));

                            assertThat(observation.getDescription(), equalTo("Some Description"));

                            assertMooselikeAmountFieldsNotNull(observation, true, true);
                            assertThat(observation.getAmount(), equalTo(inputDto.getSumOfMooselikeAmounts()));

                            assertAuthorAndActor(observation, F.getId(author), F.getId(author));

                            assertLargeCarnivoreFieldsAreNull(observation);
                            assertThat(observation.getHuntingDayOfGroup(), is(nullValue()));

                            assertThat(findSpecimens(observation), hasSize(0));
                        });
                    });
                });
    }

    private void enableDeerPilot() {
        model().newDeerPilot(fixture.permit);
    }
}
