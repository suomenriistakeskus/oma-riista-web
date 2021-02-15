package fi.riista.integration.srva.callring;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.feature.organization.rhy.SrvaRotation;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.joda.time.Weeks;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;
import static java.util.stream.Collectors.toList;

@Component
public class SrvaCallRingConfigurationService {

    @Resource
    private RiistanhoitoyhdistysRepository riistanhoitoyhdistysRepository;

    @Resource
    private OccupationRepository occupationRepository;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<SrvaCallRingConfiguration> generateConfigurationForEveryRhy() {
        final QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;
        final List<Riistanhoitoyhdistys> rhyList = riistanhoitoyhdistysRepository.findAllAsList(RHY.active.isTrue());

        final Map<Long, Set<Occupation>> allSrvaOccupations =
                occupationRepository.findActiveByOccupationTypeGroupByOrganisationId(OccupationType.SRVA_YHTEYSHENKILO);

        final Map<Long, Set<Occupation>> allContactPersons =
                occupationRepository.findActiveByOccupationTypeGroupByOrganisationId(OccupationType.TOIMINNANOHJAAJA);

        return rhyList.stream().map(rhy -> {

            final Set<Occupation> srva = allSrvaOccupations.getOrDefault(rhy.getId(), Collections.emptySet());
            final Set<Occupation> contactPersons = allContactPersons.getOrDefault(rhy.getId(), Collections.emptySet());

            final SrvaRotation rotation = rhy.getSrvaRotation();
            final LocalDate modificationTime = rhy.getRotationStart();
            final LocalDate now = DateUtil.today();
            final int rotationsSinceModification = getRotationsCount(modificationTime, now, rotation);
            final List<Phonenumber.PhoneNumber> phoneNumberList = getCallRingPhoneNumbers(srva);

            final int rotationOffset = phoneNumberList.isEmpty()
                    ? 0
                    : rotationsSinceModification % phoneNumberList.size();

            Collections.rotate(phoneNumberList, -1 * rotationOffset);
            final List<String> notificationEmailList = getNotificationEmailList(rhy, contactPersons);

            // Repeat all phone numbers twice as fallback
            final List<Phonenumber.PhoneNumber> repeatedPhoneNumberList = F.concat(phoneNumberList, phoneNumberList);

            return new SrvaCallRingConfiguration(rhy.getOfficialCode(), repeatedPhoneNumberList, notificationEmailList);

        }).collect(toList());
    }

    private static int getRotationsCount(final LocalDate start,
                                         final LocalDate end,
                                         final SrvaRotation rotation) {
        if (rotation == null || !start.isBefore(end)) {
            return 0;
        }

        switch (rotation) {
            case DAILY:
                return Days.daysBetween(start, end).getDays();
            case WEEKLY:
                return Weeks.weeksBetween(start, end).getWeeks();
            case MONTHLY:
                return Months.monthsBetween(start, end).getMonths();
            default:
                throw new IllegalArgumentException("Unsupported rotation type " + rotation);
        }
    }

    private static List<Phonenumber.PhoneNumber> getCallRingPhoneNumbers(final Set<Occupation> srvaOccupations) {
        return srvaOccupations.stream()
                .sorted(comparing(Occupation::getCallOrder, nullsLast(naturalOrder()))
                        .thenComparingLong(Occupation::getId))
                .map(Occupation::getPerson)
                .map(SrvaCallRingConfigurationService::validPhoneNumberOrNull)
                .filter(Objects::nonNull)
                .distinct()
                .collect(toList());
    }

    private static Phonenumber.PhoneNumber validPhoneNumberOrNull(final Person person) {
        try {
            final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
            final Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(person.getPhoneNumber(), "FI");
            return phoneNumberUtil.isValidNumber(phoneNumber) ? phoneNumber : null;
        } catch (NumberParseException e) {
            return null;
        }
    }

    private static List<String> getNotificationEmailList(final Organisation rhy, final Set<Occupation> contactPersons) {
        if (rhy.getEmail() != null && rhy.getEmail().contains("@")) {
            return Collections.singletonList(rhy.getEmail());
        }

        return contactPersons.stream()
                .map(Occupation::getPerson)
                .map(Person::getEmail)
                .filter(Objects::nonNull)
                .filter(email -> email.contains("@"))
                .collect(toList());
    }
}
