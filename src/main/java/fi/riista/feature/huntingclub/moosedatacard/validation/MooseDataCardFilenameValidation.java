package fi.riista.feature.huntingclub.moosedatacard.validation;

import org.joda.time.DateTime;

public class MooseDataCardFilenameValidation {

    public final String permitNumber;
    public final String clubCode;
    public final DateTime timestamp;

    public MooseDataCardFilenameValidation(
            final String permitNumber, final String clubCode, final DateTime timestamp) {

        this.permitNumber = permitNumber;
        this.clubCode = clubCode;
        this.timestamp = timestamp;
    }

}
