package fi.riista.feature.organization.rhy.annualstats.export;

import fi.riista.util.ExcelHelper;
import fi.riista.util.LocalisedString;
import io.vavr.control.Either;
import org.apache.poi.ss.usermodel.HorizontalAlignment;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class AnnualStatisticItemMeta {

    private static final DecimalFormat NUMBER_FORMATTER;

    private final LocalisedString title;
    private final Function<? super AnnualStatisticsExportItemDTO, Either<Number, String>> valueExtractor;
    private final boolean indentedOnPrintout;

    static {
        final DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        symbols.setGroupingSeparator(' ');

        NUMBER_FORMATTER = new DecimalFormat("###,###", symbols);
    }

    public static AnnualStatisticItemMeta forNumber(@Nonnull final LocalisedString title,
                                                    @Nonnull final Function<? super AnnualStatisticsExportItemDTO, Number> numberExtractor,
                                                    final boolean indented) {

        requireNonNull(title, "title is null");
        requireNonNull(numberExtractor, "numberExtractor is null");

        return new AnnualStatisticItemMeta(title, numberExtractor.andThen(Either::left), indented);
    }

    public static AnnualStatisticItemMeta forText(@Nonnull final LocalisedString title,
                                                  @Nonnull final Function<? super AnnualStatisticsExportItemDTO, String> textExtractor) {

        requireNonNull(title, "title is null");
        requireNonNull(textExtractor, "textExtractor is null");

        return new AnnualStatisticItemMeta(title, textExtractor.andThen(Either::right), false);
    }

    private AnnualStatisticItemMeta(final LocalisedString title,
                                    final Function<? super AnnualStatisticsExportItemDTO, Either<Number, String>> valueExtractor,
                                    final boolean indented) {

        this.title = title;
        this.indentedOnPrintout = indented;
        this.valueExtractor = valueExtractor;
    }

    public LocalisedString getTitle() {
        return title;
    }

    public boolean isIndentedOnPrintout() {
        return indentedOnPrintout;
    }

    public void populateExcelCell(final ExcelHelper sheetWrapper, final AnnualStatisticsExportItemDTO dto) {
        valueExtractor.apply(dto)
                .peek(text -> sheetWrapper.appendTextCell(text, HorizontalAlignment.RIGHT))
                .peekLeft(number -> {
                    if (number != null) {
                        sheetWrapper.appendNumberCell(number);
                    } else {
                        sheetWrapper.appendEmptyCell(1);
                    }
                });
    }

    public String extractValueAsText(final AnnualStatisticsExportItemDTO dto) {
        return valueExtractor.apply(dto)
                .mapLeft(number -> number == null ? null : NUMBER_FORMATTER.format(number))
                .getOrElseGet(n -> n == null ? null : String.valueOf(n));
    }
}
