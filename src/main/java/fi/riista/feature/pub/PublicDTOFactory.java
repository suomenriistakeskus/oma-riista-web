package fi.riista.feature.pub;

import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.gis.GISWGS84Point;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.address.AddressDTO;
import fi.riista.feature.organization.address.NullSafeAddress;
import fi.riista.feature.organization.calendar.CalendarEventType;
import fi.riista.feature.organization.calendar.Venue;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.pub.calendar.PublicCalendarEventDTO;
import fi.riista.feature.pub.calendar.PublicCalendarEventTypeDTO;
import fi.riista.feature.pub.calendar.PublicVenueDTO;
import fi.riista.feature.pub.occupation.PublicOccupationDTO;
import fi.riista.feature.pub.occupation.PublicOccupationTypeDTO;
import fi.riista.feature.pub.occupation.PublicOrganisationDTO;
import fi.riista.util.LocalisedString;
import fi.riista.util.Localiser;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;

@Component
public class PublicDTOFactory {

    @Resource
    private EnumLocaliser enumLocaliser;

    private LocalisedString rhyNumberString;

    private static final LocalisedString rkaReplacePattern = new LocalisedString("Suomen riistakeskus,", "Finlands viltcentral,");

    @PostConstruct
    protected void init() {
        rhyNumberString = enumLocaliser.getLocalisedString("rhyNumber");
    }

    public static PublicOccupationDTO createOrganisationWithSubOrganisations(final Occupation occupation, final PublicOccupationTypeDTO occType) {
        final Person person = occupation.getPerson();
        final String personName = String.format("%s %s", person.getByName(), person.getLastName());

        final Organisation org = occupation.getOrganisation();
        final PublicOccupationDTO dto = new PublicOccupationDTO(occType, org.getId(), personName);

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

    public PublicOrganisationDTO createOrganisationWithSubOrganisations(final Organisation organisation) {
        return createOrganisation(organisation, true, emptyMap(), emptyMap());
    }

    public PublicOrganisationDTO createOrganisationWithSubOrganisations(
            final Organisation organisation,
            final Map<Long, GISWGS84Point> locations,
            final Map<Organisation, Occupation> coordinators) {
        return createOrganisation(organisation, true, locations, coordinators);
    }

    public PublicOrganisationDTO createWithoutSuborganisations(
            final Organisation organisation, final Map<Long, GISWGS84Point> locations,
            final Map<Organisation, Occupation> coordinators) {
        return createOrganisation(organisation, false, locations,coordinators);
    }

    public PublicOrganisationDTO createWithoutSuborganisations(final Organisation organisation) {
        return createOrganisation(organisation, false, emptyMap(), emptyMap());
    }

    private PublicOrganisationDTO createOrganisation(
            final Organisation organisation,
            final boolean includeSubOrganisations,
            final Map<Long, GISWGS84Point> locations,
            final Map<Organisation, Occupation> coordinators) {
        final PublicOrganisationDTO dto = new PublicOrganisationDTO();

        dto.setId(organisation.getId());

        dto.setName(PublicDTOFactory.getOrganisationName(organisation));
        dto.setOrganisationType(organisation.getOrganisationType());
        dto.setOfficialCode(organisation.getOfficialCode());

        if (organisation.getOrganisationType() == OrganisationType.RHY) {
            dto.setRhyNumberString(getRhyNumberString(organisation));
        }

        dto.setPhoneNumber(organisation.getPhoneNumber());
        dto.setEmail(organisation.getEmail());
        dto.setAddress(AddressDTO.from(organisation.getAddress()));

        if (dto.getAddress() == null || dto.getPhoneNumber() == null || dto.getEmail() == null) {
            if (coordinators.containsKey(organisation)) {
                final Person coordinatorPerson = coordinators.get(organisation).getPerson();

                if (dto.getAddress() == null) {
                    dto.setAddress(AddressDTO.from(coordinatorPerson.getAddress()));
                }

                if (dto.getPhoneNumber() == null) {
                    dto.setPhoneNumber(coordinatorPerson.getPhoneNumber());
                }

                if (dto.getEmail() == null) {
                    dto.setEmail(coordinatorPerson.getEmail());
                }
            }
        }

        if (includeSubOrganisations) {
            List<PublicOrganisationDTO> subOrgs = organisation.getSubOrganisations().stream()
                    .filter(Organisation::isActive)
                    .map(org -> createWithoutSuborganisations(org, locations, coordinators))
                    .sorted(Comparator.comparing(PublicOrganisationDTO::getName))
                    .collect(Collectors.toList());
            dto.setSubOrganisations(subOrgs);
        }

        final GISWGS84Point loc = locations.get(organisation.getId());
        if (loc != null) {
            dto.setLatitude(loc.getLatitude());
            dto.setLongitude(loc.getLongitude());
        }


        return dto;
    }

    public PublicCalendarEventDTO create(final String calendarEventId,
                                         final PublicCalendarEventTypeDTO publicCalendarEventTypeDTO,
                                         final String name,
                                         final String description,
                                         final LocalDate date,
                                         final LocalTime beginTime,
                                         final LocalTime endTime,
                                         final Organisation organisation,
                                         final Venue venue) {
        final PublicCalendarEventDTO dto = new PublicCalendarEventDTO();

        dto.setId(calendarEventId);
        dto.setCalendarEventType(publicCalendarEventTypeDTO);
        dto.setName(name);
        dto.setDescription(description);
        dto.setDate(date);
        dto.setBeginTime(beginTime);
        dto.setEndTime(endTime);
        dto.setOrganisation(createWithoutSuborganisations(organisation));
        dto.setVenue(PublicVenueDTO.create(venue));

        return dto;
    }

    private static String getOrganisationName(final Organisation organisation) {
        String name = Localiser.select(organisation.getNameLocalisation());

        if (organisation.getOrganisationType() == OrganisationType.RKA) {
            name = name.replaceFirst(Localiser.select(rkaReplacePattern), "");
        }

        return name.trim();
    }

    private String getRhyNumberString(final Organisation organisation) {
        return getRhyNumberString(organisation.getOfficialCode());
    }

    private String getRhyNumberString(final String rhyNumber) {
        return String.format("%s %s", Localiser.select(rhyNumberString), rhyNumber);
    }

    public PublicCalendarEventTypeDTO create(final CalendarEventType calendarEventType) {
        final String name = enumLocaliser.getTranslation(calendarEventType);
        return new PublicCalendarEventTypeDTO(calendarEventType, name);
    }

    public PublicOccupationTypeDTO create(final OccupationType occType, final OrganisationType orgType) {
        final String name = enumLocaliser.getTranslation(getLocalisationKey(occType, orgType));
        return new PublicOccupationTypeDTO(name, occType, orgType);
    }

    private static String getLocalisationKey(final OccupationType occType, final OrganisationType orgType) {
        return String.format("%s.%s.%s", PublicOccupationTypeDTO.class.getSimpleName(), orgType, occType);
    }
}
