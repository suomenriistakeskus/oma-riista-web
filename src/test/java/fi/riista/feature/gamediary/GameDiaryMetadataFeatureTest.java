package fi.riista.feature.gamediary;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.Municipality;
import fi.riista.feature.common.entity.Required;
import fi.riista.feature.gamediary.harvest.HarvestReportingType;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestFieldsQuery;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestFieldsQueryResponse;
import fi.riista.feature.gis.MockGISQueryService;
import fi.riista.feature.harvestpermit.season.HarvestArea;
import fi.riista.feature.harvestpermit.season.HarvestQuota;
import fi.riista.feature.harvestpermit.season.HarvestSeason;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDate;
import org.junit.Test;

import javax.annotation.Resource;

import static java.util.Collections.emptySet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class GameDiaryMetadataFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private GameDiaryMetadataFeature gameDiaryMetadataFeature;

    @Test
    public void testGetHarvestFields_InsideSeason() {
        final GameSpecies species = model().newGameSpecies();
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final Municipality municipality = model().newMunicipality();
        final GeoLocation geoLocation = new GeoLocation(1, 1);
        final LocalDate seasonBegin = new LocalDate(2017, 8, 1);
        final LocalDate seasonEnd = new LocalDate(2017, 12, 31);
        final HarvestSeason harvestSeason = model().newHarvestSeason(species, seasonBegin, seasonEnd, seasonEnd);

        final RequiredHarvestFieldsQuery query = new RequiredHarvestFieldsQuery(
                species.getOfficialCode(), seasonBegin, geoLocation, false);

        persistInNewTransaction();

        final RequiredHarvestFieldsQueryResponse response = gameDiaryMetadataFeature.getHarvestFields(query);

        assertNotNull(response.getFields());
        assertNotNull(response.getSeason());
        assertNotNull(response.getRhy());
        assertNotNull(response.getMunicipalityName());
        assertNotNull(response.getPropertyIdentifier());
        assertNull(response.getHarvestArea());
        assertNull(response.getSeason().getSpecies());

        assertEquals(HarvestReportingType.SEASON, response.getReportingType());
        assertEquals(Required.NO, response.getFields().getPermitNumber());
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
        assertEquals(MockGISQueryService.PROPERTY_QUERY_RESULT.getPropertyIdentifier(), response.getPropertyIdentifier());
    }

    @Test
    public void testGetHarvestFields_InsideSeasonWithQuota() {
        final GameSpecies species = model().newGameSpecies();
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final Municipality municipality = model().newMunicipality();
        final GeoLocation geoLocation = new GeoLocation(1, 1);
        final LocalDate seasonBegin = new LocalDate(2017, 8, 1);
        final LocalDate seasonEnd = new LocalDate(2017, 12, 31);
        final HarvestSeason harvestSeason = model().newHarvestSeason(species, seasonBegin, seasonEnd, seasonEnd);
        final HarvestArea harvestArea = model().newHarvestArea(rhy);
        final HarvestQuota harvestQuota = model().newHarvestQuota(harvestSeason, harvestArea, 10);

        final RequiredHarvestFieldsQuery query = new RequiredHarvestFieldsQuery(
                species.getOfficialCode(), seasonBegin, geoLocation, false);

        persistInNewTransaction();

        final RequiredHarvestFieldsQueryResponse response = gameDiaryMetadataFeature.getHarvestFields(query);

        assertNotNull(response.getFields());
        assertNotNull(response.getSeason());
        assertNotNull(response.getRhy());
        assertNotNull(response.getMunicipalityName());
        assertNotNull(response.getPropertyIdentifier());
        assertNotNull(response.getHarvestArea());
        assertNull(response.getSeason().getSpecies());

        assertEquals(HarvestReportingType.SEASON, response.getReportingType());
        assertEquals(Required.NO, response.getFields().getPermitNumber());
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
        assertEquals(MockGISQueryService.PROPERTY_QUERY_RESULT.getPropertyIdentifier(), response.getPropertyIdentifier());
    }

    @Test
    public void testGetHarvestFields_InsideSeasonWithQuota_QuotaNotFound() {
        final GameSpecies species = model().newGameSpecies(GameSpecies.OFFICIAL_CODE_BEAR);
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final Municipality municipality = model().newMunicipality();
        final GeoLocation geoLocation = new GeoLocation(1, 1);
        final LocalDate seasonBegin = new LocalDate(2017, 8, 1);
        final LocalDate seasonEnd = new LocalDate(2017, 12, 31);
        final HarvestSeason harvestSeason = model().newHarvestSeason(species, seasonBegin, seasonEnd, seasonEnd);
        // Harvest area without RHY
        final HarvestArea harvestArea = model().newHarvestArea(HarvestArea.HarvestAreaType.PORONHOITOALUE, "b", "b", emptySet());
        final HarvestQuota harvestQuota = model().newHarvestQuota(harvestSeason, harvestArea, 10);

        final RequiredHarvestFieldsQuery query = new RequiredHarvestFieldsQuery(
                species.getOfficialCode(), seasonBegin, geoLocation, false);

        persistInNewTransaction();

        final RequiredHarvestFieldsQueryResponse response = gameDiaryMetadataFeature.getHarvestFields(query);

        assertNotNull(response.getFields());
        assertNull(response.getSeason());
        assertNotNull(response.getRhy());
        assertNotNull(response.getMunicipalityName());
        assertNotNull(response.getPropertyIdentifier());
        assertNull(response.getHarvestArea());

        assertEquals(HarvestReportingType.PERMIT, response.getReportingType());
        assertEquals(Required.YES, response.getFields().getPermitNumber());
        assertEquals(rhy.getOfficialCode(), response.getRhy().getOfficialCode());
        assertEquals(rhy.getNameFinnish(), response.getRhy().getNameFI());
        assertEquals(rhy.getNameSwedish(), response.getRhy().getNameSV());
        assertEquals(municipality.getNameLocalisation().asMap(), response.getMunicipalityName());
        assertEquals(MockGISQueryService.PROPERTY_QUERY_RESULT.getPropertyIdentifier(), response.getPropertyIdentifier());
    }

    @Test
    public void testGetHarvestFields_WithPermit() {
        final GameSpecies species = model().newGameSpecies();
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final Municipality municipality = model().newMunicipality();
        final GeoLocation geoLocation = new GeoLocation(1, 1);

        final RequiredHarvestFieldsQuery query = new RequiredHarvestFieldsQuery(
                species.getOfficialCode(), DateUtil.today(), geoLocation, true);

        persistInNewTransaction();

        final RequiredHarvestFieldsQueryResponse response = gameDiaryMetadataFeature.getHarvestFields(query);

        assertNotNull(response.getFields());
        assertNull(response.getSeason());
        assertNotNull(response.getRhy());
        assertNotNull(response.getMunicipalityName());
        assertNotNull(response.getPropertyIdentifier());
        assertNull(response.getHarvestArea());

        assertEquals(HarvestReportingType.PERMIT, response.getReportingType());
        assertEquals(Required.YES, response.getFields().getPermitNumber());
        assertEquals(rhy.getOfficialCode(), response.getRhy().getOfficialCode());
        assertEquals(rhy.getNameFinnish(), response.getRhy().getNameFI());
        assertEquals(rhy.getNameSwedish(), response.getRhy().getNameSV());
        assertEquals(municipality.getNameLocalisation().asMap(), response.getMunicipalityName());
        assertEquals(MockGISQueryService.PROPERTY_QUERY_RESULT.getPropertyIdentifier(), response.getPropertyIdentifier());
    }

    @Test
    public void testGetHarvestFields_SeasonNotFound_FreeHuntingAllowed() {
        final GameSpecies species = model().newGameSpecies(GameSpecies.OFFICIAL_CODE_MOOSE);
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final Municipality municipality = model().newMunicipality();
        final GeoLocation geoLocation = new GeoLocation(1, 1);

        final RequiredHarvestFieldsQuery query = new RequiredHarvestFieldsQuery(
                species.getOfficialCode(), DateUtil.today(), geoLocation, false);

        persistInNewTransaction();

        final RequiredHarvestFieldsQueryResponse response = gameDiaryMetadataFeature.getHarvestFields(query);

        assertNotNull(response.getFields());
        assertNull(response.getSeason());
        assertNull(response.getRhy());
        assertNull(response.getMunicipalityName());
        assertNull(response.getPropertyIdentifier());
        assertNull(response.getHarvestArea());

        assertEquals(HarvestReportingType.BASIC, response.getReportingType());
        assertEquals(Required.NO, response.getFields().getPermitNumber());
    }

    @Test
    public void testGetHarvestFields_SeasonNotFound_PermitRequired() {
        final GameSpecies species = model().newGameSpecies(GameSpecies.OFFICIAL_CODE_BEAR);
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final Municipality municipality = model().newMunicipality();
        final GeoLocation geoLocation = new GeoLocation(1, 1);

        final RequiredHarvestFieldsQuery query = new RequiredHarvestFieldsQuery(
                species.getOfficialCode(), DateUtil.today(), geoLocation, false);

        persistInNewTransaction();

        final RequiredHarvestFieldsQueryResponse response = gameDiaryMetadataFeature.getHarvestFields(query);

        assertNotNull(response.getFields());
        assertNull(response.getSeason());
        assertNotNull(response.getRhy());
        assertNotNull(response.getMunicipalityName());
        assertNotNull(response.getPropertyIdentifier());
        assertNull(response.getHarvestArea());

        assertEquals(HarvestReportingType.PERMIT, response.getReportingType());
        assertEquals(Required.YES, response.getFields().getPermitNumber());
        assertEquals(rhy.getOfficialCode(), response.getRhy().getOfficialCode());
        assertEquals(rhy.getNameFinnish(), response.getRhy().getNameFI());
        assertEquals(rhy.getNameSwedish(), response.getRhy().getNameSV());
        assertEquals(municipality.getNameLocalisation().asMap(), response.getMunicipalityName());
        assertEquals(MockGISQueryService.PROPERTY_QUERY_RESULT.getPropertyIdentifier(), response.getPropertyIdentifier());
    }

    @Test
    public void testGetHarvestFields_RhyNotFound() {
        final GameSpecies species = model().newGameSpecies();
        final GeoLocation geoLocation = MockGISQueryService.RHY_GEOLOCATION_NOT_FOUND;

        final RequiredHarvestFieldsQuery query = new RequiredHarvestFieldsQuery(
                species.getOfficialCode(), DateUtil.today(), geoLocation, false);

        persistInNewTransaction();

        final RequiredHarvestFieldsQueryResponse response = gameDiaryMetadataFeature.getHarvestFields(query);

        assertNotNull(response.getFields());
        assertNull(response.getSeason());
        assertNull(response.getRhy());
        assertNull(response.getMunicipalityName());
        assertNull(response.getPropertyIdentifier());
        assertNull(response.getHarvestArea());

        assertEquals(HarvestReportingType.BASIC, response.getReportingType());
        assertEquals(Required.NO, response.getFields().getPermitNumber());
    }

    @Test
    public void testGetHarvestFields_Mooselike() {

    }
}
