package fi.riista.feature.huntingclub.moosedatacard;

import fi.riista.feature.common.entity.Has2BeginEndDates;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardLargeCarnivoreObservation;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseCalf;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseFemale;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseMale;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardObservation;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static fi.riista.util.DateUtil.DATE_FORMAT_FINNISH;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;

public final class MooseDataCardImportMessages {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern(DATE_FORMAT_FINNISH);

    public static String huntingDayWithoutDate() {
        return huntingDayAbandoned(null, "päivämäärä puuttuu");
    }

    public static String huntingDayStartDateNotWithinPermittedSeason(@Nonnull final LocalDate startDate,
                                                                     @Nonnull final Has2BeginEndDates permitSeason) {

        Objects.requireNonNull(startDate, "startDate is null");
        Objects.requireNonNull(permitSeason, "permitSeason is null");

        return huntingDayAbandoned(startDate, String.format(
                "päivämäärä ei sisälly luvan metsästyskauteen: %s", permitSeason.toString(DATE_FORMATTER)));
    }

    public static String huntingDayEndDateNotWithinPermittedSeason(
            @Nonnull final LocalDate startDate,
            @Nonnull final LocalDate endDate,
            @Nonnull final Has2BeginEndDates permitSeason) {

        Objects.requireNonNull(startDate, "startDate is null");
        Objects.requireNonNull(endDate, "endDate is null");
        Objects.requireNonNull(permitSeason, "permitSeason is null");

        return huntingDayAbandoned(startDate, String.format(
                "päättymispäivämäärä %s ei sisälly luvan metsästyskauteen: %s",
                DATE_FORMATTER.print(endDate),
                permitSeason.toString(DATE_FORMATTER)));
    }

    public static String huntingDayAbandoned(@Nullable final LocalDate huntingDay, @Nonnull final String errMsg) {
        Objects.requireNonNull(errMsg, "errMsg is null");

        return String.format("Metsästyspäivä %s - hylätty, koska %s",
                huntingDay != null ? DATE_FORMATTER.print(huntingDay) : "<päivämäärää ei annettu>", errMsg);
    }

    public static String alreadyExistingHuntingDaysIgnored(@Nonnull final Stream<LocalDate> dates) {
        return "Seuraavia metsästyspäiviä ei otettu huomioon, koska niiden tiedot on tallennettu järjestelmään jo " +
                "aiemmin: " +
                formatDatesAsString(dates);
    }

    public static String observationAbandonedBecauseOfMissingDate(@Nonnull final DateAndLocation observation) {
        return observationAbandoned(observation, "päivämäärää ei ole annettu");
    }

    public static String sumOfSeenMoosesOfObservationIsNotGreaterThanZero(
            @Nonnull final MooseDataCardObservation observation) {

        return observationAbandoned(observation, "nähtyjen hirvien määriä ei ole annettu tai niiden summa on nolla");
    }

    public static String largeCarnivoreMissingObservationType(
            @Nonnull final MooseDataCardLargeCarnivoreObservation observation) {

        return observationAbandoned(observation, "havaintotyyppiä ei ole annettu");
    }

    public static String observationTypeOfLargeCarnivoreContainsIllegalCharacters(
            @Nonnull final MooseDataCardLargeCarnivoreObservation observation) {

        return observationAbandoned(observation,
                String.format("havaintotyyppi sisältää laittomia merkkejä: %s", observation.getObservationType()));
    }

    public static String sumOfSpecimenAmountsOfLargeCarnivoreObservationIsNotGreaterThanZero(
            @Nonnull final MooseDataCardLargeCarnivoreObservation observation) {

        return observationAbandoned(observation, "lajiyksilömääriä ei ole annettu tai niiden summa on nolla");
    }

    public static String observationAbandoned(@Nonnull final DateAndLocation observation,
                                              @Nonnull final String reason) {

        Objects.requireNonNull(reason, "reason is null");

        final String dateAsString = Optional.ofNullable(observation.getDate())
                .map(DATE_FORMATTER::print)
                .map(" "::concat)
                .map(str -> str + " -")
                .orElseGet(String::new);

        return String.format("%s%s hylätty, koska %s", getPrefix(observation), dateAsString, reason);
    }

    public static String harvestsIgnoredBecauseOfAlreadyExistingHuntingDays(
            @Nonnull final Map<LocalDate, Long> datesAndCounts) {

        return "Saaliskirjauksia seuraaville päivämäärille ei otettu huomioon, koska ne liittyvät metsästyspäiviin, " +
                "joiden tiedot on tallennettu järjestelmään jo aiemmin: " +
                formatDatesAndCountsAsString(datesAndCounts);
    }

    public static String observationsIgnoredBecauseOfAlreadyExistingHuntingDays(
            @Nonnull final Map<LocalDate, Long> datesAndCounts) {

        return "Havaintokirjauksia seuraaville päivämäärille ei otettu huomioon, koska ne liittyvät " +
                "metsästyspäiviin, joiden tiedot on tallennettu järjestelmään jo aiemmin: " +
                formatDatesAndCountsAsString(datesAndCounts);
    }

    public static String missingHuntingDaysCreated(@Nonnull final Stream<LocalDate> dates) {
        return "Seuraavat metsästyspäivät luotiin automaattisesti, vaikka ne puuttuivat hirvitietokortilta, koska " +
                "löytyi saalis- tai havaintokirjauksia kyseisille päivämäärille: " +
                formatDatesAsString(dates);
    }

    private static String formatDatesAsString(final Stream<LocalDate> dates) {
        return transformAndJoin(dates, DATE_FORMATTER::print);
    }

    private static String formatDatesAndCountsAsString(final Map<LocalDate, Long> datesAndCounts) {
        return transformAndJoin(
                datesAndCounts.entrySet().stream().sorted(comparing(Map.Entry::getKey)),
                entry -> String.format("%s%s",
                        DATE_FORMATTER.print(entry.getKey()),
                        entry.getValue() == 1 ? "" : String.format(" (%d kpl)", entry.getValue())));
    }

    private static <T> String transformAndJoin(final Stream<T> objects, final Function<T, String> strFn) {
        return Objects.requireNonNull(objects).map(strFn).collect(joining(", "));
    }

    private static final String getPrefix(@Nonnull final DateAndLocation diaryEntry) {
        Objects.requireNonNull(diaryEntry);

        final Class<? extends DateAndLocation> entryClass = diaryEntry.getClass();

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

    private MooseDataCardImportMessages() {
        throw new AssertionError();
    }
}
