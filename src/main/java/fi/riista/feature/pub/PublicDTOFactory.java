package fi.riista.feature.pub;

import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.address.AddressDTO;
import fi.riista.feature.organization.address.NullSafeAddress;
import fi.riista.feature.organization.calendar.CalendarEvent;
import fi.riista.feature.organization.calendar.CalendarEventType;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.pub.calendar.PublicCalendarEventDTO;
import fi.riista.feature.pub.calendar.PublicCalendarEventTypeDTO;
import fi.riista.feature.pub.calendar.PublicVenueDTO;
import fi.riista.feature.pub.occupation.PublicOccupationDTO;
import fi.riista.feature.pub.occupation.PublicOccupationTypeDTO;
import fi.riista.feature.pub.occupation.PublicOrganisationDTO;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import fi.riista.util.Locales;
import fi.riista.util.LocalisedString;
import fi.riista.util.Localiser;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Component
public class PublicDTOFactory {

    @Resource
    private MessageSource messageSource;

    private LocalisedString rhyNumberString;

    @PostConstruct
    protected void init() {
        String rhyNumberInFinnish = messageSource.getMessage("rhyNumber", null, Locales.FI);
        String rhyNumberInSwedish = messageSource.getMessage("rhyNumber", null, Locales.SV);
        rhyNumberString = LocalisedString.of(rhyNumberInFinnish, rhyNumberInSwedish);
    }

    public static PublicOccupationDTO create(Occupation occupation, PublicOccupationTypeDTO occType) {
        Person person = occupation.getPerson();
        String personName = String.format("%s %s", person.getByName(), person.getLastName());

        Organisation org = occupation.getOrganisation();
        PublicOccupationDTO dto = new PublicOccupationDTO(occType, org.getId(), personName);

        dto.setEmail(person.getEmail());
        dto.setPhoneNumber(person.getPhoneNumber());
        dto.setAdditionalInfo(occupation.getAdditionalInfo());

        final OccupationType occupationType = occupation.getOccupationType();
        final NullSafeAddress address = NullSafeAddress.of(person.getAddress());
        if (occupationType == OccupationType.TOIMINNANOHJAAJA) {
            if (StringUtils.isNotBlank(org.getEmail())) {
                dto.setEmail(org.getEmail());
            }
        } else if (occupationType == OccupationType.SRVA_YHTEYSHENKILO) {
            dto.setCallOrder(occupation.getCallOrder());
            dto.setEmail(null);
            dto.setCity(address.getCity());
        } else if (occupationType == OccupationType.PETOYHDYSHENKILO) {
            dto.setCity(address.getCity());
        } else if (occupationType == OccupationType.JALJESTYSKOIRAN_OHJAAJA_HIRVI
                || occupationType == OccupationType.JALJESTYSKOIRAN_OHJAAJA_PIENET_HIRVIELAIMET
                || occupationType == OccupationType.JALJESTYSKOIRAN_OHJAAJA_SUURPEDOT) {
            dto.setCity(address.getCity());
        }
        return dto;
    }

    public PublicOrganisationDTO create(Organisation organisation) {
        return create(organisation, true);
    }

    public PublicOrganisationDTO createWithoutSuborganisations(Organisation organisation) {
        return create(organisation, false);
    }

    private PublicOrganisationDTO create(Organisation organisation, boolean includeSubOrganisations) {
        PublicOrganisationDTO dto = new PublicOrganisationDTO();

        dto.setId(organisation.getId());
        dto.setName(PublicDTOFactory.getOrganisationName(organisation));
        dto.setOrganisationType(organisation.getOrganisationType());
        dto.setOfficialCode(organisation.getOfficialCode());

        if (organisation.getOrganisationType() == OrganisationType.RHY) {
            dto.setRhyNumberString(getRhyNumberString(organisation));
        }

        dto.setPhoneNumber(organisation.getPhoneNumber());
        dto.setEmail(organisation.getEmail());

        if (organisation.getAddress() != null) {
            dto.setAddress(AddressDTO.from(organisation.getAddress()));
        }

        if (includeSubOrganisations) {
            dto.setSubOrganisations(F.mapNonNullsToList(organisation.getSubOrganisations(), org -> create(org, false)));
        }

        return dto;
    }

    public PublicCalendarEventDTO create(CalendarEvent calendarEvent, PublicCalendarEventTypeDTO type) {
        PublicCalendarEventDTO dto = new PublicCalendarEventDTO();
        dto.setId(calendarEvent.getId());
        dto.setCalendarEventType(type);
        dto.setName(calendarEvent.getName());
        dto.setDescription(calendarEvent.getDescription());
        dto.setDate(DateUtil.toLocalDateNullSafe(calendarEvent.getDate()));
        dto.setBeginTime(calendarEvent.getBeginTime());
        dto.setEndTime(calendarEvent.getEndTime());
        dto.setOrganisation(createWithoutSuborganisations(calendarEvent.getOrganisation()));
        dto.setVenue(PublicVenueDTO.create(calendarEvent.getVenue()));
        return dto;
    }

    private static String getOrganisationName(Organisation organisation) {
        return Localiser.select(organisation.getNameLocalisation());
    }

    private String getRhyNumberString(Organisation organisation) {
        return getRhyNumberString(organisation.getOfficialCode());
    }

    private String getRhyNumberString(String rhyNumber) {
        return String.format("%s %s", Localiser.select(rhyNumberString), rhyNumber);
    }

    public PublicCalendarEventTypeDTO create(CalendarEventType calendarEventType) {
        String key = String.format("%s.%s", CalendarEventType.class.getSimpleName(), calendarEventType);
        String name = messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
        return new PublicCalendarEventTypeDTO(calendarEventType, name);
    }

    public PublicOccupationTypeDTO create(OccupationType occType, OrganisationType orgType) {
        String name =
                messageSource.getMessage(getLocalisationKey(occType, orgType), null, LocaleContextHolder.getLocale());
        return new PublicOccupationTypeDTO(name, occType, orgType);
    }

    private static String getLocalisationKey(OccupationType occType, OrganisationType orgType) {
        return String.format("%s.%s.%s", PublicOccupationTypeDTO.class.getSimpleName(), orgType, occType);
    }
}
