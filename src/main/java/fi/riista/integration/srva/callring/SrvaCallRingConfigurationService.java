package fi.riista.integration.srva.callring;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.OrganisationRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;
import static java.util.stream.Collectors.toList;

@Component
public class SrvaCallRingConfigurationService {

    @Resource
    private OrganisationRepository organisationRepository;

    @Resource
    private OccupationRepository occupationRepository;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<SrvaCallRingConfiguration> generateConfigurationForEveryRhy() {
        final List<Organisation> rhyList = organisationRepository.findByOrganisationType(
                EnumSet.of(OrganisationType.RHY));

        final Map<Long, Set<Occupation>> allSrvaOccupations =
                occupationRepository.findActiveByOccupationTypeGroupByOrganisationId(OccupationType.SRVA_YHTEYSHENKILO);

        final Map<Long, Set<Occupation>> allContactPersons =
                occupationRepository.findActiveByOccupationTypeGroupByOrganisationId(OccupationType.TOIMINNANOHJAAJA);

        return rhyList.stream().map(rhy -> {
            final Set<Occupation> srva = allSrvaOccupations.getOrDefault(rhy.getId(), Collections.emptySet());
            final Set<Occupation> contactPersons = allContactPersons.getOrDefault(rhy.getId(), Collections.emptySet());

            final List<Phonenumber.PhoneNumber> phoneNumberList = getCallRingPhoneNumbers(srva);
            final List<String> notificationEmailList = getNotificationEmailList(rhy, contactPersons);

            // Repeat all phone numbers twice as fallback
            final List<Phonenumber.PhoneNumber> repeatedPhoneNumberList =
                    Stream.concat(phoneNumberList.stream(), phoneNumberList.stream()).collect(toList());

            return new SrvaCallRingConfiguration(rhy.getOfficialCode(), repeatedPhoneNumberList, notificationEmailList);

        }).collect(toList());
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
