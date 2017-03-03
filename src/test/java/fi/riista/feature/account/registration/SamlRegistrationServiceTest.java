package fi.riista.feature.account.registration;

import com.google.common.collect.Lists;
import fi.riista.feature.account.audit.AuditService;
import fi.riista.feature.account.password.ChangePasswordService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.util.NumberGenerator;
import fi.riista.util.NumberSequence;
import fi.riista.util.ValueGeneratorMixin;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SamlRegistrationServiceTest implements ValueGeneratorMixin {

    @InjectMocks
    private SamlRegistrationService samlRegistrationService;

    @Mock
    private AuditService auditService;

    @Mock
    private ChangePasswordService changePasswordService;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private UserRepository userRepository;

    @Captor
    private ArgumentCaptor<Person> personCaptor;

    @Captor
    private ArgumentCaptor<SystemUser> userCaptor;

    private static final String EMAIL = "foo@bar.fi";
    private static final String SSN = "111111-1012";

    private SamlUserAttributes createSamlUser() {
        return SamlUserAttributes.builder()
                .withSsn(SSN)
                .withFirstNames("First-" + zeroPaddedNumber(4))
                .withByName("ByName-" + zeroPaddedNumber(4))
                .withLastName("Last-" + zeroPaddedNumber(4))
                .build();
    }

    @Override
    public NumberGenerator getNumberGenerator() {
        return NumberSequence.INSTANCE;
    }

    @Before
    public void setUp() {
        when(personRepository.findBySsn(SSN)).thenReturn(Optional.empty());
        when(personRepository.save(any(Person.class))).thenAnswer(answerFirstArgument());
        when(userRepository.save(any(SystemUser.class))).thenAnswer(answerFirstArgument());
        when(userRepository.findByPerson(any(Person.class))).thenReturn(Collections.<SystemUser>emptyList());
    }

    private static SystemUser mockSystemUser(SystemUser.Role role) {
        SystemUser user = mock(SystemUser.class);
        when(user.getRole()).thenReturn(role);
        return user;
    }

    @Test
    public void testNewPersonRegister() {
        SamlUserAttributes samlUser = createSamlUser();

        samlRegistrationService.registerUserAndPerson(EMAIL, samlUser);

        assertCommon(samlUser);
    }

    @Test
    public void testPersonAddressesNotAffectedByVtj() {
        Address mrAddress = new Address();
        Address otherAddress = new Address();

        Person person = new Person();
        person.setMrAddress(mrAddress);
        person.setOtherAddress(otherAddress);

        when(personRepository.findBySsn(SSN)).thenReturn(Optional.of(person));

        SamlUserAttributes samlUser = createSamlUser();

        samlRegistrationService.registerUserAndPerson(EMAIL, samlUser);

        assertEquals(mrAddress, person.getMrAddress());
        assertEquals(otherAddress, person.getOtherAddress());
    }

    @Test
    public void testPreviousAccountsAreDeactivated() {
        SystemUser user1 = mockSystemUser(SystemUser.Role.ROLE_USER);
        SystemUser user2 = mockSystemUser(SystemUser.Role.ROLE_ADMIN);
        when(userRepository.findByPerson(any(Person.class))).thenReturn(Lists.newArrayList(user1, user2));

        SamlUserAttributes samlUser = createSamlUser();
        samlRegistrationService.registerUserAndPerson(EMAIL, samlUser);

        verify(user1).setActive(eq(false));
        verify(user2, never()).setActive(anyBoolean());
    }

    private void assertCommon(final SamlUserAttributes samlUser) {
        verify(personRepository).save(personCaptor.capture());
        verify(userRepository).save(userCaptor.capture());

        assertPerson(EMAIL, SSN, samlUser);
        assertUser(EMAIL, samlUser);
    }

    private void assertPerson(final String email, final String ssn, final SamlUserAttributes samlUser) {
        Person person = personCaptor.getValue();
        assertEquals(email, person.getEmail());
        assertEquals(ssn, person.getSsn());
        assertEquals(samlUser.getLastName(), person.getLastName());
        assertEquals(samlUser.getFirstNames(), person.getFirstName());
        assertEquals(samlUser.getByName(), person.getByName());
    }

    private void assertUser(final String email, final SamlUserAttributes samlUser) {
        SystemUser user = userCaptor.getValue();
        assertEquals(email, user.getEmail());
        assertEquals(email, user.getUsername());
        assertEquals(samlUser.getLastName(), user.getLastName());
        assertEquals(samlUser.getFirstNames(), user.getFirstName());
    }

    private static <T> Answer<T> answerFirstArgument() {
        return invocation -> (T) invocation.getArguments()[0];
    }
}
