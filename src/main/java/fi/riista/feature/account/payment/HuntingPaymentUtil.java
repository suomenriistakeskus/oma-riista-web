package fi.riista.feature.account.payment;

import fi.riista.feature.organization.person.Person;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class HuntingPaymentUtil {
    public static Set<Integer> getHuntingPaymentPdfYears(final Person person) {
        return getHuntingPaymentPdfYears(DateUtil.today(), person);
    }

    static Set<Integer> getHuntingPaymentPdfYears(final LocalDate today, final Person person) {
        return DateUtil.streamCurrentAndNextHuntingYear(today)
                .filter(person::isInvoiceReferenceAvailable)
                .filter(person::isPaymentDateMissing)
                .filter(year -> isPaymentAllowedForHuntingSeason(year, today))
                .boxed()
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public static boolean isPaymentAllowedForHuntingSeason(final int huntingYear) {
        return isPaymentAllowedForHuntingSeason(huntingYear, DateUtil.today());
    }

    private static boolean isPaymentAllowedForHuntingSeason(final int huntingYear, final LocalDate today) {
        final LocalDate firstPaymentDate = firstValidPaymentDate(huntingYear);
        final LocalDate endDate = DateUtil.huntingYearEndDate(huntingYear);

        return !(today.isBefore(firstPaymentDate) || today.isAfter(endDate));
    }

    // Not available before release date of Hunter magazine
    @Nonnull
    private static LocalDate firstValidPaymentDate(final int huntingYear) {
        final LocalDate beginDate = DateUtil.huntingYearBeginDate(huntingYear);
        return new LocalDate(beginDate.getYear(), 7, 15);
    }

    private HuntingPaymentUtil() {
        throw new AssertionError();
    }
}
