package fi.riista.feature.gamediary.observation;

import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.gamediary.GameDiaryEntryFeatureTest;
import fi.riista.feature.gamediary.GameDiaryFeature;
import fi.riista.feature.gamediary.observation.metadata.ObservationContextParameters;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenRepository;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen_;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.F;
import org.springframework.data.jpa.domain.JpaSort;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static fi.riista.test.Asserts.assertEmpty;
import static fi.riista.util.DateUtil.today;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public abstract class ObservationFeatureTestBase extends GameDiaryEntryFeatureTest {

    @Resource
    protected GameDiaryFeature feature;

    @Resource
    protected ObservationRepository observationRepo;

    @Resource
    protected ObservationSpecimenRepository observationSpecimenRepo;

    @Resource
    protected UserAuthorizationHelper userAuthorizationHelper;

    protected final ObservationContextParameters params = new ObservationContextParameters(() -> {
        return callInTransaction(() -> userAuthorizationHelper.isCarnivoreContactPersonAnywhere(today()))
                .booleanValue();
    });

    protected void doCreateAssertions(final long observationId,
                                      final ObservationDTO expectedValues,
                                      final Person expectedAuthor,
                                      final Person expectedActor) {

        doCreateAssertions(observationId, expectedValues, expectedAuthor, expectedActor, h -> {});
    }

    protected void doCreateAssertions(final long observationId,
                                      final ObservationDTO expectedValues,
                                      final Person expectedAuthor,
                                      final Person expectedActor,
                                      final Consumer<Observation> additionalAssertions) {

        runInTransaction(() -> {
            final Observation observation = observationRepo.findOne(observationId);
            assertObservationExpectations(observation, expectedValues);
            assertVersion(observation, 0);

            validateAuthorAndActor(observation, F.getId(expectedAuthor), F.getId(expectedActor));

            additionalAssertions.accept(observation);
        });
    }

    protected void doUpdateAssertions(final ObservationDTO expectedValues,
                                      final Person expectedAuthor,
                                      final Person expectedActor,
                                      final int expectedRevision) {

        doUpdateAssertions(expectedValues, expectedAuthor, expectedActor, expectedRevision, h -> {});
    }

    protected void doUpdateAssertions(final ObservationDTO expectedValues,
                                      final Person expectedAuthor,
                                      final Person expectedActor,
                                      final int expectedRevision,
                                      final Consumer<Observation> additionalAssertions) {

        runInTransaction(() -> {
            final Observation observation = observationRepo.findOne(expectedValues.getId());
            assertObservationExpectations(observation, expectedValues);
            assertVersion(observation, expectedRevision);

            validateAuthorAndActor(observation, F.getId(expectedAuthor), F.getId(expectedActor));

            additionalAssertions.accept(observation);
        });
    }

    protected void assertObservationExpectations(final Observation observation, final ObservationDTO expectedValues) {
        assertCommonExpectations(observation, expectedValues);
        assertFalse(observation.isFromMobile());

        assertEquals(expectedValues.getWithinMooseHunting(), observation.getWithinMooseHunting());
        assertEquals(expectedValues.getObservationType(), observation.getObservationType());

        assertEquals(expectedValues.getAmount(), observation.getAmount());
        assertEquals(expectedValues.getMooselikeMaleAmount(), observation.getMooselikeMaleAmount());
        assertEquals(expectedValues.getMooselikeFemaleAmount(), observation.getMooselikeFemaleAmount());
        assertEquals(expectedValues.getMooselikeCalfAmount(), observation.getMooselikeCalfAmount());
        assertEquals(expectedValues.getMooselikeFemale1CalfAmount(), observation.getMooselikeFemale1CalfAmount());
        assertEquals(expectedValues.getMooselikeFemale2CalfsAmount(), observation.getMooselikeFemale2CalfsAmount());
        assertEquals(expectedValues.getMooselikeFemale3CalfsAmount(), observation.getMooselikeFemale3CalfsAmount());
        assertEquals(expectedValues.getMooselikeFemale4CalfsAmount(), observation.getMooselikeFemale4CalfsAmount());
        assertEquals(
                expectedValues.getMooselikeUnknownSpecimenAmount(), observation.getMooselikeUnknownSpecimenAmount());

        Optional.ofNullable(expectedValues.getActorInfo())
                .ifPresent(actor -> assertEquals(actor.getId(), observation.getObserver().getId()));

        assertEquals(expectedValues.getHuntingDayId(), F.getId(observation.getHuntingDayOfGroup()));

        assertEquals(expectedValues.getVerifiedByCarnivoreAuthority(), observation.getVerifiedByCarnivoreAuthority());
        assertEquals(expectedValues.getObserverName(), observation.getObserverName());
        assertEquals(expectedValues.getObserverPhoneNumber(), observation.getObserverPhoneNumber());
        assertEquals(expectedValues.getOfficialAdditionalInfo(), observation.getOfficialAdditionalInfo());

        final List<ObservationSpecimen> actualSpecimens =
                observationSpecimenRepo.findByObservation(observation, new JpaSort(ObservationSpecimen_.id));

        if (expectedValues.getSpecimens() == null) {
            assertEmpty(actualSpecimens);
        } else {
            assertSpecimens(actualSpecimens, expectedValues.getSpecimens(), expectedValues.specimenOps()::equalContent);
        }
    }

    protected ObservationDTO invokeCreateObservation(final ObservationDTO input) {
        return withVersionChecked(feature.createObservation(input));
    }

    protected ObservationDTO invokeUpdateObservation(final ObservationDTO input) {
        return withVersionChecked(feature.updateObservation(input));
    }

    protected ObservationDTO withVersionChecked(final ObservationDTO dto) {
        return checkDtoVersionAgainstEntity(dto, Observation.class);
    }
}
