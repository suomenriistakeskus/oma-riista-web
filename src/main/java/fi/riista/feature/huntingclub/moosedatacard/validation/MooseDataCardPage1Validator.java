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
import static fi.riista.util.ValidationUtils.reduce;
import static fi.riista.util.ValidationUtils.toValidation;
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
import javaslang.Tuple;
import javaslang.Tuple3;
import javaslang.collection.List;
import javaslang.control.Either;
import javaslang.control.Validation;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
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
    public Validation<java.util.List<String>, MooseDataCardPage1Validation> validate(
            @Nonnull final MooseDataCardPage1 page, @Nonnull final MooseDataCardFilenameValidation filenameValidation) {

        Objects.requireNonNull(page, "page is null");
        Objects.requireNonNull(filenameValidation, "filenameValidation is null");

        final Validation<List<String>, Either<String, String>> validationOfHunterNumberAndSsn =
                validateHunterNumberAndSsn(page.getContactPerson());

        final Validation<List<String>, Tuple3<String, String, GeoLocation>> validationOfRest =
                validatePermitNumber(page, filenameValidation)
                        .combine(validateClubCode(page, filenameValidation))
                        .combine(validateClubCoordinates(page))
                        .combine(validateReportingPeriod(page))
                        .ap((permitNumber, clubCode, clubCoordinates, reportingPeriod) -> {
                            return Tuple.of(permitNumber, clubCode, clubCoordinates);
                        });

        return reduce(validationOfHunterNumberAndSsn, validationOfRest, (eitherHunterNumberOrSsn, tuple3) -> {
            return new MooseDataCardPage1Validation(eitherHunterNumberOrSsn, tuple3._1, tuple3._2, tuple3._3);
        });
    }

    @Nonnull
    private static Validation<List<String>, Either<String, String>> validateHunterNumberAndSsn(
            @Nonnull final MooseDataCardContactPerson contactPerson) {

        return validateHunterNumber(contactPerson)
                .combine(validateSsn(contactPerson))
                .ap((hunterNumberOpt, ssnOpt) -> F.optionallyEither(hunterNumberOpt, () -> ssnOpt))
                .flatMap(optionallyEitherHunterNumberOrSsn -> toValidation(
                        optionallyEitherHunterNumberOrSsn,
                        () -> invalid(List.of(hunterNumberAndSsnMissingForContactPerson()))));
    }

    @Nonnull
    private static Validation<String, Optional<String>> validateHunterNumber(
            @Nonnull final MooseDataCardContactPerson contactPerson) {

        final String hunterNumber = StringUtils.trimToNull(contactPerson.getHunterNumber());

        if (hunterNumber == null) {
            return valid(Optional.empty());
        }

        return FinnishHunterNumberValidator.validate(hunterNumber, true)
                ? valid(Optional.of(hunterNumber))
                : invalid(invalidHunterNumber(hunterNumber));
    }

    @Nonnull
    private static Validation<String, Optional<String>> validateSsn(
            @Nonnull final MooseDataCardContactPerson contactPerson) {

        final String ssn = StringUtils.trimToNull(contactPerson.getSsn());

        return ssn == null
                ? valid(Optional.empty())
                : SSN_VALIDATOR.isValid(ssn, null) ? valid(Optional.of(ssn)) : invalid(invalidSsn());
    }

    @Nonnull
    private static Validation<String, String> validatePermitNumber(
            @Nonnull final MooseDataCardPage1 page, @Nonnull final MooseDataCardFilenameValidation filenameValidation) {

        final String permitNumber = StringUtils.trimToNull(page.getPermitNumber());

        if (permitNumber == null) {
            return invalid(missingPermitNumber());
        } else if (!FinnishHuntingPermitNumberValidator.validate(permitNumber, true)) {
            return invalid(invalidPermitNumber(permitNumber));
        }

        return filenameValidation.permitNumber.equals(permitNumber)
                ? valid(permitNumber)
                : invalid(permitNumberMismatchBetweenNameAndContentOfXmlFile(permitNumber));
    }

    @Nonnull
    private static Validation<String, String> validateClubCode(@Nonnull final MooseDataCardPage1 page,
                                                               @Nonnull final MooseDataCardFilenameValidation filenameValidation) {

        final String clubCode = StringUtils.trimToNull(page.getHuntingClubCode());

        if (clubCode == null) {
            return invalid(missingClubCode());
        } else if (!CLUB_CODE_TESTER.test(clubCode)) {
            return invalid(invalidClubCode(clubCode));
        }

        return filenameValidation.clubCode.equals(clubCode)
                ? valid(clubCode)
                : invalid(huntingClubCodeMismatchBetweenNameAndContentOfXmlFile(clubCode));
    }

    @Nonnull
    private static Validation<String, GeoLocation> validateClubCoordinates(@Nonnull final MooseDataCardPage1 page) {
        final String coordinates = StringUtils.trimToNull(page.getClubInfo().getHuntingClubCoordinate());

        if (coordinates == null) {
            return invalid(missingClubCoordinates());
        }

        final Matcher matcher = CLUB_COORDINATES_PATTERN.matcher(coordinates);

        if (!matcher.matches()) {
            return invalid(invalidClubCoordinates(coordinates));
        }

        final long lat = Long.parseLong(matcher.group(1));
        final long lng = Long.parseLong(matcher.group(2));

        return BoundariesOfFinland.isWithinBoundaries(lat, lng)
                // Valid values can be safely casted.
                ? valid(new GeoLocation((int) lat, (int) lng))
                : invalid(clubCoordinatesOutOfFinland(lat, lng));
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
                ? Range.atLeast(beginDate)
                : endDate != null ? Range.atMost(endDate) : Range.all();

        return valid(dateRange);
    }

}
