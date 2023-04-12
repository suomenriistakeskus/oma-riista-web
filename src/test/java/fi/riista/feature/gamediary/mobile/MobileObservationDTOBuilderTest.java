package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.common.entity.Required;
import fi.riista.feature.gamediary.DeerHuntingType;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.observation.ObservationCategory;
import fi.riista.feature.gamediary.observation.ObservationSpecVersion;
import fi.riista.feature.gamediary.observation.ObservationType;
import fi.riista.feature.gamediary.observation.metadata.ObservationBaseFields;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.stream.Stream;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_CANADIAN_BEAVER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_EUROPEAN_BEAVER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MOOSE;
import static fi.riista.feature.gamediary.observation.ObservationType.NAKO;
import static fi.riista.feature.gamediary.observation.ObservationType.PARI;
import static fi.riista.feature.gamediary.observation.ObservationType.PESA;
import static fi.riista.feature.gamediary.observation.ObservationType.PESA_KEKO;
import static fi.riista.feature.gamediary.observation.ObservationType.PESA_PENKKA;
import static fi.riista.feature.gamediary.observation.ObservationType.PESA_SEKA;
import static fi.riista.feature.gamediary.observation.ObservationType.POIKUE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(Theories.class)
public class MobileObservationDTOBuilderTest {

    @DataPoints("specVersions")
    public static final ObservationSpecVersion[] SPEC_VERSIONS = ObservationSpecVersion.values();

    private GameSpecies species;
    private ObservationBaseFields baseFields;

    private void setUp(final ObservationSpecVersion version) {
        species = new GameSpecies();
        species.setOfficialCode(1);

        baseFields = new ObservationBaseFields(species, version.getMetadataVersion());
    }

    @Theory
    public void testPlain(final ObservationSpecVersion version) {
        setUp(version);

        final MobileObservationDTO dto = MobileObservationDTO.builder(baseFields).build();

        assertEquals(
                ObservationSpecVersion.fromIntValue(baseFields.getMetadataVersion()), dto.getObservationSpecVersion());
        assertNull(dto.getMobileClientRefId());

        assertEquals(species.getOfficialCode(), dto.getGameSpeciesCode());
        assertNull(dto.getObservationCategory());
        assertNull(dto.getObservationType());
    }

    @Theory
    public void testWithMobileClientRefId(final ObservationSpecVersion version) {
        setUp(version);

        final MobileObservationDTO dto = MobileObservationDTO
                .builder(baseFields)
                .withMobileClientRefId(123L)
                .build();

        assertEquals(Long.valueOf(123L), dto.getMobileClientRefId());
    }

    @Theory
    public void testWithGameSpeciesCode(final ObservationSpecVersion version) {
        setUp(version);

        final MobileObservationDTO dto = MobileObservationDTO
                .builder(baseFields)
                .withGameSpeciesCode(OFFICIAL_CODE_MOOSE)
                .build();

        assertEquals(OFFICIAL_CODE_MOOSE, dto.getGameSpeciesCode());
    }

    @Theory
    public void testWithObservationCategory_whenBaseFieldsEnablesMooseHunting(final ObservationSpecVersion version) {
        setUp(version);

        baseFields.setWithinMooseHunting(Required.VOLUNTARY);

        Stream.of(ObservationCategory.values()).forEach(category -> {

            final MobileObservationDTO dto = MobileObservationDTO
                    .builder(baseFields)
                    .withObservationCategory(category)
                    .build();

            if (version.supportsCategory()) {
                assertEquals(category, dto.getObservationCategory());
                assertNull(dto.getWithinMooseHunting());
            } else {
                assertNull(dto.getObservationCategory());
                assertNotNull(dto.getWithinMooseHunting());
                assertEquals(category.isWithinMooseHunting(), dto.getWithinMooseHunting());
            }
        });
    }

    @Theory
    public void testWithObservationCategory_whenBaseFieldsDisablesMooseHunting(final ObservationSpecVersion version) {
        setUp(version);

        baseFields.setWithinMooseHunting(Required.NO);

        Stream.of(ObservationCategory.values()).forEach(category -> {

            final MobileObservationDTO dto = MobileObservationDTO
                    .builder(baseFields)
                    .withObservationCategory(category)
                    .build();

            if (version.supportsCategory()) {
                assertEquals(category, dto.getObservationCategory());
            } else {
                assertNull(dto.getObservationCategory());
            }

            assertNull(dto.getWithinMooseHunting());
        });
    }

    @Theory
    public void testWithObservationType(final ObservationSpecVersion version) {
        setUp(version);

        Stream.of(OFFICIAL_CODE_CANADIAN_BEAVER, OFFICIAL_CODE_EUROPEAN_BEAVER, OFFICIAL_CODE_MOOSE)
                .forEach(speciesCode -> {

                    species.setOfficialCode(speciesCode);

                    Stream.of(ObservationType.values()).forEach(type -> {

                        final MobileObservationDTO dto = MobileObservationDTO
                                .builder(baseFields)
                                .withObservationType(type)
                                .build();

                        if (species.isBeaver()
                                && !version.supportsExtraBeaverTypes()
                                && (type == PESA_KEKO || type == PESA_PENKKA || type == PESA_SEKA)) {

                            assertEquals(PESA, dto.getObservationType());
                        } else if (!version.supportsBirdLitterAndCoupleObservationTypes()
                                && (type == POIKUE || type == PARI)) {

                            assertEquals(NAKO, dto.getObservationType());
                        } else {
                            assertEquals(type, dto.getObservationType());
                        }
                    });
                });
    }

    @Theory
    public void testWithDeerHuntingType(final ObservationSpecVersion version) {
        setUp(version);

        Stream.of(DeerHuntingType.values()).forEach(deerHuntingType -> {

            final MobileObservationDTO dto = MobileObservationDTO
                    .builder(baseFields)
                    .withDeerHuntingType(deerHuntingType)
                    .build();

            if (version.supportsDeerHuntingType()) {
                assertEquals(deerHuntingType, dto.getDeerHuntingType());
            } else {
                assertNull(dto.getDeerHuntingType());
            }
        });
    }
}
