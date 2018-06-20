package fi.riista.feature.huntingclub.moosedatacard.exception;

import static fi.riista.util.DateUtil.DATE_FORMAT_FINNISH;
import static java.lang.String.format;

import fi.riista.feature.common.entity.Has2BeginEndDates;
import fi.riista.feature.huntingclub.moosedatacard.DateAndLocation;
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardHarvest;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardLargeCarnivoreObservation;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseCalf;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseFemale;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseMale;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardObservation;
import fi.riista.feature.huntingclub.moosedatacard.validation.NumericFieldMeta;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

public final class MooseDataCardImportFailureReasons {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern(DATE_FORMAT_FINNISH);

    // Prefer more granular error messages instead of this for debugging purposes.
    @Nonnull
    public static String internalServerError() {
        return "Hirvitietokortin sisäänluku epäonnistui.";
    }

    @Nonnull
    public static String invalidFilename(@Nonnull final String filename, @Nonnull final String fileType) {
        Objects.requireNonNull(filename, "filename is null");
        Objects.requireNonNull(fileType, "fileType is null");

        return format(
                "Hirvitietokortin sisäänluvussa %s-tiedoston nimi on virheellisen muotoinen: %s", fileType, filename);
    }

    @Nonnull
    public static String invalidTimestampInXmlFileName(@Nonnull final String timestamp) {
        Objects.requireNonNull(timestamp);
        return "Virheellinen aikaleima tiedostonimessä: " + timestamp;
    }

    @Nonnull
    public static String basenameMismatchBetweenXmlAndPdfFile() {
        return "XML- ja PDF-tiedoston nimet ilman tiedostopäätettä eivät vastaa toisiaan.";
    }

    @Nonnull
    public static String parsingXmlFileOfMooseDataCardFailed(@Nonnull final String xmlFileName) {
        Objects.requireNonNull(xmlFileName);
        return "Virhe luettaessa XML-tiedostoa: " + xmlFileName;
    }

    @Nonnull
    public static String permitNumberMismatchBetweenNameAndContentOfXmlFile(@Nonnull final String permitNumber) {
        Objects.requireNonNull(permitNumber);
        return "XML-tiedoston sisältämä lupanumero ei täsmää tiedoston nimessä olevan kanssa: " + permitNumber;
    }

    @Nonnull
    public static String huntingClubCodeMismatchBetweenNameAndContentOfXmlFile(@Nonnull final String clubCode) {
        Objects.requireNonNull(clubCode);
        return "XML-tiedoston sisältämä seuran asiakasnumero ei täsmää tiedoston nimessä olevan kanssa: " + clubCode;
    }

    @Nonnull
    public static String missingPermitNumber() {
        return "Lupanumeroa ei ole annettu";
    }

    @Nonnull
    public static String invalidPermitNumber(@Nonnull final String permitNumber) {
        Objects.requireNonNull(permitNumber);
        return "Virheellisen muotoinen lupanumero: " + permitNumber;
    }

    @Nonnull
    public static String missingClubCode() {
        return "Seuran asiakasnumeroa ei ole annettu";
    }

    @Nonnull
    public static String invalidClubCode(@Nonnull final String clubCode) {
        Objects.requireNonNull(clubCode);
        return "Virheellisen muotoinen seuran asiakasnumero: " + clubCode;
    }

    @Nonnull
    public static String missingClubCoordinates() {
        return "Seuran koordinaatteja ei ole annettu";
    }

    @Nonnull
    public static String invalidClubCoordinates(@Nonnull final String coordinates) {
        Objects.requireNonNull(coordinates);
        return "Seuran koordinaatit on annettu virheellisessä muodossa: " + coordinates;
    }

    @Nonnull
    public static String clubCoordinatesOutOfFinland(final long latitude, final long longitude) {
        return String.format(
                "Seuran koordinaatit eivät ole Suomen rajojen sisäpuolella: P %d I %d", latitude, longitude);
    }

    @Nonnull
    public static String invalidHunterNumber(@Nonnull final String hunterNumber) {
        Objects.requireNonNull(hunterNumber);
        return "Hirvitietokortin yhteyshenkilön metsästäjänumero on virheellisen muotoinen: " + hunterNumber;
    }

    @Nonnull
    public static String invalidSsn() {
        return "Hirvitietokortin yhteyshenkilön henkilötunnus on virheellisen muotoinen";
    }

    @Nonnull
    public static String hunterNumberAndSsnMissingForContactPerson() {
        return "Hirvitietokortin yhteyshenkilölle ei ole annettu metsästäjänumeroa eikä henkilötunnusta";
    }

    @Nonnull
    public static String permitNotFoundByPermitNumber(@Nonnull final String permitNumber) {
        Objects.requireNonNull(permitNumber);
        return "Pyyntilupaa ei löytynyt annetulla lupanumerolla: " + permitNumber;
    }

    @Nonnull
    public static String permitNotOfCorrectType(@Nonnull final String permitType) {
        Objects.requireNonNull(permitType);
        return "Pyyntiluvan tyyppi ei sovellu hirvitietokortteihin: " + permitType;
    }

    @Nonnull
    public static String huntingFinishedForPermit(@Nonnull final String permitNumber) {
        Objects.requireNonNull(permitNumber);
        return "Kaikki osakkaat ovat päättäneet metsästyksen, ja metsästys on päätetty luvalle: " + permitNumber;
    }

    @Nonnull
    public static String huntingClubNotFoundByCustomerNumber(@Nonnull final String customerNumber) {
        Objects.requireNonNull(customerNumber);
        return "Seuraa ei löytynyt annetulla asiakasnumerolla: " + customerNumber;
    }

    @Nonnull
    public static String harvestPermitSpeciesAmountForMooseNotFound() {
        return "Pyyntiluvalta puuttuvat hirvilajikohtaiset tiedot";
    }

    @Nonnull
    public static String multipleHarvestPermitMooseAmountsFound() {
        return "Pyyntiluvalle löytyi useita hirvilajikohtaisia ajanjakso-lupamäärä-tietoja, automaattinen valinta ei onnistu";
    }

    @Nonnull
    public static String huntingYearForHarvestPermitCouldNotBeUnambiguouslyResolved(
            @Nonnull final String permitNumber) {

        Objects.requireNonNull(permitNumber);
        return "Pyyntiluvan metsästysvuotta ei pystytty johtamaan yksikäsitteisesti: " + permitNumber;
    }

    @Nonnull
    public static String clubHuntingFinishedByModeratorOverride(@Nonnull final String customerNumber) {
        Objects.requireNonNull(customerNumber);
        return String.format(
                "Moderaattori on päättänyt lupametsästyksen seuralle, jonka asiakasnumero on: %s", customerNumber);
    }

    @Nonnull
    public static String contactPersonCouldNotBeFoundByHunterNumberOrSsn(
            @Nullable final String hunterNumber, @Nullable final String ssn) {

        return hunterNumber != null
                ? "Hirvitietokortin yhteyshenkilöä ei löytynyt annetun metsästäjänumeron perusteella: " + hunterNumber
                : "Hirvitietokortin yhteyshenkilöä ei löytynyt annetun henkilötunnuksen perusteella: " + ssn;
    }

    @Nonnull
    public static <N extends Number & Comparable<N>> String numericFieldNotInValidRange(
            @Nonnull final NumericFieldMeta<?, N> fieldMeta, @Nullable final N value) {

        Objects.requireNonNull(fieldMeta, "fieldMeta is null");

        return String.format("%s ei ole sallitulla välillä %s: %s",
                fieldMeta.getNameFinnish(),
                fieldMeta.getRange().toString(),
                value != null ? value.toString() : null);
    }

    @Nonnull
    public static String huntingDayValidationError(@Nullable final LocalDate huntingDay, @Nonnull final String errMsg) {
        Objects.requireNonNull(errMsg, "errMsg is null");

        return String.format("Metsästyspäivä %s - %s",
                huntingDay != null ? DATE_FORMATTER.print(huntingDay) : "<päivämäärää ei annettu>", errMsg);
    }

    @Nonnull
    public static String huntingDayAppearsMoreThanOnce(@Nonnull final LocalDate startDate) {
        Objects.requireNonNull(startDate);

        return huntingDayValidationError(startDate, "duplikaatti päivämäärä (esiintyy moneen kertaan)");
    }

    @Nonnull
    public static String diaryEntryMissingDate(@Nonnull final DateAndLocation entry) {
        return String.format("%s - päivämäärää ei ole annettu", getPrefix(entry));
    }

    @Nonnull
    public static String diaryEntryValidationError(@Nonnull final DateAndLocation entry, @Nonnull final String errMsg) {
        Objects.requireNonNull(errMsg, "errMsg is null");

        final String dateAsString = Optional.ofNullable(entry.getDate())
                .map(DATE_FORMATTER::print)
                .map(" "::concat)
                .orElseGet(String::new);

        return String.format("%s%s - %s", getPrefix(entry), dateAsString, errMsg);
    }

    @Nonnull
    public static String harvestDateNotWithinPermittedSeason(
            @Nonnull final MooseDataCardHarvest harvest, @Nonnull final Has2BeginEndDates permitSeason) {

        Objects.requireNonNull(permitSeason, "permitSeason is null");

        return diaryEntryValidationError(harvest, String.format(
                "päivämäärä ei sisälly luvan metsästyskauteen: %s",
                permitSeason.toString(DATE_FORMATTER)));
    }

    @Nonnull
    public static String mooseCalfMissingGender(@Nonnull final MooseDataCardMooseCalf mooseCalf) {
        return diaryEntryValidationError(mooseCalf, "saalisilmoituksessa ei ole annettu vasan sukupuolta");
    }

    @Nonnull
    public static String genderOfMooseCalfContainsIllegalCharacters(@Nonnull final MooseDataCardMooseCalf mooseCalf) {
        return diaryEntryValidationError(mooseCalf,
                String.format("sukupuoli sisältää laittomia merkkejä: %s", mooseCalf.getGender()));
    }

    private static final String getPrefix(@Nonnull final DateAndLocation entry) {
        Objects.requireNonNull(entry);

        final Class<? extends DateAndLocation> entryClass = entry.getClass();

        if (entryClass.equals(MooseDataCardMooseMale.class)) {
            return "Hirviuros";
        } else if (entryClass.equals(MooseDataCardMooseFemale.class)) {
            return "Hirvinaaras";
        } else if (entryClass.equals(MooseDataCardMooseCalf.class)) {
            return "Hirvivasa";
        } else if (entryClass.equals(MooseDataCardObservation.class)) {
            return "Hirvihavainto";
        } else if (entryClass.equals(MooseDataCardLargeCarnivoreObservation.class)) {
            return "Suurpetohavainto";
        }

        throw new IllegalStateException("Name unknown for moose data card diary entry class: " + entryClass.getName());
    }

    @Nonnull
    public static String huntingAreaNotGivenAtAll() {
        return "Metsästysalueen kokoa ei ole annettu (kohta 8.1)";
    }

    @Nonnull
    public static String effectiveHuntingAreaLargerThanTotalHuntingArea(
            final double effectiveHuntingArea, final double totalHuntingArea) {

        return String.format("Metsästykseen käytetyn alueen koko on virheellisesti ilmoitettu suuremmaksi kuin koko " +
                "metsästysalueen koko (kohta 8.1): %s > %s", effectiveHuntingArea, totalHuntingArea);
    }

    @Nonnull
    public static String totalHuntingAreaMissingAndEffectiveHuntingAreaGivenAsPercentageShare() {
        return "Metsästykseen käytetyn alueen koko ilmoitettu prosenttiosuutena koko metsästysalueen pinta-alasta, " +
                "jota ei ole annettu (kohta 8.1)";
    }

    @Nonnull
    public static String moosesRemainingInTotalHuntingAreaGivenButAreaMissing() {
        return "Hirvien jäävä kanta on annettu koko metsästysalueelle, mutta sen pinta-alaa ei ole annettu (kohta 8.1)";
    }

    @Nonnull
    public static String moosesRemainingInEffectiveHuntingAreaGivenButAreaMissing() {
        return "Hirvien jäävä kanta on annettu metsästykseen käytetylle alueelle, mutta sen pinta-alaa ei ole " +
                "annettu (kohta 8.1)";
    }

    @Nonnull
    public static String moosesRemainingInEffectiveHuntingAreaGreaterThanMoosesRemainingInTotalHuntingArea(
            final int moosesRemaingingInEffectiveArea, final int moosesRemaingingInTotalArea) {

        return String.format("Hirvien jäävä kanta metsästykseen käytetylle alueelle on virheellisesti ilmoitettu " +
                "suuremmaksi kuin koko metsästysalueelle jäävä kanta (kohta 8.1): %s > %s",
                moosesRemaingingInEffectiveArea, moosesRemaingingInTotalArea);
    }

    @Nonnull
    public static String moosesRemainingNotGivenAtAll() {
        return "Hirvien jäävää kantaa ei ole annettu (kohta 8.1)";
    }

    @Nonnull
    public static String adultMaleHarvestCountMismatch(final int actual, final int expected) {
        return String.format(
                "Aikuisten kaadettujen hirviurosten määrä yhteenvedossa (kohta 8.2) ei vastaa saalisilmoitusten " +
                        "yhteenlaskettua määrää: yhteenveto %d, ilmoituksia %d",
                actual, expected);
    }

    @Nonnull
    public static String adultFemaleHarvestCountMismatch(final int actual, final int expected) {
        return String.format(
                "Aikuisten kaadettujen hirvinaarasten määrä yhteenvedossa (kohta 8.2) ei vastaa saalisilmoitusten " +
                        "yhteenlaskettua määrää: yhteenveto %d, ilmoituksia %d",
                actual, expected);
    }

    @Nonnull
    public static String youngMaleHarvestCountMismatch(final int actual, final int expected) {
        return String.format(
                "Kaadettujen hirviurosvasojen määrä yhteenvedossa (kohta 8.2) ei vastaa saalisilmoitusten " +
                        "yhteenlaskettua määrää: yhteenveto %d, ilmoituksia %d",
                actual, expected);
    }

    @Nonnull
    public static String youngFemaleHarvestCountMismatch(final int actual, final int expected) {
        return String.format(
                "Kaadettujen hirvinaarasvasojen määrä yhteenvedossa (kohta 8.2) ei vastaa saalisilmoitusten " +
                        "yhteenlaskettua määrää: yhteenveto %d, ilmoituksia %d",
                actual, expected);
    }

    @Nonnull
    public static String nonEdibleAdultHarvestCountMismatch(final int actual, final int expected) {
        return String.format(
                "Ihmisravinnoksi kelpaamattomien aikuisten hirvien määrä yhteenvedossa (kohta 8.2) ei vastaa " +
                        "saalisilmoitusten yhteenlaskettua määrää: yhteenveto %d, ilmoituksia %d",
                actual, expected);
    }

    @Nonnull
    public static String nonEdibleYoungHarvestCountMismatch(final int actual, final int expected) {
        return String.format(
                "Ihmisravinnoksi kelpaamattomien hirvivasojen määrä yhteenvedossa (kohta 8.2) ei vastaa " +
                        "saalisilmoitusten yhteenlaskettua määrää: yhteenveto %d, ilmoituksia %d",
                actual, expected);
    }

    @Nonnull
    public static String huntingEndDateNotGiven() {
        return "Pyynnin päättymispäivää ei ole annettu (kohta 8.5)";
    }

    @Nonnull
    public static String huntingEndDateNotWithinPermitSeason(
            @Nonnull final LocalDate huntingEndDate, @Nonnull final Has2BeginEndDates permitSeason) {

        return String.format("Pyynnin päättymispäivä (kohta 8.5) ei sisälly luvan metsästyskauteen (%s): %s",
                permitSeason.toString(DATE_FORMATTER), DATE_FORMATTER.print(huntingEndDate));
    }

    @Nonnull
    public static String huntingClubAlreadyHasGroupNotCreatedWithinMooseDataCardImport() {
        return "Metsästysryhmää ei voi luoda, koska seuralla on jo metsästysryhmä, jota ei ole luotu " +
                "hirvitietokortin pohjalta";
    }

    @Nonnull
    public static String contactPersonMemberOfMultipleMooseDataCardGroupsButWithNoActiveOccupations() {
        return "Ei voitu yksikäsitteisesti päätellä oikeaa metsästysryhmää, koska hirvitietokortin yhteyshenkilöllä " +
                "on jäsenyys useassa hirvitietokorttiryhmässä, mutta yksikään niistä ei ole voimassa";
    }

    @Nonnull
    public static String contactPersonMemberOfMultipleMooseDataCardGroupsButNotAsLeader() {
        return "Ei voitu yksikäsitteisesti päätellä oikeaa metsästysryhmää, koska hirvitietokortin yhteyshenkilö " +
                "on jäsenenä useassa hirvitietokorttiryhmässä mutta yhdessäkään ei metsästyksenjohtajana";
    }

    @Nonnull
    public static String contactPersonIsLeaderInMultipleMooseDataCardGroups() {
        return "Ei voitu yksikäsitteisesti päätellä oikeaa metsästysryhmää, koska hirvitietokortin yhteyshenkilö " +
                "on metsästyksenjohtajana useassa hirvitietokorttiryhmässä";
    }

    @Nonnull
    public static String couldNotCreateHuntingClubGroup() {
        return "Metsästysryhmän luonti epäonnistui";
    }

    @Nonnull
    public static String couldNotCreateHuntingDays() {
        return "Metsästyspäivien tallennus epäonnistui";
    }

    @Nonnull
    public static String couldNotCreateHarvests() {
        return "Saaliskirjausten tallennus epäonnistui";
    }

    @Nonnull
    public static String couldNotCreateObservations() {
        return "Havaintokirjausten tallennus epäonnistui";
    }

    @Nonnull
    public static String couldNotCreateMooseDataCardImport() {
        return "Hirvitietokortin sisäänluvun tietojen tallennus tietokantaan epäonnistui";
    }

    @Nonnull
    public static String failureOnFileStorage() {
        return "Hirvitietokortin tiedostojen tallennus epäonnistui";
    }

    private MooseDataCardImportFailureReasons() {
        throw new AssertionError();
    }

}
