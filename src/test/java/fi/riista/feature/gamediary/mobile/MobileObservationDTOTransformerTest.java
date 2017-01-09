package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.gamediary.GameDiaryEntryType;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationSpecVersion;
import fi.riista.util.DateUtil;
import fi.riista.util.VersionedTestExecutionSupport;

import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;

import static fi.riista.feature.gamediary.observation.ObservationSpecVersion.LOWEST_VERSION_SUPPORTING_XTRA_BEAVER_TYPES;
import static fi.riista.feature.gamediary.observation.ObservationSpecVersion.MOST_RECENT;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_CANADIAN_BEAVER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_EUROPEAN_BEAVER;
import static fi.riista.feature.gamediary.observation.ObservationType.PESA;
import static fi.riista.feature.gamediary.observation.ObservationType.PESA_KEKO;
import static fi.riista.util.TestUtils.createList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MobileObservationDTOTransformerTest extends EmbeddedDatabaseTest
        implements VersionedTestExecutionSupport<ObservationSpecVersion> {

    @Resource
    private MobileObservationDTOTransformer transformer;

    @Override
    public List<ObservationSpecVersion> getTestExecutionVersions() {
        return new ArrayList<>(EnumSet.allOf(ObservationSpecVersion.class));
    }

    @Test(expected = RuntimeException.class)
    public void testUserNotAuthenticated() {
        final List<Observation> observations = createList(5, model()::newObservation);
        persistInNewTransaction();
        transformer.apply(observations);
    }

    @Test
    public void testTranslationOfObsoleteBeaverObservationType() {
        forEachVersionBefore(LOWEST_VERSION_SUPPORTING_XTRA_BEAVER_TYPES, v -> withPerson(author -> {

            Stream.of(OFFICIAL_CODE_CANADIAN_BEAVER, OFFICIAL_CODE_EUROPEAN_BEAVER, OFFICIAL_CODE_BEAR)
                    .forEach(speciesCode -> {

                        final GameSpecies species = model().newGameSpecies(speciesCode);

                        createObservationMetaF(species, MOST_RECENT, PESA_KEKO).forMobile().consumeBy(currentMeta -> {

                            createObservationMetaF(species, v, PESA).forMobile().consumeBy(oldMeta -> {

                                final List<Observation> observations =
                                        createList(5, () -> model().newMobileObservation(author, currentMeta));

                                onSavedAndAuthenticated(createUser(author), () -> {

                                    final List<MobileObservationDTO> dtos = transformer.apply(observations, v);

                                    for (int i = 0; i < observations.size(); i++) {
                                        final MobileObservationDTO dto = dtos.get(i);
                                        assertNotNull(dto);

                                        assertEquals(species.isBeaver() ? PESA : PESA_KEKO, dto.getObservationType());
                                        assertFieldsExcludingPluralAssociations(observations.get(i), dto);
                                    }
                                });
                            });
                        });
                    });
        }));
    }

    private static void assertFieldsExcludingPluralAssociations(final Observation observation,
                                                                final MobileObservationDTO dto) {

        assertNotNull(observation.getId());
        assertNotNull(observation.getConsistencyVersion());

        assertEquals(observation.getId(), dto.getId());
        assertEquals(observation.getConsistencyVersion(), dto.getRev());
        Assert.assertEquals(GameDiaryEntryType.OBSERVATION, dto.getType());
        assertEquals(observation.getSpecies().getOfficialCode(), dto.getGameSpeciesCode());
        assertEquals(observation.getWithinMooseHunting(), dto.getWithinMooseHunting());
        assertEquals(observation.getGeoLocation(), dto.getGeoLocation());
        assertEquals(DateUtil.toLocalDateTimeNullSafe(observation.getPointOfTime()), dto.getPointOfTime());
        assertEquals(observation.getDescription(), dto.getDescription());

        assertTrue(observation.isAmountEqualTo(dto.getAmount()));
        assertEquals(observation.getMooselikeMaleAmount(), dto.getMooselikeMaleAmount());
        assertEquals(observation.getMooselikeFemaleAmount(), dto.getMooselikeFemaleAmount());
        assertEquals(observation.getMooselikeFemale1CalfAmount(), dto.getMooselikeFemale1CalfAmount());
        assertEquals(observation.getMooselikeFemale2CalfsAmount(), dto.getMooselikeFemale2CalfsAmount());
        assertEquals(observation.getMooselikeFemale3CalfsAmount(), dto.getMooselikeFemale3CalfsAmount());
        assertEquals(observation.getMooselikeFemale4CalfsAmount(), dto.getMooselikeFemale4CalfsAmount());
        assertEquals(observation.getMooselikeUnknownSpecimenAmount(), dto.getMooselikeUnknownSpecimenAmount());
    }

}
