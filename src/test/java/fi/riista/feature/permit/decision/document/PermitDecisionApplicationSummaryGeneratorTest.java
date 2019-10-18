package fi.riista.feature.permit.decision.document;

import com.google.common.collect.ImmutableSet;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.gis.zone.GISZoneSizeDTO;
import fi.riista.feature.gis.zone.TotalLandWaterSizeDTO;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.area.rhy.HarvestPermitAreaRhy;
import fi.riista.test.DefaultEntitySupplierProvider;
import fi.riista.util.Locales;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;

public class PermitDecisionApplicationSummaryGeneratorTest implements DefaultEntitySupplierProvider {

    @Test
    public void testGenerateAllFieldsFinnish() {
        final GISZoneSizeDTO areaSize = createAreaSize();
        final HarvestPermitApplication application = createApplication(areaSize);
        final String generate = PermitDecisionApplicationSummaryGenerator.generate(Locales.FI, application, areaSize);

        assertEquals("Hakija on hakenut hirvieläinten pyyntilupaa seuraavasti:\n" +
                "\n" +
                "Eläinlaji ja lupamäärä:\n" +
                "\n" +
                "---|---:\n" +
                "Hirvi|20 kpl\n" +
                "Valkohäntäpeura|700 kpl\n\n" +
                "Alue:\n" +
                "Hakemus sijaitsee seuraavien riistanhoitoyhdistysten alueilla:\n" +
                "Puolangan riistanhoitoyhdistys, karttaan rajattu alue\n" +
                "Suomussalmen riistanhoitoyhdistys, karttaan rajattu alue\n" +
                "\n" +
                "Alueen tunnus 2RYF88RTHWMO\n" +
                "Alueen pinta-ala:\n" +
                "\n" +
                "---|---:\n" +
                "Maapinta-ala|3163 ha\n" +
                "Vesipinta-ala|2 ha\n" +
                "Kokonaispinta-ala|3165 ha\n" +
                "Valtionmaiden maapinta-ala|114 ha\n" +
                "Yksityismaiden maapinta-ala|3050 ha\n" +
                "\n" +
                "---:|---\n" +
                "Lupaosakkaiden määrä,|2 kpl\n" +
                "asiakasnumerot ja nimet|\n" +
                "5000377|Lassen hirviporukka\n" +
                "6000890|Jennin seurue\n" +
                "\n" +
                "---|---:\n" +
                "Ampujat, jotka eivät kuulu muuhun pyyntilupaa hakevaan seuraan / seurueeseen|111 kpl\n" +
                "Ampujat, jotka kuuluvat muuhun hirveä metsästävään seuraan / seurueeseen, mutta eivät metsästä siellä tulevana metsästyskautena|333 kpl\n" +
                "Ampujat, jotka kuuluvat muuhun hirveä metsästävään seuraan / seurueeseen, ja metsästävät siellä tulevana metsästyskautena|222 kpl\n" +
                "\n" +
                "---|---:\n" +
                "Metsähallituksen aluelupia|0 kpl\n" +
                "Ampujaluetteloita|0 kpl\n" +
                "Muita liitteitä|0 kpl", generate);
    }

    @Test
    public void testGenerateWithFreeHuntingFalseFinnish() {
        final GISZoneSizeDTO areaSize = createAreaSize();
        final HarvestPermitApplication application = createApplication(areaSize);

        application.getArea().setFreeHunting(false);

        final String generate = PermitDecisionApplicationSummaryGenerator.generate(Locales.FI, application, areaSize);

        assertEquals("Hakija on hakenut hirvieläinten pyyntilupaa seuraavasti:\n" +
                "\n" +
                "Eläinlaji ja lupamäärä:\n" +
                "\n" +
                "---|---:\n" +
                "Hirvi|20 kpl\n" +
                "Valkohäntäpeura|700 kpl\n\n" +
                "Alue:\n" +
                "Hakemus sijaitsee seuraavien riistanhoitoyhdistysten alueilla:\n" +
                "Puolangan riistanhoitoyhdistys, karttaan rajattu alue\n" +
                "Suomussalmen riistanhoitoyhdistys, karttaan rajattu alue\n" +
                "\n" +
                "Alueen tunnus 2RYF88RTHWMO\n" +
                "Alueen pinta-ala:\n" +
                "\n" +
                "---|---:\n" +
                "Maapinta-ala|3163 ha\n" +
                "Vesipinta-ala|2 ha\n" +
                "Kokonaispinta-ala|3165 ha\n" +
                "Valtionmaiden maapinta-ala|114 ha\n" +
                "Yksityismaiden maapinta-ala|3050 ha\n" +
                "\n" +
                "---:|---\n" +
                "Lupaosakkaiden määrä,|2 kpl\n" +
                "asiakasnumerot ja nimet|\n" +
                "5000377|Lassen hirviporukka\n" +
                "6000890|Jennin seurue\n" +
                "\n" +
                "---|---:\n" +
                "Muita liitteitä|0 kpl", generate);
    }

    @Test
    public void testGenerateAllFieldsSwedish() {
        final GISZoneSizeDTO areaSize = createAreaSize();
        final HarvestPermitApplication application = createApplication(areaSize);
        final String generate = PermitDecisionApplicationSummaryGenerator.generate(Locales.SV, application, areaSize);

        assertEquals("Den sökande har ansökt om jaktlicens för hjortdjur enligt följande:\n" +
                "\n" +
                "Djurart och licensantal:\n" +
                "\n" +
                "---|---:\n" +
                "Älg|20 st.\n" +
                "Vitsvanshjort|700 st.\n\n" +
                "Område:\n" +
                "Ansökningsområdet är på följande jaktvårdsföreningars områden:\n" +
                "Puolangan jaktvårdsförening, på kartan avgränsat område\n" +
                "Suomussalmen jaktvårdsförening, på kartan avgränsat område\n" +
                "\n" +
                "Områdets kod 2RYF88RTHWMO\n" +
                "Areal:\n" +
                "\n" +
                "---|---:\n" +
                "Markareal|3163 ha\n" +
                "Vattenareal|2 ha\n" +
                "Areal|3165 ha\n" +
                "Statsägda områdens areal|114 ha\n" +
                "Privatägda områdens areal|3050 ha\n" +
                "\n" +
                "---:|---\n" +
                "Antal licensdelägare,|2 st.\n" +
                "kundnummer och namn|\n" +
                "5000377|Lassen hirviporukka\n" +
                "6000890|Jennin seurue\n" +
                "\n" +
                "---|---:\n" +
                "Skyttar som inte hör till annan förening / annat sällskap som ansöker om jaktlicens|111 st.\n" +
                "Skyttar som hör till en annan förening / annat sällskap som jagar älg men som inte jagar där under den aktuella jaktsäsongen|333 st.\n" +
                "Skyttar som hör till en annan förening / annat sällskap som jagar älg och som jagar där under den aktuella jaktsäsongen|222 st.\n" +
                "\n" +
                "---|---:\n" +
                "Områdeslicens JL|0 st.\n" +
                "Skytteförteckningar|0 st.\n" +
                "Övriga bilagor|0 st.", generate);
    }

    @Test
    public void testGenerateWithFreeHuntingFalseSwedish() {
        final GISZoneSizeDTO areaSize = createAreaSize();
        final HarvestPermitApplication application = createApplication(areaSize);

        application.getArea().setFreeHunting(false);

        final String generate = PermitDecisionApplicationSummaryGenerator.generate(Locales.SV, application, areaSize);

        assertEquals("Den sökande har ansökt om jaktlicens för hjortdjur enligt följande:\n" +
                "\n" +
                "Djurart och licensantal:\n" +
                "\n" +
                "---|---:\n" +
                "Älg|20 st.\n" +
                "Vitsvanshjort|700 st.\n\n" +
                "Område:\n" +
                "Ansökningsområdet är på följande jaktvårdsföreningars områden:\n" +
                "Puolangan jaktvårdsförening, på kartan avgränsat område\n" +
                "Suomussalmen jaktvårdsförening, på kartan avgränsat område\n" +
                "\n" +
                "Områdets kod 2RYF88RTHWMO\n" +
                "Areal:\n" +
                "\n" +
                "---|---:\n" +
                "Markareal|3163 ha\n" +
                "Vattenareal|2 ha\n" +
                "Areal|3165 ha\n" +
                "Statsägda områdens areal|114 ha\n" +
                "Privatägda områdens areal|3050 ha\n" +
                "\n" +
                "---:|---\n" +
                "Antal licensdelägare,|2 st.\n" +
                "kundnummer och namn|\n" +
                "5000377|Lassen hirviporukka\n" +
                "6000890|Jennin seurue\n" +
                "\n" +
                "---|---:\n" +
                "Övriga bilagor|0 st.", generate);
    }

    @Nonnull
    private static GISZoneSizeDTO createAreaSize() {
        return new GISZoneSizeDTO(new TotalLandWaterSizeDTO(3165.0 * 10_000, 3163 * 10_000, 2 * 10_000),
                114.0 * 10_000, 3050.0 * 10_000);
    }

    @Nonnull
    private HarvestPermitApplication createApplication(final GISZoneSizeDTO areaSize) {
        final GISZone zone = getEntitySupplier().newGISZone();
        zone.setComputedAreaSize(areaSize.getAll().getTotal());
        zone.setWaterAreaSize(areaSize.getAll().getWater());
        zone.setStateLandAreaSize(areaSize.getStateLandAreaSize());
        zone.setPrivateLandAreaSize(areaSize.getPrivateLandAreaSize());

        final Riistanhoitoyhdistys rhy1 = getEntitySupplier().newRiistanhoitoyhdistys();
        rhy1.setNameFinnish("Puolangan riistanhoitoyhdistys");
        rhy1.setNameSwedish("Puolangan jaktvårdsförening");
        final Riistanhoitoyhdistys rhy2 = getEntitySupplier().newRiistanhoitoyhdistys();
        rhy2.setNameFinnish("Suomussalmen riistanhoitoyhdistys");
        rhy2.setNameSwedish("Suomussalmen jaktvårdsförening");

        final HarvestPermitArea permitArea = new HarvestPermitArea();
        permitArea.setZone(zone);
        permitArea.setExternalId("2RYF88RTHWMO");
        permitArea.setFreeHunting(true);

        permitArea.getRhy().add(new HarvestPermitAreaRhy(permitArea, rhy1,
                new TotalLandWaterSizeDTO(1, 2, 3),
                new TotalLandWaterSizeDTO(2, 3, 4),
                new TotalLandWaterSizeDTO(3, 4, 5)));

        permitArea.getRhy().add(new HarvestPermitAreaRhy(permitArea, rhy2,
                new TotalLandWaterSizeDTO(1, 2, 3),
                new TotalLandWaterSizeDTO(2, 3, 4),
                new TotalLandWaterSizeDTO(3, 4, 5)));

        final HuntingClub club1 = getEntitySupplier().newHuntingClub(rhy1);
        club1.setOfficialCode("5000377");
        club1.setNameFinnish("Lassen hirviporukka");
        club1.setNameSwedish("Lassen hirviporukka");

        final HuntingClub club2 = getEntitySupplier().newHuntingClub(rhy2);
        club2.setOfficialCode("6000890");
        club2.setNameFinnish("Jennin seurue");
        club2.setNameSwedish("Jennin seurue");

        final HarvestPermitApplicationSpeciesAmount spaMoose = new HarvestPermitApplicationSpeciesAmount();
        spaMoose.setGameSpecies(getEntitySupplier().newGameSpeciesMoose());
        spaMoose.setAmount(20);

        final GameSpecies deer = getEntitySupplier().newGameSpecies();
        deer.setNameFinnish("valkohäntäpeura");
        deer.setNameSwedish("vitsvanshjort");
        final HarvestPermitApplicationSpeciesAmount spaDeer = new HarvestPermitApplicationSpeciesAmount();
        spaDeer.setGameSpecies(deer);
        spaDeer.setAmount(700);

        final HuntingClub permitHolder = getEntitySupplier().newHuntingClub();

        final HarvestPermitApplication application = new HarvestPermitApplication();
        application.setArea(permitArea);
        application.setHuntingClubAndPermitHolder(permitHolder);
        application.setPermitPartners(ImmutableSet.of(club1, club2));
        application.setRhy(rhy1);
        application.setRelatedRhys(new HashSet<>());

        application.setShooterOnlyClub(111);
        application.setShooterOtherClubActive(222);
        application.setShooterOtherClubPassive(333);

        application.getSpeciesAmounts().add(spaMoose);
        application.getSpeciesAmounts().add(spaDeer);
        return application;
    }
}
