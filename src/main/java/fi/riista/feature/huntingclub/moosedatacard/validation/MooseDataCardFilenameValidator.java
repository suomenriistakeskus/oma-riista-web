package fi.riista.feature.huntingclub.moosedatacard.validation;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import fi.riista.config.Constants;
import fi.riista.util.Patterns;
import io.vavr.Tuple;
import io.vavr.Tuple3;
import io.vavr.Value;
import io.vavr.control.Validation;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.basenameMismatchBetweenXmlAndPdfFile;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.invalidFilename;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.invalidTimestampInXmlFileName;
import static io.vavr.control.Validation.invalid;
import static io.vavr.control.Validation.valid;
import static java.lang.String.format;

public class MooseDataCardFilenameValidator {

    private static final String TIMESTAMP_FORMAT_OF_MOOSE_DATA_CARD_FILENAME = "yyyyMMddHHmmss";

    // Package-private because referenced by a test class.
    static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormat.forPattern(TIMESTAMP_FORMAT_OF_MOOSE_DATA_CARD_FILENAME);

    @Nonnull
    public Validation<List<String>, MooseDataCardFilenameValidation> validate(@Nonnull final String xmlFileName,
                                                                              @Nonnull final String pdfFileName) {

        Objects.requireNonNull(xmlFileName, "xmlFileName is null");
        Objects.requireNonNull(pdfFileName, "pdfFileName is null");

        final Validation<String, Boolean> basenameMatchValidation =
                doFilenamesHaveCommonBasename(xmlFileName, pdfFileName)
                        ? valid(true)
                        : invalid(basenameMismatchBetweenXmlAndPdfFile());

        return basenameMatchValidation
                .combine(InputFileType.XML.validate(xmlFileName))
                .combine(InputFileType.PDF.validate(pdfFileName))
                // No need to touch pdfTuple because it consists of the same tokens as xmlTuple.
                .ap((match, xmlTuple, pdfTuple) -> xmlTuple.apply(MooseDataCardFilenameValidation::new))
                .mapError(Value::toJavaList);
    }

    private static boolean doFilenamesHaveCommonBasename(final String filename1, final String filename2) {
        final Function<String, String> basenameFn = filename -> {
            final int lastIndexOfDot = filename.lastIndexOf('.');
            final int basenameEndIndex = lastIndexOfDot < 0 ? filename.length() : lastIndexOfDot;
            return filename.substring(0, basenameEndIndex);
        };

        return basenameFn.apply(filename1).equals(basenameFn.apply(filename2));
    }

    private enum InputFileType {

        XML, PDF;

        private final Pattern filenamePattern;

        InputFileType() {
            this.filenamePattern = Pattern.compile(
                    format("(%s)-(%s)-(%s)\\.%s", Patterns.PERMIT_NUMBER, Patterns.HUNTING_CLUB_CODE,
                            Patterns.RECENT_TIMESTAMP, canonicalName()),
                    Pattern.CASE_INSENSITIVE);
        }

        @Nonnull
        public Validation<String, Tuple3<String, String, DateTime>> validate(@Nonnull final String filename) {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(filename),
                    format("Filename was empty while expected to have extension %s", canonicalName()));

            final Matcher matcher = this.filenamePattern.matcher(filename);

            if (!matcher.matches()) {
                return invalid(invalidFilename(filename, canonicalName()));
            }

            final String timestampStr = matcher.group(3);

            try {
                final LocalDateTime timestamp = DATE_FORMATTER.parseLocalDateTime(timestampStr);
                return valid(Tuple.of(
                        matcher.group(1), matcher.group(2), timestamp.toDateTime(Constants.DEFAULT_TIMEZONE)));

            } catch (final Exception e) {
                return invalid(invalidTimestampInXmlFileName(timestampStr));
            }
        }

        public @Nonnull String canonicalName() {
            return this.name().toLowerCase();
        }
    }
}
