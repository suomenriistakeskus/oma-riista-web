package fi.riista.integration.lupahallinta.parser;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import fi.riista.feature.common.entity.Has2BeginEndDatesDTO;
import fi.riista.util.DateUtil;
import fi.riista.validation.FinnishCreditorReferenceValidator;
import io.vavr.Tuple2;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static fi.riista.util.DateUtil.toDateTimeNullSafe;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static org.apache.commons.lang.StringUtils.isBlank;

public class PermitCSVSpeciesAmountParser {
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("dd.MM.yyyy");

    private static final Splitter COMMA_SPLITTER = Splitter.on(',').trimResults();

    private final List<String> errors;

    public PermitCSVSpeciesAmountParser(List<String> errors) {
        this.errors = errors;
    }

    public List<PermitCSVLine.SpeciesAmount> parse(final String species,
                                                   final String amounts,
                                                   final String dates,
                                                   final String dates2,
                                                   final String restrictionTypes,
                                                   final String restrictionAmounts,
                                                   final String referenceNumbers,
                                                   final boolean mooseLike) {
        return parse(
                COMMA_SPLITTER.splitToList(species),
                COMMA_SPLITTER.splitToList(amounts),
                COMMA_SPLITTER.splitToList(dates),
                COMMA_SPLITTER.splitToList(dates2),
                COMMA_SPLITTER.splitToList(restrictionTypes),
                COMMA_SPLITTER.splitToList(restrictionAmounts),
                COMMA_SPLITTER.splitToList(referenceNumbers),
                mooseLike);
    }

    public List<PermitCSVLine.SpeciesAmount> parse(final List<String> speciesList,
                                                   final List<String> amountList,
                                                   final List<String> dateList,
                                                   final List<String> date2List,
                                                   final List<String> restrictionTypeList,
                                                   final List<String> restrictionAmountList,
                                                   final List<String> referenceNumberList,
                                                   final boolean mooseLike) {
        if (speciesList.size() != amountList.size() ||
                speciesList.size() != restrictionTypeList.size() ||
                speciesList.size() != restrictionAmountList.size() ||
                speciesList.size() != dateList.size() ||
                speciesList.size() != date2List.size() ||
                speciesList.size() != referenceNumberList.size()) {
            errors.add(String.format(
                    "Eläinlajien määrä %s on eri kuin lupien määrä %s ja rajoitteiden määrä %s / %s ja lupa aikojen määrä %s / %s ja viitenumeroiden määrä %s",
                    speciesList.size(), amountList.size(), restrictionTypeList.size(), restrictionAmountList.size(), dateList.size(), date2List.size(), referenceNumberList.size()));

            return Collections.emptyList();
        }

        final Iterator<String> speciesIterator = speciesList.iterator();
        final Iterator<String> amountsIterator = amountList.iterator();
        final Iterator<String> restrictionTypeIterator = restrictionTypeList.iterator();
        final Iterator<String> restrictionAmountIterator = restrictionAmountList.iterator();
        final Iterator<String> datesIterator = dateList.iterator();
        final Iterator<String> dates2Iterator = date2List.iterator();
        final Iterator<String> referenceNumberIterator = referenceNumberList.iterator();

        final ArrayList<PermitCSVLine.SpeciesAmount> result = Lists.newArrayListWithExpectedSize(speciesList.size());

        while (speciesIterator.hasNext()) {
            final String speciesCode = speciesIterator.next();
            final String amount = amountsIterator.next();
            final String restrictionType = restrictionTypeIterator.next();
            final String restrictionAmount = restrictionAmountIterator.next();
            final String dates = datesIterator.next();
            final String dates2 = dates2Iterator.next();
            final String referenceNumber = referenceNumberIterator.next();

            PermitCSVLine.SpeciesAmount speciesAmount =
                    parseAmount(speciesCode, amount, restrictionType, restrictionAmount, dates, dates2, referenceNumber, mooseLike);

            if (speciesAmount != null) {
                result.add(speciesAmount);
            }
        }
        checkDates(result, mooseLike);
        checkSpeciesGivenOnlyOnce(result);
        return result;
    }

    private PermitCSVLine.SpeciesAmount parseAmount(final String speciesCode,
                                                    final String amount,
                                                    final String restrictionType,
                                                    final String restrictionAmount,
                                                    final String dates,
                                                    final String dates2,
                                                    final String referenceNumber,
                                                    boolean mooseLike) {

        final PermitCSVLine.SpeciesAmount speciesAmount = new PermitCSVLine.SpeciesAmount();
        speciesAmount.setAmount(parseAmount(amount));

        if (speciesAmount.getAmount() == null || BigDecimal.ZERO.compareTo(speciesAmount.getAmount()) == 0) {
            return null;
        }

        final Integer speciesOfficialCode = parseSpeciesCode(speciesCode);

        if (speciesOfficialCode == null) {
            return null;
        }

        speciesAmount.setSpeciesOfficialCode(speciesOfficialCode);

        speciesAmount.setRestrictionType(isBlank(restrictionType) ? null : restrictionType);
        speciesAmount.setRestrictionAmount(
                isBlank(restrictionAmount) ? null : new BigDecimal(restrictionAmount.trim()));

        if (isBlank(dates)) {
            errors.add("Lupa-ajat on tyhjä");
            return null;
        }

        final Tuple2<LocalDate, LocalDate> datePair = parseBeginAndEndDates(dates);

        if (datePair == null) {
            errors.add("Lupa-ajat virheelliset: ensimmäinen aikaväli on annettava");
            return null;
        }

        speciesAmount.setBeginDate(datePair._1());
        speciesAmount.setEndDate(datePair._2());

        if (!isBlank(dates2)) {
            final Tuple2<LocalDate, LocalDate> datePair2 = parseBeginAndEndDates(dates2);

            if (datePair2 == null) {
                errors.add(String.format("Lupa-ajat virheelliset: %s", dates2));
                return null;
            }

            speciesAmount.setBeginDate2(datePair2._1());
            speciesAmount.setEndDate2(datePair2._2());
        }

        if (mooseLike && (isBlank(referenceNumber) || !FinnishCreditorReferenceValidator.validate(referenceNumber, true))) {
            errors.add("Viitenumero on pakollinen mutta se on virheellinen:" + referenceNumber);
            return null;
        }
        speciesAmount.setReferenceNumber(isBlank(referenceNumber) ? null : referenceNumber);
        return speciesAmount;
    }

    private Integer parseSpeciesCode(String speciesCode) {
        if (isBlank(speciesCode)) {
            errors.add("Eläinlaji puuttuu");
            return null;
        }

        try {
            return Integer.parseInt(speciesCode.trim());

        } catch (NumberFormatException e) {
            errors.add("Virheellinen eläinlaji:" + speciesCode);
        }

        return null;
    }

    private BigDecimal parseAmount(String value) {
        if (isBlank(value)) {
            return BigDecimal.ZERO;
        }

        try {
            return parseAmountInternal(value);

        } catch (NumberFormatException e) {
            errors.add("Virheellinen lupamäärä:" + value);
        }

        return null;
    }

    static BigDecimal parseAmountInternal(String value) {
        if (isBlank(value)) {
            return BigDecimal.ZERO;
        }

        return new BigDecimal(value.trim());
    }

    private Tuple2<LocalDate, LocalDate> parseBeginAndEndDates(final String dates) {
        try {
            return DateUtil.parseDateInterval(dates, DATE_FORMAT);

        } catch (RuntimeException e) {
            errors.add(String.format("Lupa-ajat virheelliset: %s", dates));
        }

        return null;
    }

    private void checkDates(List<PermitCSVLine.SpeciesAmount> amounts, boolean mooselike) {
        amounts.forEach(a -> {
            if (beginAfterEnd(a.getBeginDate(), a.getEndDate())) {
                errors.add("Lupa-ajat virheelliset, ensimmäinen aikaväli: alkupäivä ei ole ennen loppupäivää. "
                        + fmtDates(a.getBeginDate(), a.getEndDate()));
            }
            if (beginAfterEnd(a.getBeginDate2(), a.getEndDate2())) {
                errors.add("Lupa-ajat virheelliset, toinen aikaväli: alkupäivä ei ole ennen loppupäivää. "
                        + fmtDates(a.getBeginDate2(), a.getEndDate2()));
            }

            if (beginAfterEnd(a.getEndDate(), a.getBeginDate2()) || a.getEndDate().equals(a.getBeginDate2())) {
                errors.add("Lupa-ajat virheelliset: ensimmäinen aikaväli täytyy olla jälkimmäistä ennen. "
                        + fmtDates(a));
            }

            if (isIntervalLongerThanYear(a.getBeginDate(), a.getEndDate())) {
                errors.add("Lupa-ajat virheelliset: ensimmäinen aikaväli on yli 365 päivää. "
                        + fmtDates(a.getBeginDate(), a.getEndDate()));
            }
            if (isIntervalLongerThanYear(a.getBeginDate2(), a.getEndDate2())) {
                errors.add("Lupa-ajat virheelliset: jälkimmäinen aikaväli on yli 365 päivää. "
                        + fmtDates(a.getBeginDate2(), a.getEndDate2()));
            }
            if (isIntervalLongerThanYear(a.getBeginDate(), a.getEndDate2())) {
                errors.add("Lupa-ajat virheelliset: ensimmäinen aikavälin alku ja jälkimmäisen aikavälin loppu on yli 365 päivää. "
                        + fmtDates(a));
            }
            if (mooselike && a.collectClosedRangeHuntingYears().count() > 1) {
                errors.add("Hirvieläinluvan voimassaoloajat täytyy olla yhden metsästysvuoden sisällä");
            }
        });
    }

    private static boolean beginAfterEnd(LocalDate begin, LocalDate end) {
        return begin != null && end != null && begin.isAfter(end);
    }

    private static boolean isIntervalLongerThanYear(final LocalDate begin, final LocalDate end) {
        return begin != null && end != null
                && new Duration(toDateTimeNullSafe(begin), toDateTimeNullSafe(end)).getStandardDays() > 365;
    }

    private void checkSpeciesGivenOnlyOnce(List<PermitCSVLine.SpeciesAmount> amounts) {
        final Map<Integer, Long> count = amounts.stream().collect(groupingBy(a -> a.getSpeciesOfficialCode(), counting()));
        final boolean anyGivenMoreThanOnce = count.values().stream().anyMatch(c -> c > 1);
        if (anyGivenMoreThanOnce) {
            errors.add("Luvalle voi antaa eläinlajin vain kerran");
        }
    }

    private static String fmtDates(LocalDate beginDate, LocalDate endDate) {
        return DATE_FORMAT.print(beginDate) + "-" + DATE_FORMAT.print(endDate);
    }

    private static String fmtDates(final Has2BeginEndDatesDTO dates) {
        return fmtDates(dates.getBeginDate(), dates.getEndDate()) + ", " + fmtDates(dates.getBeginDate2(), dates.getEndDate2());
    }
}
