package fi.riista.feature.permit.application.dogevent.disturbance;

import com.google.common.collect.ImmutableSet;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.permit.application.dogevent.DogEventDisturbance;
import fi.riista.feature.permit.application.dogevent.DogEventDisturbanceContact;
import fi.riista.feature.permit.application.dogevent.DogEventDisturbanceContactRepository;
import fi.riista.feature.permit.application.dogevent.DogEventDisturbanceRepository;
import fi.riista.feature.permit.application.dogevent.DogEventType;
import fi.riista.feature.permit.application.dogevent.fixture.DogEventDisturbanceFixtureMixin;
import fi.riista.feature.permit.application.dogevent.fixture.SpeciesFixtureMixin;
import fi.riista.feature.permit.application.dogevent.fixture.SpeciesMap;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.test.TestUtils;
import fi.riista.util.DateUtil;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import javax.validation.ConstraintViolationException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static fi.riista.feature.permit.application.dogevent.DogEventType.DOG_TEST;
import static fi.riista.feature.permit.application.dogevent.DogEventType.DOG_TRAINING;
import static fi.riista.test.Asserts.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.not;

@RunWith(Theories.class)
public class DogEventDisturbanceFeatureTest extends EmbeddedDatabaseTest
        implements DogEventDisturbanceFixtureMixin, SpeciesFixtureMixin {

    @Resource
    private DogEventDisturbanceFeature feature;

    @Resource
    private DogEventDisturbanceRepository eventRepository;

    @Resource
    private DogEventDisturbanceContactRepository contactRepository;

    @DataPoints
    public static final ImmutableSet<Integer> ALLOWED_SPECIES =
            ImmutableSet.<Integer>builder()
                    .add(GameSpecies.OFFICIAL_CODE_BEAR)
                    .add(GameSpecies.OFFICIAL_CODE_LYNX)
                    .add(GameSpecies.OFFICIAL_CODE_OTTER)
                    .add(GameSpecies.OFFICIAL_CODE_WOLF)
                    .build();

    @Test(expected = AccessDeniedException .class)
    public void getEvent_unauthorized() {
        withDogDisturbanceSpecies(s -> {
            withDogEventDisturbanceFixtureWithNoEvents(s, f -> {
                onSavedAndAuthenticated(createNewUser(), () -> {
                    feature.getEvent(f.application.getId(), DOG_TEST);
                });
            });
        });
    }

    @Test
    public void getEvent_noEvents() {
        withDogDisturbanceSpecies(s -> {
            withDogEventDisturbanceFixtureWithNoEvents(s, f -> {
                onSavedAndAuthenticated(createUser(f.applicant), () -> {

                    final DogEventDisturbanceDTO actualTest = feature.getEvent(f.application.getId(), DOG_TEST);
                    final DogEventDisturbanceDTO actualTraining = feature.getEvent(f.application.getId(), DOG_TRAINING);

                    assertThat(actualTraining, is(nullValue()));
                    assertThat(actualTest, is(nullValue()));
                });
            });
        });
    }

    @Test
    public void getEvent_onlyTestEvent() {
        withDogDisturbanceSpecies(s -> {
            withDogEventDisturbanceFixtureWithOnlyTestEvent(s, f -> {
                onSavedAndAuthenticated(createUser(f.applicant), () -> {

                    final DogEventDisturbanceDTO actualTest = feature.getEvent(f.application.getId(), DOG_TEST);
                    final DogEventDisturbanceDTO actualTraining = feature.getEvent(f.application.getId(), DOG_TRAINING);

                    assertThat(actualTraining, is(nullValue()));
                    assertDtoEqualsToEntities(s, actualTest, f.testEvent, f.testContacts);
                });
            });
        });
    }

    @Test
    public void getEvent_onlyTrainingEvent() {
        withDogDisturbanceSpecies(s -> {
            withDogEventDisturbanceFixtureWithOnlyTrainingEvent(s, f -> {
                onSavedAndAuthenticated(createUser(f.applicant), () -> {

                    final DogEventDisturbanceDTO actualTest = feature.getEvent(f.application.getId(), DOG_TEST);
                    final DogEventDisturbanceDTO actualTraining = feature.getEvent(f.application.getId(), DOG_TRAINING);

                    assertDtoEqualsToEntities(s, actualTraining, f.trainingEvent, f.trainingContacts);
                    assertThat(actualTest, is(nullValue()));
                });
            });
        });
    }

    @Theory
    public void getEvent_withTrainingAndTestEvent(final int gameSpeciesCode) {
        withDogDisturbanceSpecies(s -> {
            withDogEventDisturbanceFixture(s, gameSpeciesCode, f -> {
                onSavedAndAuthenticated(createUser(f.applicant), () -> {

                    final DogEventDisturbanceDTO actualTest = feature.getEvent(f.application.getId(), DOG_TEST);
                    final DogEventDisturbanceDTO actualTraining = feature.getEvent(f.application.getId(), DOG_TRAINING);

                    assertDtoEqualsToEntities(s, actualTraining, f.trainingEvent, f.trainingContacts);
                    assertDtoEqualsToEntities(s, actualTest, f.testEvent, f.testContacts);
                });
            });
        });
    }

    @Theory
    public void getEvent_eventsDontGetMixedUp(final int gameSpeciesCode) {
        withDogDisturbanceSpecies(s -> {
            withDogEventDisturbanceFixture(s, gameSpeciesCode, f -> {
                withDogEventDisturbanceFixture(s, gameSpeciesCode, anotherFixture -> {
                    onSavedAndAuthenticated(createUser(f.applicant), () -> {

                        final DogEventDisturbanceDTO actualTest = feature.getEvent(f.application.getId(), DOG_TEST);
                        final DogEventDisturbanceDTO actualTraining = feature.getEvent(f.application.getId(), DOG_TRAINING);

                        assertDtoEqualsToEntities(s, actualTraining, f.trainingEvent, f.trainingContacts);
                        assertDtoEqualsToEntities(s, actualTest, f.testEvent, f.testContacts);
                    });
                });
            });
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void updateEvent_unauthorized() {
        withDogDisturbanceSpecies(s -> {
            withDogEventDisturbanceFixture(s, f -> {
                onSavedAndAuthenticated(createNewUser(), () -> {
                    feature.updateEvent(f.application.getId(), newDogEventDisturbanceDTO(DOG_TRAINING, 1));
                });
            });
        });
    }

    @Theory
    public void updateEvent_addNewEvent(DogEventType eventType, Integer speciesCode) {
        withDogDisturbanceSpecies(s -> {
            withDogEventDisturbanceFixtureWithNoEvents(s, f -> {
                onSavedAndAuthenticated(createUser(f.applicant), () -> {
                    final DogEventDisturbanceDTO expected = newDogEventDisturbanceDTO(eventType, 5);
                    expected.setSpeciesCode(speciesCode);

                    feature.updateEvent(f.application.getId(), expected);

                    final DogEventDisturbance actualEvent = eventRepository.findByHarvestPermitApplicationAndEventType(f.application, eventType);
                    final List<DogEventDisturbanceContact> actualContacts = contactRepository.findAllByEvent(actualEvent);

                    assertDtoEqualsToEntities(s, expected, actualEvent, actualContacts);
                });
            });
        });
    }

    @Test
    public void updateEvent_updateExistingEvent() {
        withDogDisturbanceSpecies(s -> {
            withDogEventDisturbanceFixture(s, f -> {
                onSavedAndAuthenticated(createUser(f.applicant), () -> {
                    final DogEventDisturbanceDTO expected = feature.getEvent(f.application.getId(), DOG_TRAINING);
                    expected.setDogsAmount(100);

                    feature.updateEvent(f.application.getId(), expected);

                    final DogEventDisturbance actualEvent = eventRepository.findByHarvestPermitApplicationAndEventType(f.application, DOG_TRAINING);
                    final List<DogEventDisturbanceContact> actualContacts = contactRepository.findAllByEvent(actualEvent);
                    assertDtoEqualsToEntities(s, expected, actualEvent, actualContacts);
                });
            });
        });
    }

    @Test
    public void updateEvent_addContact() {
        withDogDisturbanceSpecies(s -> {
            withDogEventDisturbanceFixture(s, f -> {
                onSavedAndAuthenticated(createUser(f.applicant), () -> {
                    final DogEventDisturbanceDTO expected = feature.getEvent(f.application.getId(), DOG_TRAINING);
                    final List<DogEventDisturbanceContactDTO> expectedContacts = expected.getContacts();
                    expectedContacts.add(newDogEventDisturbanceContactDTO());
                    expected.setContacts(expectedContacts);

                    feature.updateEvent(f.application.getId(), expected);

                    final DogEventDisturbance actualEvent = eventRepository.findByHarvestPermitApplicationAndEventType(f.application, DOG_TRAINING);
                    final List<DogEventDisturbanceContact> actualContacts = contactRepository.findAllByEvent(actualEvent);
                    assertDtoEqualsToEntities(s, expected, actualEvent, actualContacts);
                });
            });
        });
    }

    @Test
    public void updateEvent_updateExistingContact() {
        withDogDisturbanceSpecies(s -> {
            withDogEventDisturbanceFixture(s, f -> {
                onSavedAndAuthenticated(createUser(f.applicant), () -> {
                    final DogEventDisturbanceDTO expected = feature.getEvent(f.application.getId(), DOG_TRAINING);
                    final List<DogEventDisturbanceContactDTO> expectedContacts = expected.getContacts();
                    expectedContacts.get(0).setName("New Name");
                    expected.setContacts(expectedContacts);

                    feature.updateEvent(f.application.getId(), expected);

                    final DogEventDisturbance actualEvent = eventRepository.findByHarvestPermitApplicationAndEventType(f.application, DOG_TRAINING);
                    final List<DogEventDisturbanceContact> actualContacts = contactRepository.findAllByEvent(actualEvent);
                    assertDtoEqualsToEntities(s, expected, actualEvent, actualContacts);
                });
            });
        });
    }

    @Test
    public void updateEvent_deleteContact() {
        withDogDisturbanceSpecies(s -> {
            withDogEventDisturbanceFixture(s, f -> {
                onSavedAndAuthenticated(createUser(f.applicant), () -> {
                    final DogEventDisturbanceDTO expected = feature.getEvent(f.application.getId(), DOG_TRAINING);
                    final List<DogEventDisturbanceContactDTO> expectedContacts = expected.getContacts();
                    expectedContacts.remove(0);
                    expected.setContacts(expectedContacts);

                    feature.updateEvent(f.application.getId(), expected);

                    final DogEventDisturbance actualEvent = eventRepository.findByHarvestPermitApplicationAndEventType(f.application, DOG_TRAINING);
                    final List<DogEventDisturbanceContact> actualContacts = contactRepository.findAllByEvent(actualEvent);
                    assertDtoEqualsToEntities(s, expected, actualEvent, actualContacts);
                });
            });
        });
    }

    /**
     *  Data validation tests
     */

    @Test(expected = ConstraintViolationException.class)
    public void updateEvent_validate_beginDateNotSet() {
        withDogDisturbanceSpecies(s -> {
            withDogEventDisturbanceFixtureWithNoEvents(s, f -> {
                onSavedAndAuthenticated(createUser(f.applicant), () -> {
                    final DogEventDisturbanceDTO event = newDogEventDisturbanceDTO(DOG_TRAINING, 2);
                    event.setBeginDate(null);
                    feature.updateEvent(f.application.getId(), event);
                });
            });
        });
    }

    @Test(expected = ConstraintViolationException.class)
    public void updateEvent_validate_endDateNotSet() {
        withDogDisturbanceSpecies(s -> {
            withDogEventDisturbanceFixtureWithNoEvents(s, f -> {
                onSavedAndAuthenticated(createUser(f.applicant), () -> {
                    final DogEventDisturbanceDTO event = newDogEventDisturbanceDTO(DOG_TRAINING, 2);
                    event.setEndDate(null);
                    feature.updateEvent(f.application.getId(), event);
                });
            });
        });
    }

    @Test
    public void updateEvent_validate_beginDateInPast() {
        withDogDisturbanceSpecies(s -> {
            withDogEventDisturbanceFixtureWithNoEvents(s, f -> {
                onSavedAndAuthenticated(createUser(f.applicant), () -> {
                    final DogEventDisturbanceDTO event = newDogEventDisturbanceDTO(DOG_TRAINING, 2);
                    event.setBeginDate(DateUtil.today().minusDays(1));
                    feature.updateEvent(f.application.getId(), event);
                });
            });
        });
    }

    @Test(expected = ConstraintViolationException.class)
    public void updateEvent_validate_endDateBeforeBeginDate() {
        withDogDisturbanceSpecies(s -> {
            withDogEventDisturbanceFixtureWithNoEvents(s, f -> {
                onSavedAndAuthenticated(createUser(f.applicant), () -> {
                    final DogEventDisturbanceDTO event = newDogEventDisturbanceDTO(DOG_TRAINING, 2);
                    event.setBeginDate(DateUtil.today().plusDays(2));
                    event.setEndDate(DateUtil.today().plusDays(1));
                    feature.updateEvent(f.application.getId(), event);
                });
            });
        });
    }

    @Test
    public void updateEvent_validate_beginAndEndDatesAreToday() {
        withDogDisturbanceSpecies(s -> {
            withDogEventDisturbanceFixtureWithNoEvents(s, f -> {
                onSavedAndAuthenticated(createUser(f.applicant), () -> {
                    final DogEventDisturbanceDTO event = newDogEventDisturbanceDTO(DOG_TRAINING, 2);
                    event.setBeginDate(DateUtil.today());
                    event.setEndDate(DateUtil.today());
                    feature.updateEvent(f.application.getId(), event);
                });
            });
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateEvent_validate_invalidSpeciesFails() {
        withDogDisturbanceSpecies(s -> {
            withDogEventDisturbanceFixtureWithNoEvents(s, f -> {
                onSavedAndAuthenticated(createUser(f.applicant), () -> {
                    final DogEventDisturbanceDTO event = newDogEventDisturbanceDTO(DOG_TRAINING, 2);
                    event.setSpeciesCode(GameSpecies.OFFICIAL_CODE_MOOSE);
                    feature.updateEvent(f.application.getId(), event);
                });
            });
        });
    }

    @Test(expected = ConstraintViolationException.class)
    public void updateEvent_validate_eventDescriptionCannotBeEmpty() {
        withDogDisturbanceSpecies(s -> {
            withDogEventDisturbanceFixtureWithNoEvents(s, f -> {
                onSavedAndAuthenticated(createUser(f.applicant), () -> {
                    final DogEventDisturbanceDTO event = newDogEventDisturbanceDTO(DOG_TRAINING, 2);
                    event.setEventDescription("");
                    feature.updateEvent(f.application.getId(), event);
                });
            });
        });
    }

    @Test(expected = ConstraintViolationException.class)
    public void updateEvent_validate_eventDescriptionCannotContainJustSpaces() {
        withDogDisturbanceSpecies(s -> {
            withDogEventDisturbanceFixtureWithNoEvents(s, f -> {
                onSavedAndAuthenticated(createUser(f.applicant), () -> {
                    final DogEventDisturbanceDTO event = newDogEventDisturbanceDTO(DOG_TRAINING, 2);
                    event.setEventDescription("          ");
                    feature.updateEvent(f.application.getId(), event);
                });
            });
        });
    }

    @Test
    public void updateEvent_validate_skippedEvent() {
        withDogDisturbanceSpecies(s -> {
            withDogEventDisturbanceFixtureWithNoEvents(s, f -> {
                onSavedAndAuthenticated(createUser(f.applicant), () -> {
                    final DogEventDisturbanceDTO event = newSkippedDogEventDisturbanceDTO(DOG_TRAINING);
                    feature.updateEvent(f.application.getId(), event);
                });
            });
        });
    }

    @Test(expected = ConstraintViolationException.class)
    public void updateEvent_validate_whenSkipped_beginDate_mustBeNull() {
        withDogDisturbanceSpecies(s -> {
            withDogEventDisturbanceFixtureWithNoEvents(s, f -> {
                onSavedAndAuthenticated(createUser(f.applicant), () -> {
                    final DogEventDisturbanceDTO event = newSkippedDogEventDisturbanceDTO(DOG_TRAINING);
                    event.setBeginDate(DateUtil.today());
                    feature.updateEvent(f.application.getId(), event);
                });
            });
        });
    }

    @Test(expected = ConstraintViolationException.class)
    public void updateEvent_validate_whenSkipped_endDate_mustBeNull() {
        withDogDisturbanceSpecies(s -> {
            withDogEventDisturbanceFixtureWithNoEvents(s, f -> {
                onSavedAndAuthenticated(createUser(f.applicant), () -> {
                    final DogEventDisturbanceDTO event = newSkippedDogEventDisturbanceDTO(DOG_TRAINING);
                    event.setEndDate(DateUtil.today());
                    feature.updateEvent(f.application.getId(), event);
                });
            });
        });
    }

    @Test(expected = ConstraintViolationException.class)
    public void updateEvent_validate_whenSkipped_eventDescription_mustBeNull() {
        withDogDisturbanceSpecies(s -> {
            withDogEventDisturbanceFixtureWithNoEvents(s, f -> {
                onSavedAndAuthenticated(createUser(f.applicant), () -> {
                    final DogEventDisturbanceDTO event = newSkippedDogEventDisturbanceDTO(DOG_TRAINING);
                    event.setEventDescription("Should be null");
                    feature.updateEvent(f.application.getId(), event);
                });
            });
        });
    }

    @Test(expected = ConstraintViolationException.class)
    public void updateEvent_validate_whenSkipped_speciesCode_mustBeNull() {
        withDogDisturbanceSpecies(s -> {
            withDogEventDisturbanceFixtureWithNoEvents(s, f -> {
                onSavedAndAuthenticated(createUser(f.applicant), () -> {
                    final DogEventDisturbanceDTO event = newSkippedDogEventDisturbanceDTO(DOG_TRAINING);
                    event.setSpeciesCode(GameSpecies.OFFICIAL_CODE_BEAR);
                    feature.updateEvent(f.application.getId(), event);
                });
            });
        });
    }

    @Test(expected = ConstraintViolationException.class)
    public void updateEvent_validate_whenSkipped_dogsAmount_mustBeNull() {
        withDogDisturbanceSpecies(s -> {
            withDogEventDisturbanceFixtureWithNoEvents(s, f -> {
                onSavedAndAuthenticated(createUser(f.applicant), () -> {
                    final DogEventDisturbanceDTO event = newSkippedDogEventDisturbanceDTO(DOG_TRAINING);
                    event.setDogsAmount(1);
                    feature.updateEvent(f.application.getId(), event);
                });
            });
        });
    }

    @Test(expected = ConstraintViolationException.class)
    public void updateEvent_validate_contactMustHaveName() {
        withDogDisturbanceSpecies(s -> {
            withDogEventDisturbanceFixture(s, f -> {
                onSavedAndAuthenticated(createUser(f.applicant), () -> {
                    final DogEventDisturbanceDTO expected = feature.getEvent(f.application.getId(), DOG_TRAINING);
                    final DogEventDisturbanceContactDTO contact = newDogEventDisturbanceContactDTO();
                    contact.setName(null);
                    expected.setContacts(Arrays.asList(contact));

                    feature.updateEvent(f.application.getId(), expected);
                });
            });
        });
    }

    @Test(expected = ConstraintViolationException.class)
    public void updateEvent_validate_contactMustHavePhone() {
        withDogDisturbanceSpecies(s -> {
            withDogEventDisturbanceFixture(s, f -> {
                onSavedAndAuthenticated(createUser(f.applicant), () -> {
                    final DogEventDisturbanceDTO expected = feature.getEvent(f.application.getId(), DOG_TRAINING);
                    final DogEventDisturbanceContactDTO contact = newDogEventDisturbanceContactDTO();
                    contact.setPhone(null);
                    expected.setContacts(Arrays.asList(contact));

                    feature.updateEvent(f.application.getId(), expected);
                });
            });
        });
    }

    @Test
    public void updateEvent_validate_contactHasOptionalMail() {
        withDogDisturbanceSpecies(s -> {
            withDogEventDisturbanceFixture(s, f -> {
                onSavedAndAuthenticated(createUser(f.applicant), () -> {
                    final DogEventDisturbanceDTO expected = feature.getEvent(f.application.getId(), DOG_TRAINING);
                    final DogEventDisturbanceContactDTO contact = newDogEventDisturbanceContactDTO();
                    contact.setMail(null);
                    expected.setContacts(Arrays.asList(contact));

                    feature.updateEvent(f.application.getId(), expected);
                });
            });
        });
    }

    /**
     *
     *  Helper functions
     *
     */

    private void assertDtoEqualsToEntities(final SpeciesMap speciesMap,
                                           final DogEventDisturbanceDTO dto,
                                           final DogEventDisturbance entity,
                                           final List<DogEventDisturbanceContact> contactEntities) {
        // When event is been created, its DTO has no id, but stored entity has.
        if (dto.getId() == null) {
            assertThat(entity.getId(), is(not(nullValue())));
        } else {
            assertThat(dto.getId(), equalTo(entity.getId()));
        }
        assertThat(dto.getEventType(), equalTo(entity.getEventType()));
        assertThat(dto.isSkipped(), equalTo(entity.isSkipped()));
        assertThat(speciesMap.byOfficialCode(dto.getSpeciesCode()), equalTo(entity.getGameSpecies()));
        assertThat(dto.getDogsAmount(), equalTo(entity.getDogsAmount()));
        assertThat(dto.getBeginDate(), equalTo(entity.getBeginDate()));
        assertThat(dto.getEndDate(), equalTo(entity.getEndDate()));
        assertThat(dto.getEventDescription(), equalTo(entity.getEventDescription()));

        assertContactDTOsEqualsToEntities(dto.getContacts(), contactEntities);
    }

    private void assertContactDTOsEqualsToEntities(final List<DogEventDisturbanceContactDTO> dtos,
                                                   final List<DogEventDisturbanceContact> entities) {

        if (Objects.isNull(dtos)) {
            assertThat(entities, equalTo(nullValue()));
        } else {

            assertThat(dtos.size(), equalTo(entities.size()));
            for (int i = 0; i < dtos.size(); i++) {
                assertContactDtoEqualsToEntity(dtos.get(i), entities.get(i));
            }
        }
    }

    private void assertContactDtoEqualsToEntity(final DogEventDisturbanceContactDTO dto,
                                                final DogEventDisturbanceContact entity) {
        if (Objects.isNull(dto)) {
            assertThat(entity, equalTo(nullValue()));
        } else {
            // When contact is been created, its DTO has no id, but stored entity has.
            if (dto.getId() == null) {
                assertThat(entity.getId(), is(not(nullValue())));
            } else {
                assertThat(dto.getId(), equalTo(entity.getId()));
            }
            assertThat(dto.getName(), equalTo(entity.getContactName()));
            assertThat(dto.getPhone(), equalTo(entity.getContactPhone()));
            assertThat(dto.getMail(), equalTo(entity.getContactMail()));
        }
    }

    private DogEventDisturbanceDTO newDogEventDisturbanceDTO(final DogEventType eventType, final int contactNum) {
        final DogEventDisturbanceDTO event = new DogEventDisturbanceDTO();
        event.setId(null);
        event.setEventType(eventType);
        event.setSpeciesCode(GameSpecies.OFFICIAL_CODE_BEAR);
        event.setDogsAmount(nextIntBetween(1, 9999));
        event.setBeginDate(DateUtil.today().plusDays(1));
        event.setEndDate(DateUtil.today().plusDays(2));
        event.setEventDescription("Event description");
        event.setContacts(TestUtils.createList(contactNum, this::newDogEventDisturbanceContactDTO));
        return event;
    }

    private DogEventDisturbanceDTO newSkippedDogEventDisturbanceDTO(final DogEventType eventType) {
        final DogEventDisturbanceDTO event = new DogEventDisturbanceDTO();
        event.setId(null);
        event.setEventType(eventType);
        event.setSkipped(true);
        return event;
    }


    private DogEventDisturbanceContactDTO newDogEventDisturbanceContactDTO() {
        final DogEventDisturbanceContactDTO contact = new DogEventDisturbanceContactDTO();
        contact.setId(null);
        contact.setName(personName());
        contact.setPhone(phoneNumber());
        contact.setMail("contact@mail");
        return contact;
    }

}
