package fi.riista.feature.permit.application.dogevent.unleash;

import com.google.common.base.Strings;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.feature.permit.application.dogevent.DogEventUnleash;
import fi.riista.feature.permit.application.dogevent.DogEventUnleashRepository;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import javax.validation.ConstraintViolationException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static fi.riista.feature.permit.application.dogevent.DogEventType.DOG_TRAINING;
import static fi.riista.test.Asserts.assertThat;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;

public class DogEventUnleashFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private DogEventUnleashRepository repository;

    @Resource
    private DogEventUnleashFeature feature;

    private Person applicant;
    private Riistanhoitoyhdistys rhy;
    private HarvestPermitApplication application;
    private SystemUser user;

    @Before
    public void setUp() {
        applicant = model().newPerson();
        rhy = model().newRiistanhoitoyhdistys();
        application = model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.DOG_UNLEASH);
        model().newHuntingDogEventPermitApplication(application);
        application.setStatus(HarvestPermitApplication.Status.DRAFT);
        application.setPermitHolder(PermitHolder.createHolderForPerson(applicant));
        application.setContactPerson(applicant);
        user = createNewUser("applicant", applicant);
        persistInNewTransaction();
    }

    @Test(expected = AccessDeniedException.class)
    public void getEvents_unauthorized() {
        onSavedAndAuthenticated(createNewUser(), () -> {
            feature.getEvents(application.getId());
        });
    }

    @Test
    public void getEvents_noDetailsStored() {
        onAuthenticated(user, () -> {
            final List<DogEventUnleashDTO> events = feature.getEvents(application.getId());
            assertEquals(emptyList(), events);
        });
    }


    @Test
    public void getEvents_detailsFound() {
        final List<DogEventUnleash> expected = Arrays.asList(model().newDogEventUnleash(application),
                                                             model().newDogEventUnleash(application));
        onSavedAndAuthenticated(user, () -> {
            final List<DogEventUnleashDTO> actual = feature.getEvents(application.getId());
            assertDTOsEqualsToEntities(actual, expected);
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void updateEvent_unauthorized() {
        onSavedAndAuthenticated(createNewUser(), () -> {
            feature.updateEvent(application.getId(), newEventDto(1));
        });
    }

    @Test
    public void updateEvent_addNewEvents() {
        onAuthenticated(user, () -> {
            final List<DogEventUnleashDTO> expected = Arrays.asList(newEventDto(1),
                                                                    newEventDto(2));

            expected.stream().forEach(e -> feature.updateEvent(application.getId(), e));
            final List<DogEventUnleash> actual = repository.findAllByHarvestPermitApplication(application);
            assertDTOsEqualsToEntities(expected, actual);
        });
    }

    @Test
    public void updateEvent_updateExistingEvent() {
        model().newDogEventUnleash(application);
        onSavedAndAuthenticated(user, () -> {
            final List<DogEventUnleashDTO> expected = feature.getEvents(application.getId());
            expected.get(0).setDogsAmount(1);

            feature.updateEvent(application.getId(), expected.get(0));

            final List<DogEventUnleash> actual = repository.findAllByHarvestPermitApplication(application);
            assertThat(actual.size(), equalTo(1));
            assertDTOsEqualsToEntities(expected, actual);
        });
    }

    @Test
    public void deleteEvent() {
        final List<DogEventUnleash> expected = Arrays.asList(model().newDogEventUnleash(application),
                                                             model().newDogEventUnleash(application));
        final DogEventUnleash eventToBeRemoved = model().newDogEventUnleash(application);
        onSavedAndAuthenticated(user, () -> {

            feature.deleteEvent(application.getId(), eventToBeRemoved.getId());

            final List<DogEventUnleash> actual = repository.findAllByHarvestPermitApplication(application);
            assertThat(actual.size(), equalTo(2));
            assertEntitiesEquals(expected, actual);

        });
    }

    @Test(expected = AccessDeniedException.class)
    public void deleteEvent_unauthorized() {
        final DogEventUnleash event = model().newDogEventUnleash(application);
        onSavedAndAuthenticated(createNewUser(), () -> {
            feature.deleteEvent(application.getId(), event.getId());
        });
    }

    /**
     *
     *  Data validation tests
     *
     */

    @Test(expected = ConstraintViolationException.class)
    public void updateEvents_validate_beginDateNotSet() {
        onAuthenticated(user, () -> {
            final DogEventUnleashDTO event = newEventDto(1);
            event.setBeginDate(null);
            feature.updateEvent(application.getId(), event);
        });
    }

    @Test
    public void updateEvents_validate_beginDateInPast() {
        onAuthenticated(user, () -> {
            final DogEventUnleashDTO event = newEventDto(1);
            event.setBeginDate(DateUtil.today().minusDays(1));
            feature.updateEvent(application.getId(), event);
        });
    }

    @Test
    public void updateEvents_validate_beginDateToday() {
        onAuthenticated(user, () -> {
            final DogEventUnleashDTO event = newEventDto(1);
            event.setBeginDate(DateUtil.today());
            feature.updateEvent(application.getId(), event);
        });
    }

    @Test
    public void updateEvents_validate_endDateNotSet() {
        onAuthenticated(user, () -> {
            final DogEventUnleashDTO event = newEventDto(1);
            event.setEndDate(null);
            feature.updateEvent(application.getId(), event);
        });
    }

    @Test(expected = ConstraintViolationException.class)
    public void updateEvents_validate_endDateInPast() {
        onAuthenticated(user, () -> {
            final DogEventUnleashDTO event = newEventDto(1);
            event.setEndDate(DateUtil.today().minusDays(1));
            feature.updateEvent(application.getId(), event);
        });
    }

    @Test
    public void updateEvents_validate_endDateToday() {
        onAuthenticated(user, () -> {
            final DogEventUnleashDTO event = newEventDto(1);
            event.setBeginDate(DateUtil.today());
            event.setEndDate(DateUtil.today());
            feature.updateEvent(application.getId(), event);
        });
    }

    @Test(expected = ConstraintViolationException.class)
    public void updateEvents_validate_endDateBeforeStartDay() {
        onAuthenticated(user, () -> {
            final DogEventUnleashDTO event = newEventDto(1);
            event.setBeginDate(DateUtil.today().plusDays(1));
            event.setEndDate(DateUtil.today());
            feature.updateEvent(application.getId(), event);
        });
    }

    @Test(expected = ConstraintViolationException.class)
    public void updateEvents_validate_dogsAmountTooSmall() {
        onAuthenticated(user, () -> {
            final DogEventUnleashDTO event = newEventDto(0);
            feature.updateEvent(application.getId(), event);
        });
    }

    @Test(expected = ConstraintViolationException.class)
    public void updateEvents_validate_dogsAmountTooBig() {
        onAuthenticated(user, () -> {
            final DogEventUnleashDTO event = newEventDto(10000);
            feature.updateEvent(application.getId(), event);
        });
    }

    @Test(expected = ConstraintViolationException.class)
    public void updateEvents_validate_naturaAreaTooLong() {
        onAuthenticated(user, () -> {
            final DogEventUnleashDTO event = newEventDto(1);
            event.setNaturaArea(Strings.repeat("x", 256));
            feature.updateEvent(application.getId(), event);
        });
    }

    @Test(expected = ConstraintViolationException.class)
    public void updateEvents_validate_naturaAreaContainsHtml() {
        onAuthenticated(user, () -> {
            final DogEventUnleashDTO event = newEventDto(1);
            event.setNaturaArea("Area <b>code</b>");
            feature.updateEvent(application.getId(), event);
        });
    }

    @Test(expected = ConstraintViolationException.class)
    public void updateEvents_validate_eventDescriptionIsEmpty() {
        onAuthenticated(user, () -> {
            final DogEventUnleashDTO event = newEventDto(1);
            event.setEventDescription("");
            feature.updateEvent(application.getId(), event);
        });
    }

    @Test(expected = ConstraintViolationException.class)
    public void updateEvents_validate_eventDescriptionContainsHtml() {
        onAuthenticated(user, () -> {
            final DogEventUnleashDTO event = newEventDto(1);
            event.setEventDescription("Event <b>description</b>");
            feature.updateEvent(application.getId(), event);
        });
    }

    @Test(expected = ConstraintViolationException.class)
    public void updateEvents_validate_locationDescriptionIsEmpty() {
        onAuthenticated(user, () -> {
            final DogEventUnleashDTO event = newEventDto(1);
            event.setLocationDescription("");
            feature.updateEvent(application.getId(), event);
        });
    }

    @Test(expected = ConstraintViolationException.class)
    public void updateEvents_validate_locationDescriptionContainsHtml() {
        onAuthenticated(user, () -> {
            final DogEventUnleashDTO event = newEventDto(1);
            event.setLocationDescription("Location <b>description</b>");
            feature.updateEvent(application.getId(), event);
        });
    }

    @Test(expected = ConstraintViolationException.class)
    public void updateEvents_validate_contactNameIsEmpty() {
        onAuthenticated(user, () -> {
            final DogEventUnleashDTO event = newEventDto(1);
            event.setContactName("");
            feature.updateEvent(application.getId(), event);
        });
    }

    @Test(expected = ConstraintViolationException.class)
    public void updateEvents_validate_contactNameContainsHtml() {
        onAuthenticated(user, () -> {
            final DogEventUnleashDTO event = newEventDto(1);
            event.setContactName("Contact <b>name</b>");
            feature.updateEvent(application.getId(), event);
        });
    }

    @Test(expected = ConstraintViolationException.class)
    public void updateEvents_validate_contactMailHasNoAtChar() {
        onAuthenticated(user, () -> {
            final DogEventUnleashDTO event = newEventDto(1);
            event.setContactMail("contact.mail");
            feature.updateEvent(application.getId(), event);
        });
    }

    @Test(expected = ConstraintViolationException.class)
    public void updateEvents_validate_contactPhoneIsEmpty() {
        onAuthenticated(user, () -> {
            final DogEventUnleashDTO event = newEventDto(1);
            event.setContactPhone("");
            feature.updateEvent(application.getId(), event);
        });
    }

    @Test(expected = ConstraintViolationException.class)
    public void updateEvents_validate_contactPhoneContainsCharacter() {
        onAuthenticated(user, () -> {
            final DogEventUnleashDTO event = newEventDto(1);
            event.setContactPhone("+123x");
            feature.updateEvent(application.getId(), event);
        });
    }

    @Test(expected = ConstraintViolationException.class)
    public void updateEvents_validate_additionalInfoContainsHtml() {
        onAuthenticated(user, () -> {
            final DogEventUnleashDTO event = newEventDto(1);
            event.setAdditionalInfo("Additional <pre>info</pre>");
            feature.updateEvent(application.getId(), event);
        });
    }

    @Test(expected = ConstraintViolationException.class)
    public void updateEvents_validate_geoLocationIsNull() {
        onAuthenticated(user, () -> {
            final DogEventUnleashDTO event = newEventDto(1);
            event.setGeoLocation(null);
            feature.updateEvent(application.getId(), event);
        });
    }

    /**
     *
     *  Helper functions
     *
     */

    private DogEventUnleashDTO newEventDto(final int dogsAmount) {
        final DogEventUnleashDTO event = new DogEventUnleashDTO();
        event.setId(null);
        event.setEventType(DOG_TRAINING);
        event.setBeginDate(DateUtil.today().plusDays(1));
        event.setEndDate(DateUtil.today().plusDays(2));
        event.setDogsAmount(dogsAmount);
        event.setNaturaArea("Natura area code");
        event.setEventDescription("Event description");
        event.setLocationDescription("Location description");
        event.setContactName("Contact name");
        event.setContactMail("contact@mail");
        event.setContactPhone("1234567890");
        event.setAdditionalInfo("Additional info");
        event.setGeoLocation(geoLocation());
        return event;
    }

    private void assertDTOsEqualsToEntities(
            final List<DogEventUnleashDTO> dto,
            final List<DogEventUnleash> entity) {

        assertThat(entity.size(), equalTo(dto.size()));

        for (int i = 0; i < dto.size(); i++) {

            if (dto.get(i).getId() == null) {
                assertThat(entity.get(i).getId(), is(not(nullValue())));
            } else {
                assertThat(dto.get(i).getId(), equalTo(entity.get(i).getId()));
            }

            final DogEventUnleashDTO aDTO = dto.get(i);
            final DogEventUnleash anEvent = entity.get(i);

            assertThat(aDTO.getEventType(), equalTo(anEvent.getEventType()));
            assertThat(aDTO.getBeginDate(), equalTo(anEvent.getBeginDate()));
            assertThat(aDTO.getEndDate(), equalTo(anEvent.getEndDate()));
            assertThat(aDTO.getDogsAmount(), equalTo(Optional.ofNullable(anEvent.getDogsAmount()).orElse(0).intValue()));
            assertThat(aDTO.getNaturaArea(), equalTo(anEvent.getNaturaArea()));
            assertThat(aDTO.getEventDescription(), equalTo(anEvent.getEventDescription()));
            assertThat(aDTO.getLocationDescription(), equalTo(anEvent.getLocationDescription()));
            assertThat(aDTO.getLocationDescription(), equalTo(anEvent.getLocationDescription()));
            assertThat(aDTO.getContactName(), equalTo(anEvent.getContactName()));
            assertThat(aDTO.getContactMail(), equalTo(anEvent.getContactMail()));
            assertThat(aDTO.getContactPhone(), equalTo(anEvent.getContactPhone()));
            assertThat(aDTO.getAdditionalInfo(), equalTo(anEvent.getAdditionalInfo()));
            assertThat(aDTO.getGeoLocation(), equalTo(anEvent.getGeoLocation()));
        }
    }

    private void assertEntitiesEquals(final List<DogEventUnleash> expected, final List<DogEventUnleash> actual) {
        assertThat(actual.size(), equalTo(expected.size()));

        for (int i = 0; i < expected.size(); i++) {
            assertThat(expected.get(i).isEqualTo(actual.get(i)), is(true));
        }
    }
}