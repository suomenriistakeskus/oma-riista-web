package fi.riista.feature.huntingclub.moosedatacard.validation;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardContactPerson;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardPage1;
import fi.riista.util.F;
import fi.riista.util.Patterns;
import fi.riista.validation.Validators;
import io.vavr.Tuple;
import io.vavr.Tuple3;
import io.vavr.collection.Seq;
import io.vavr.control.Either;
import io.vavr.control.Validation;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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
import static fi.riista.util.ValidationUtils.flattenErrorsOrElseGet;
import static fi.riista.util.ValidationUtils.toValidation;
import static io.vavr.control.Validation.invalid;
import static io.vavr.control.Validation.valid;

public class MooseDataCardPage1Validator {

    private static final Pattern CLUB_CODE_PATTERN = Pattern.compile(Patterns.HUNTING_CLUB_CODE);
    private static final Pattern CLUB_COORDINATES_PATTERN = Pattern.compile("(\\d+);(\\d+)");

    private static final Predicate<String> CLUB_CODE_TESTER =
            clubCode -> CLUB_CODE_PATTERN.matcher(clubCode).matches();

    @Nonnull
    public Validation<List<String>, MooseDataCardPage1Validation> validate(
            @Nonnull final MooseDataCardPage1 page, @Nonnull final MooseDataCardFilenameValidation filenameValidation) {

        Objects.requireNonNull(page, "page is null");
        Objects.requireNonNull(filenameValidation, "filenameValidation is null");

        final Validation<Seq<String>, Either<String, String>> validationOfHunterNumberAndSsn =
                validateHunterNumberAndSsn(page.getContactPerson());

        final Validation<Seq<String>, Tuple3<String, String, GeoLocation>> validationOfRest =
                validatePermitNumber(page, filenameValidation)
                        .combine(validateClubCode(page, filenameValidation))
                        .combine(validateClubCoordinates(page))
                        .ap(Tuple::of);

        return Stream.of(validationOfHunterNumberAndSsn, validationOfRest)
                .collect(flattenErrorsOrElseGet(() -> {
                    final Tuple3<String, String, GeoLocation> t = validationOfRest.get();

                    return new MooseDataCardPage1Validation(validationOfHunterNumberAndSsn.get(), t._1, t._2, t._3);
                }));
    }

    @Nonnull
    private static Validation<Seq<String>, Either<String, String>> validateHunterNumberAndSsn(
            @Nonnull final MooseDataCardContactPerson contactPerson) {

        return validateHunterNumber(contactPerson)
                .combine(validateSsn(contactPerson))
                .ap((hunterNumberOpt, ssnOpt) -> F.optionallyEither(hunterNumberOpt, () -> ssnOpt))
                .flatMap(optionallyEitherHunterNumberOrSsn -> toValidation(
                        optionallyEitherHunterNumberOrSsn,
                        () -> invalid(io.vavr.collection.List.of(hunterNumberAndSsnMissingForContactPerson()))));
    }

    @Nonnull
    private static Validation<String, Optional<String>> validateHunterNumber(
            @Nonnull final MooseDataCardContactPerson contactPerson) {

        final String hunterNumber = StringUtils.trimToNull(contactPerson.getHunterNumber());

        if (hunterNumber == null) {
            return valid(Optional.empty());
        }

        return Validators.isValidHunterNumber(hunterNumber)
                ? valid(Optional.of(hunterNumber))
                : invalid(invalidHunterNumber(hunterNumber));
    }

    @Nonnull
    private static Validation<String, Optional<String>> validateSsn(
            @Nonnull final MooseDataCardContactPerson contactPerson) {

        final String ssn = StringUtils.trimToNull(contactPerson.getSsn());

        return ssn == null
                ? valid(Optional.empty())
                : Validators.isValidSsn(ssn) ? valid(Optional.of(ssn)) : invalid(invalidSsn());
    }

    @Nonnull
    private static Validation<String, String> validatePermitNumber(
            @Nonnull final MooseDataCardPage1 page, @Nonnull final MooseDataCardFilenameValidation filenameValidation) {

        final String permitNumber = StringUtils.trimToNull(page.getPermitNumber());

        if (permitNumber == null) {
            return invalid(missingPermitNumber());
        } else if (!Validators.isValidPermitNumber(permitNumber)) {
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
}
