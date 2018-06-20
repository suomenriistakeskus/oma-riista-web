package fi.riista.feature.huntingclub.moosedatacard.validation;

import fi.riista.feature.common.entity.GeoLocation;
import io.vavr.Tuple;
import io.vavr.Tuple4;
import io.vavr.control.Either;

public class MooseDataCardPage1Validation {

    public final Either<String, String> hunterNumberOrDateOfBirth;
    public final String permitNumber;
    public final String clubCode;
    public final GeoLocation clubCoordinates;

    public MooseDataCardPage1Validation(final Either<String, String> hunterNumberOrDateOfBirth,
                                        final String permitNumber,
                                        final String clubCode,
                                        final GeoLocation clubCoordinates) {

        this.hunterNumberOrDateOfBirth = hunterNumberOrDateOfBirth;
        this.permitNumber = permitNumber;
        this.clubCode = clubCode;
        this.clubCoordinates = clubCoordinates;
    }

    public Tuple4<Either<String, String>, String, String, GeoLocation> asTuple4() {
        return Tuple.of(hunterNumberOrDateOfBirth, permitNumber, clubCode, clubCoordinates);
    }
}
