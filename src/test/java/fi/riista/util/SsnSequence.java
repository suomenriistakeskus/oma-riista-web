package fi.riista.util;

import fi.riista.validation.FinnishSocialSecurityNumberValidator;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.joda.time.LocalDate;

import java.util.concurrent.atomic.AtomicReference;

public class SsnSequence {

    private static final LocalDate BEGIN_DATE = new LocalDate(1900, 1, 1);
    private static final SsnSequence INSTANCE_FAKES= createForSerial(900);
    private static final SsnSequence INSTANCE_REALS = createForSerial(1);
    private final AtomicReference<Tuple2<LocalDate, Integer>> nextBirthDateAndPersonNumber;

    private static SsnSequence createForSerial(int serial) {
        return new SsnSequence(new AtomicReference<>(Tuple.of(BEGIN_DATE, serial)));
    }

    private SsnSequence(AtomicReference<Tuple2<LocalDate, Integer>> nextBirthDateAndPersonNumber) {
        this.nextBirthDateAndPersonNumber = nextBirthDateAndPersonNumber;
    }

    public static String nextArtificialSsn() {
        return INSTANCE_FAKES.nextSsn();
    }

    public static String nextRealSsn() {
        return INSTANCE_REALS.nextSsn();
    }

    public String nextSsn() {
        final Tuple2<LocalDate, Integer> t = nextBirthDateAndPersonNumber.getAndUpdate(current -> {
            final LocalDate date = current._1.plusDays(1);
            final Integer i = current._2;
            if (date.getYear() < 2000) {
                return Tuple.of(date, i);
            }
            return Tuple.of(BEGIN_DATE, i + 1);
        });
        final int month = t._1.getMonthOfYear();
        final int day = t._1.getDayOfMonth();
        final int year = t._1.getYear() - 1900;

        final char checksum = t.apply(FinnishSocialSecurityNumberValidator::calculateChecksum);

        return String.format("%02d%02d%02d-%03d%s", day, month, year, t._2, checksum);
    }
}
