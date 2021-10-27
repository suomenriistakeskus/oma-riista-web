package fi.riista.feature.gamediary;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.Municipality;
import fi.riista.feature.gamediary.harvest.HarvestReportingType;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestField;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestFieldsRequestDTO;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestFieldsResponseDTO;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestSpecimenField;
import fi.riista.feature.gis.MockGISQueryService;
import fi.riista.feature.harvestpermit.season.HarvestArea;
import fi.riista.feature.harvestpermit.season.HarvestSeason;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import org.joda.time.LocalDate;
import org.junit.Ignore;
import org.junit.Test;

import javax.annotation.Resource;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAR;
import static fi.riista.feature.gamediary.harvest.HarvestSpecVersion.CURRENTLY_SUPPORTED;
import static fi.riista.util.DateUtil.today;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class GameDiaryMetadataFeatureTest extends EmbeddedDatabaseTest implements HuntingGroupFixtureMixin {

    @Resource
    private GameDiaryMetadataFeature feature;

    @Test
    public void testGetRequiredHarvestFields_insideSeason() {
        final GameSpecies species = model().newGameSpecies();
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final Municipality municipality = model().newMunicipality();
        final GeoLocation geoLocation = new GeoLocation(1, 1);
        final LocalDate seasonBegin = new LocalDate(2017, 8, 1);
        final LocalDate seasonEnd = new LocalDate(2017, 12, 31);
        final HarvestSeason harvestSeason = model().newHarvestSeason(species, seasonBegin, seasonEnd, seasonEnd);

        onSavedAndAuthenticated(createUserWithPerson(), () -> {

            final RequiredHarvestFieldsRequestDTO request =
                    new RequiredHarvestFieldsRequestDTO(species.getOfficialCode(), seasonBegin, geoLocation, false);

            final RequiredHarvestFieldsResponseDTO response = getRequiredFields(request);

            assertNotNull(response.getFields());
            assertNotNull(response.getSeason());
            assertNotNull(response.getRhy());
            assertNotNull(response.getMunicipalityName());
            assertNotNull(response.getPropertyIdentifier());
            assertNull(response.getHarvestArea());
            assertNull(response.getSeason().getSpecies());

            assertEquals(HarvestReportingType.SEASON, response.getReportingType());
            assertEquals(RequiredHarvestField.NO, response.getFields().getReport().getPermitNumber());
            assertEquals(harvestSeason.getId(), response.getSeason().getId());
            assertEquals(harvestSeason.getNameLocalisation().asMap(), response.getSeason().getName());
            assertEquals(harvestSeason.getBeginDate(), response.getSeason().getBeginDate());
            assertEquals(harvestSeason.getEndDate(), response.getSeason().getEndDate());
            assertEquals(harvestSeason.getEndOfReportingDate(), response.getSeason().getEndOfReportingDate());
            assertEquals(harvestSeason.getBeginDate2(), response.getSeason().getBeginDate2());
            assertEquals(harvestSeason.getEndDate2(), response.getSeason().getEndDate2());
            assertEquals(harvestSeason.getEndOfReportingDate2(), response.getSeason().getEndOfReportingDate2());
            assertEquals(rhy.getOfficialCode(), response.getRhy().getOfficialCode());
            assertEquals(rhy.getNameFinnish(), response.getRhy().getNameFI());
            assertEquals(rhy.getNameSwedish(), response.getRhy().getNameSV());
            assertEquals(municipality.getNameLocalisation().asMap(), response.getMunicipalityName());
            assertEquals(
                    MockGISQueryService.PROPERTY_QUERY_RESULT.getPropertyIdentifier(),
                    response.getPropertyIdentifier());
        });
    }

    @Test
    public void testGetRequiredHarvestFields_insideSeasonWithQuota() {
        final GameSpecies species = model().newGameSpecies(OFFICIAL_CODE_BEAR);
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final Municipality municipality = model().newMunicipality();
        final GeoLocation geoLocation = new GeoLocation(1, 1);
        final LocalDate seasonBegin = new LocalDate(2017, 8, 1);
        final LocalDate seasonEnd = new LocalDate(2017, 12, 31);
        final HarvestSeason harvestSeason = model().newHarvestSeason(species, seasonBegin, seasonEnd, seasonEnd);
        final HarvestArea harvestArea = model().newHarvestAreaContaining(geoLocation);
        model().newHarvestQuota(harvestSeason, harvestArea, 10);

        onSavedAndAuthenticated(createUserWithPerson(), () -> {

            final RequiredHarvestFieldsRequestDTO request =
                    new RequiredHarvestFieldsRequestDTO(
                            species.getOfficialCode(), seasonBegin, geoLocation, false);

            final RequiredHarvestFieldsResponseDTO response = getRequiredFields(request);

            assertNotNull(response.getFields());
            assertNotNull(response.getSeason());
            assertNotNull(response.getRhy());
            assertNotNull(response.getMunicipalityName());
            assertNotNull(response.getPropertyIdentifier());
            assertNotNull(response.getHarvestArea());
            assertNull(response.getSeason().getSpecies());

            assertEquals(HarvestReportingType.SEASON, response.getReportingType());
            assertEquals(RequiredHarvestField.NO, response.getFields().getReport().getPermitNumber());
            assertEquals(harvestSeason.getId(), response.getSeason().getId());
            assertEquals(harvestSeason.getNameLocalisation().asMap(), response.getSeason().getName());
            assertEquals(harvestSeason.getBeginDate(), response.getSeason().getBeginDate());
            assertEquals(harvestSeason.getEndDate(), response.getSeason().getEndDate());
            assertEquals(harvestSeason.getEndOfReportingDate(), response.getSeason().getEndOfReportingDate());
            assertEquals(harvestSeason.getBeginDate2(), response.getSeason().getBeginDate2());
            assertEquals(harvestSeason.getEndDate2(), response.getSeason().getEndDate2());
            assertEquals(harvestSeason.getEndOfReportingDate2(), response.getSeason().getEndOfReportingDate2());
            assertEquals(harvestArea.getNameFinnish(), response.getHarvestArea().getNameFI());
            assertEquals(harvestArea.getNameSwedish(), response.getHarvestArea().getNameSV());
            assertEquals(rhy.getOfficialCode(), response.getRhy().getOfficialCode());
            assertEquals(rhy.getNameFinnish(), response.getRhy().getNameFI());
            assertEquals(rhy.getNameSwedish(), response.getRhy().getNameSV());
            assertEquals(municipality.getNameLocalisation().asMap(), response.getMunicipalityName());
            assertEquals(
                    MockGISQueryService.PROPERTY_QUERY_RESULT.getPropertyIdentifier(),
                    response.getPropertyIdentifier());
        });
    }

    @Test
    public void testGetRequiredHarvestFields_insideSeasonWithQuota_quotaNotFound() {
        final GameSpecies species = model().newGameSpecies(OFFICIAL_CODE_BEAR);
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final Municipality municipality = model().newMunicipality();
        final GeoLocation geoLocation = new GeoLocation(1, 1);
        final LocalDate seasonBegin = new LocalDate(2017, 8, 1);
        final LocalDate seasonEnd = new LocalDate(2017, 12, 31);
        final HarvestSeason harvestSeason = model().newHarvestSeason(species, seasonBegin, seasonEnd, seasonEnd);

        // Harvest area without RHY
        final HarvestArea harvestArea =
                model().newHarvestArea(HarvestArea.HarvestAreaType.PORONHOITOALUE, "b", "b");

        model().newHarvestQuota(harvestSeason, harvestArea, 10);

        onSavedAndAuthenticated(createUserWithPerson(), () -> {

            final RequiredHarvestFieldsRequestDTO request =
                    new RequiredHarvestFieldsRequestDTO(
                            species.getOfficialCode(), seasonBegin, geoLocation, false);

            final RequiredHarvestFieldsResponseDTO response = getRequiredFields(request);

            assertNotNull(response.getFields());
            assertNull(response.getSeason());
            assertNotNull(response.getRhy());
            assertNotNull(response.getMunicipalityName());
            assertNotNull(response.getPropertyIdentifier());
            assertNull(response.getHarvestArea());

            assertEquals(HarvestReportingType.PERMIT, response.getReportingType());
            assertEquals(RequiredHarvestField.YES, response.getFields().getReport().getPermitNumber());
            assertEquals(rhy.getOfficialCode(), response.getRhy().getOfficialCode());
            assertEquals(rhy.getNameFinnish(), response.getRhy().getNameFI());
            assertEquals(rhy.getNameSwedish(), response.getRhy().getNameSV());
            assertEquals(municipality.getNameLocalisation().asMap(), response.getMunicipalityName());
            assertEquals(
                    MockGISQueryService.PROPERTY_QUERY_RESULT.getPropertyIdentifier(),
                    response.getPropertyIdentifier());
        });
    }

    @Test
    public void testGetRequiredHarvestFields_withPermit() {
        final GameSpecies species = model().newGameSpecies();
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final Municipality municipality = model().newMunicipality();
        final GeoLocation geoLocation = new GeoLocation(1, 1);

        onSavedAndAuthenticated(createUserWithPerson(), () -> {

            final RequiredHarvestFieldsRequestDTO request =
                    new RequiredHarvestFieldsRequestDTO(species.getOfficialCode(), today(), geoLocation, true);

            final RequiredHarvestFieldsResponseDTO response = getRequiredFields(request);

            assertNotNull(response.getFields());
            assertNull(response.getSeason());
            assertNotNull(response.getRhy());
            assertNotNull(response.getMunicipalityName());
            assertNotNull(response.getPropertyIdentifier());
            assertNull(response.getHarvestArea());

            assertEquals(HarvestReportingType.PERMIT, response.getReportingType());
            assertEquals(RequiredHarvestField.YES, response.getFields().getReport().getPermitNumber());
            assertEquals(rhy.getOfficialCode(), response.getRhy().getOfficialCode());
            assertEquals(rhy.getNameFinnish(), response.getRhy().getNameFI());
            assertEquals(rhy.getNameSwedish(), response.getRhy().getNameSV());
            assertEquals(municipality.getNameLocalisation().asMap(), response.getMunicipalityName());
            assertEquals(
                    MockGISQueryService.PROPERTY_QUERY_RESULT.getPropertyIdentifier(),
                    response.getPropertyIdentifier());
        });
    }

    @Test
    public void testGetRequiredHarvestFields_seasonNotFound_freeHuntingAllowed() {
        final GameSpecies species = model().newGameSpecies(GameSpecies.OFFICIAL_CODE_MOOSE);
        model().newRiistanhoitoyhdistys();
        model().newMunicipality();
        final GeoLocation geoLocation = new GeoLocation(1, 1);

        onSavedAndAuthenticated(createUserWithPerson(), () -> {

            final RequiredHarvestFieldsRequestDTO request =
                    new RequiredHarvestFieldsRequestDTO(species.getOfficialCode(), today(), geoLocation, false);

            final RequiredHarvestFieldsResponseDTO response = getRequiredFields(request);

            assertNotNull(response.getFields());
            assertNull(response.getSeason());
            assertNull(response.getRhy());
            assertNull(response.getMunicipalityName());
            assertNull(response.getPropertyIdentifier());
            assertNull(response.getHarvestArea());

            assertEquals(HarvestReportingType.BASIC, response.getReportingType());
            assertEquals(RequiredHarvestField.NO, response.getFields().getReport().getPermitNumber());
        });
    }

    @Test
    public void testGetRequiredHarvestFields_seasonNotFound_permitRequired() {
        final GameSpecies species = model().newGameSpecies(OFFICIAL_CODE_BEAR);
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final Municipality municipality = model().newMunicipality();
        final GeoLocation geoLocation = new GeoLocation(1, 1);

        onSavedAndAuthenticated(createUserWithPerson(), () -> {

            final RequiredHarvestFieldsRequestDTO request =
                    new RequiredHarvestFieldsRequestDTO(species.getOfficialCode(), today(), geoLocation, false);

            final RequiredHarvestFieldsResponseDTO response = getRequiredFields(request);

            assertNotNull(response.getFields());
            assertNull(response.getSeason());
            assertNotNull(response.getRhy());
            assertNotNull(response.getMunicipalityName());
            assertNotNull(response.getPropertyIdentifier());
            assertNull(response.getHarvestArea());

            assertEquals(HarvestReportingType.PERMIT, response.getReportingType());
            assertEquals(RequiredHarvestField.YES, response.getFields().getReport().getPermitNumber());
            assertEquals(rhy.getOfficialCode(), response.getRhy().getOfficialCode());
            assertEquals(rhy.getNameFinnish(), response.getRhy().getNameFI());
            assertEquals(rhy.getNameSwedish(), response.getRhy().getNameSV());
            assertEquals(municipality.getNameLocalisation().asMap(), response.getMunicipalityName());
            assertEquals(
                    MockGISQueryService.PROPERTY_QUERY_RESULT.getPropertyIdentifier(),
                    response.getPropertyIdentifier());
        });
    }

    @Test
    public void testGetRequiredHarvestFields_rhyNotFound() {
        final GameSpecies species = model().newGameSpecies();
        final GeoLocation geoLocation = MockGISQueryService.RHY_GEOLOCATION_NOT_FOUND;

        onSavedAndAuthenticated(createUserWithPerson(), () -> {

            final RequiredHarvestFieldsRequestDTO request =
                    new RequiredHarvestFieldsRequestDTO(species.getOfficialCode(), today(), geoLocation, false);

            final RequiredHarvestFieldsResponseDTO response = getRequiredFields(request);

            assertNotNull(response.getFields());
            assertNull(response.getSeason());
            assertNull(response.getRhy());
            assertNull(response.getMunicipalityName());
            assertNull(response.getPropertyIdentifier());
            assertNull(response.getHarvestArea());

            assertEquals(HarvestReportingType.BASIC, response.getReportingType());
            assertEquals(RequiredHarvestField.NO, response.getFields().getReport().getPermitNumber());
        });
    }

    @Test
    public void testGetRequiredHarvestFields_withPermit_withPersonId() {
        withDeerHuntingGroupFixture(fixt -> {

            onSavedAndAuthenticated(createNewModerator(), () -> {

                final RequiredHarvestFieldsRequestDTO request =
                        new RequiredHarvestFieldsRequestDTO(
                                fixt.species.getOfficialCode(), today(), fixt.zoneCentroid, true);

                final RequiredHarvestFieldsResponseDTO response = getRequiredFields(request);

                assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ADULT_MALE,
                        response.getFields().getSpecimen().getAntlersLost());
            });
        });
    }

    @Ignore
    @Test
    public void testGetRequiredHarvestFields_mooselike() {

    }

    private RequiredHarvestFieldsResponseDTO getRequiredFields(final RequiredHarvestFieldsRequestDTO request) {
        return feature.getRequiredHarvestFields(request, CURRENTLY_SUPPORTED);
    }
}
