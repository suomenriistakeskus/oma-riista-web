package fi.riista.feature.huntingclub.moosedatacard.validation;

import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.clubCoordinatesOutOfFinland;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.hunterNumberAndSsnMissingForContactPerson;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.huntingClubCodeMismatchBetweenNameAndContentOfXmlFile;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.invalidClubCode;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.invalidClubCoordinates;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.invalidHunterNumber;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.invalidPermitNumber;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.invalidSsn;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.missingClubCode;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.missingClubCoordinates;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.missingPermitNumber;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.permitNumberMismatchBetweenNameAndContentOfXmlFile;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.reportingPeriodBeginDateIsAfterEndDate;
import static javaslang.control.Validation.invalid;
import static javaslang.control.Validation.valid;

import com.google.common.collect.Range;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardContactPerson;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardPage1;
import fi.riista.util.F;
import fi.riista.util.Patterns;
import fi.riista.validation.FinnishHunterNumberValidator;
import fi.riista.validation.FinnishHuntingPermitNumberValidator;
import fi.riista.validation.FinnishSocialSecurityNumberValidator;

import javaslang.Value;
import javaslang.control.Either;
import javaslang.control.Validation;

import org.joda.time.LocalDate;

import javax.annotation.Nonnull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MooseDataCardPage1Validator {

    private static final FinnishSocialSecurityNumberValidator SSN_VALIDATOR =
            new FinnishSocialSecurityNumberValidator(true);

    private static final Pattern CLUB_CODE_PATTERN = Pattern.compile(Patterns.HUNTING_CLUB_CODE);
    private static final Pattern CLUB_COORDINATES_PATTERN = Pattern.compile("(\\d+);(\\d+)");

    private static final Predicate<String> CLUB_CODE_TESTER =
            clubCode -> CLUB_CODE_PATTERN.matcher(clubCode).matches();

    @Nonnull
    public Validation<List<String>, MooseDataCardPage1Validation> validate(
            @Nonnull final MooseDataCardPage1 page, @Nonnull final MooseDataCardFilenameValidation filenameValidation) {

        Objects.requireNonNull(page, "page is null");
        Objects.requireNonNull(filenameValidation, "filenameValidation is null");

        return validateHuntingNumberAndSsn(page.getContactPerson())
                .combine(validatePermitNumber(page, filenameValidation))
                .combine(validateClubCode(page, filenameValidation))
                .combine(validateClubCoordinates(page))
                .combine(validateReportingPeriod(page))
                .ap((hunterNumberOpt, ssnOpt, permitNumber, clubCode, clubCoordinates, reportingPeriodDateRange) -> {

                    final Either<String, String> eitherHunterNumberOrSsn = hunterNumberOpt.isPresent()
                            ? Either.left(hunterNumberOpt.get()) : Either.right(ssnOpt.get());

                    return new MooseDataCardPage1Validation(
                            eitherHunterNumberOrSsn, permitNumber, clubCode, clubCoordinates);
                })
                .leftMap(Value::toJavaList);
    }

    @Nonnull
    private static Validation.Builder<String, Optional<String>, Optional<String>> validateHuntingNumberAndSsn(
            @Nonnull final MooseDataCardContactPerson contactPerson) {

        final Validation.Builder<String, Optional<String>, Optional<String>> builder =
                validateHunterNumber(contactPerson).combine(validateSsn(contactPerson));

        final Validation<? extends Iterable<String>, Boolean> personReferencePresent = builder.ap((hnOpt, ssnOpt) -> {
            return hnOpt.map(hn -> true).orElseGet(() -> ssnOpt.map(ssn -> true).orElse(false));
        });

        return personReferencePresent.fold(errMsgs -> builder, isRefFound -> {
            if (!isRefFound) {
                final Validation.Builder<String, Optional<String>, Optional<String>> invalidBuilder = Validation
                        .combine(invalid(hunterNumberAndSsnMissingForContactPerson()), valid(Optional.empty()));
                return invalidBuilder;
            }

            return builder;
        });
    }

    @Nonnull
    private static Validation<String, Optional<String>> validateHunterNumber(
            @Nonnull final MooseDataCardContactPerson contactPerson) {

        return F.trimToOptional(contactPerson.getHunterNumber())
                .<Validation<String, Optional<String>>> map(hunterNumber -> {
                    return FinnishHunterNumberValidator.validate(hunterNumber, true)
                            ? valid(Optional.of(hunterNumber))
                            : invalid(invalidHunterNumber(hunterNumber));
                })
                .orElseGet(() -> valid(Optional.empty()));
    }

    @Nonnull
    private static Validation<String, Optional<String>> validateSsn(
            @Nonnull final MooseDataCardContactPerson contactPerson) {

        return F.trimToOptional(contactPerson.getSsn())
                .<Validation<String, Optional<String>>> map(ssn -> SSN_VALIDATOR.isValid(ssn, null)
                        ? valid(Optional.of(ssn))
                        : invalid(invalidSsn()))
                .orElseGet(() -> valid(Optional.empty()));
    }

    @Nonnull
    private static Validation<String, String> validatePermitNumber(
            @Nonnull final MooseDataCardPage1 page, @Nonnull final MooseDataCardFilenameValidation filenameValidation) {

        return F.trimToOptional(page.getPermitNumber())
                .<Validation<String, String>> map(
                        permitNumber -> FinnishHuntingPermitNumberValidator.validate(permitNumber, true)
                                ? valid(permitNumber)
                                : invalid(invalidPermitNumber(permitNumber)))
                .orElseGet(() -> invalid(missingPermitNumber()))
                .flatMap(permitNumber -> filenameValidation.permitNumber.equals(permitNumber)
                        ? valid(permitNumber)
                        : invalid(permitNumberMismatchBetweenNameAndContentOfXmlFile(permitNumber)));
    }

    @Nonnull
    private static Validation<String, String> validateClubCode(
            @Nonnull final MooseDataCardPage1 page, @Nonnull final MooseDataCardFilenameValidation filenameValidation) {

        return F.trimToOptional(page.getHuntingClubCode())
                .<Validation<String, String>> map(clubCode -> CLUB_CODE_TESTER.test(clubCode)
                        ? valid(clubCode)
                        : invalid(invalidClubCode(clubCode)))
                .orElseGet(() -> invalid(missingClubCode()))
                .flatMap(clubCode -> filenameValidation.clubCode.equals(clubCode)
                        ? valid(clubCode)
                        : invalid(huntingClubCodeMismatchBetweenNameAndContentOfXmlFile(clubCode)));
    }

    @Nonnull
    private static Validation<String, GeoLocation> validateClubCoordinates(@Nonnull final MooseDataCardPage1 page) {
        return F.trimToOptional(page.getClubInfo().getHuntingClubCoordinate())
                .<Validation<String, GeoLocation>> map(coordinates -> {
                    final Matcher matcher = CLUB_COORDINATES_PATTERN.matcher(coordinates);

                    if (!matcher.matches()) {
                        return invalid(invalidClubCoordinates(coordinates));
                    }

                    final long lat = Long.parseLong(matcher.group(1));
                    final long lng = Long.parseLong(matcher.group(2));

                    if (!BoundariesOfFinland.isWithinBoundaries(lat, lng)) {
                        return invalid(clubCoordinatesOutOfFinland(lat, lng));
                    }

                    return valid(new GeoLocation((int) lat, (int) lng));
                })
                .orElseGet(() -> invalid(missingClubCoordinates()));
    }

    @Nonnull
    private static Validation<String, Range<LocalDate>> validateReportingPeriod(
            @Nonnull final MooseDataCardPage1 page) {

        final LocalDate beginDate = page.getReportingPeriodBeginDate();
        final LocalDate endDate = page.getReportingPeriodEndDate();

        if (beginDate != null && endDate != null) {
            return beginDate.isAfter(endDate)
                    ? invalid(reportingPeriodBeginDateIsAfterEndDate(beginDate, endDate))
                    : valid(Range.closed(beginDate, endDate));
        }

        final Range<LocalDate> dateRange = beginDate != null
                ? Range.atLeast(beginDate) : endDate != null ? Range.atMost(endDate) : Range.all();

        return valid(dateRange);
    }

}
