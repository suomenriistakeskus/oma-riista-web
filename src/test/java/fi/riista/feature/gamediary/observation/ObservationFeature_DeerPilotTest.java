package fi.riista.feature.gamediary.observation;

import fi.riista.feature.common.entity.Required;
import fi.riista.feature.error.ProhibitedFieldFound;
import fi.riista.feature.error.RequiredFieldMissing;
import fi.riista.feature.gamediary.DeerHuntingType;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.fixture.ObservationFixtureMixin;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.organization.person.Person;
import org.junit.Before;
import org.junit.Test;

import static fi.riista.feature.gamediary.DeerHuntingType.OTHER;
import static fi.riista.feature.gamediary.observation.ObservationCategory.DEER_HUNTING;
import static fi.riista.feature.gamediary.observation.ObservationType.NAKO;
import static fi.riista.feature.gamediary.observation.metadata.DynamicObservationFieldPresence.VOLUNTARY;
import static fi.riista.feature.gamediary.observation.metadata.DynamicObservationFieldPresence.YES;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class ObservationFeature_DeerPilotTest extends ObservationFeatureTestBase
        implements HuntingGroupFixtureMixin, ObservationFixtureMixin {

    private HuntingGroupFixture huntingFixture;

    @Before
    public void setup() {
        huntingFixture = new HuntingGroupFixture(model());
    }

    @Test
    public void testCreateObservationWithPilotUser() {
        testCreateDeerPilotObservation(OTHER, "Description");
    }

    @Test
    public void testCreateObservationWithoutVoluntaryField() {
        testCreateDeerPilotObservation(OTHER, null);
    }

    @Test(expected = RequiredFieldMissing.class)
    public void testCreateObservationWithoutRequiredField() {
        testCreateDeerPilotObservation(null, "Description");
    }

    private void testCreateDeerPilotObservation(final DeerHuntingType type, final String description) {
        final GameSpecies whileTailedDeer = model().newGameSpeciesWhiteTailedDeer();
        final Person author = huntingFixture.groupLeader;

        createObservationMetaF(whileTailedDeer, DEER_HUNTING, NAKO)
                .withDeerHuntingTypeFieldsAs(YES, VOLUNTARY)
                .withMooselikeAmountFieldsAs(Required.YES)
                .consumeBy(obsMeta -> {

                    onSavedAndAuthenticated(createUser(author), () -> {

                        final ObservationDTO inputDto = obsMeta.dtoBuilder()
                                .withDeerHuntingType(type)
                                .withDeerHuntingTypeDescription(description)
                                .mutateMooselikeAmountFields()
                                .withActorInfo(author)
                                .build();

                        final ObservationDTO outputDto = invokeCreateObservation(inputDto);

                        runInTransaction(() -> {
                            final Observation observation = assertObservationCreated(outputDto.getId());

                            assertVersion(observation, 0);

                            assertThat(observation.getObservationCategory(), equalTo(DEER_HUNTING));
                            assertThat(observation.getObservationType(), equalTo(NAKO));

                            assertThat(observation.getDeerHuntingType(), is(notNullValue()));
                            assertThat(observation.getDeerHuntingType(), equalTo(type));
                            assertThat(observation.getDeerHuntingTypeDescription(), equalTo(description));
                        });
                    });
                });
    }
}
