package fi.riista.feature.account.registration;

import com.google.common.collect.ImmutableMap;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.mail.token.EmailToken;
import fi.riista.feature.mail.token.EmailTokenRepository;
import fi.riista.feature.mail.token.EmailTokenType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class RegisterAccountFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private RegisterAccountFeature registerAccountFeature;

    @Resource
    private UserRepository userRepository;

    @Resource
    private PersonRepository personRepository;

    @Resource
    private VetumaTransactionRepository vetumaTransactionRepository;

    @Resource
    private EmailTokenRepository emailTokenRepository;

    private MockHttpServletRequest request;
    private String ssn;
    private String firstNames;
    private String lastName;
    private String byName;
    private SamlUserAttributes samlUser;

    @Before
    public void before() {
        this.request = new MockHttpServletRequest();
        this.request.setRemoteAddr("192.168.1.2");

        this.ssn = ssn();
        this.firstNames = "FirstName-" + zeroPaddedNumber(3);
        this.byName = "ByName-" + zeroPaddedNumber(3);
        this.lastName = "LastName-" + zeroPaddedNumber(3);

        this.samlUser = new SamlUserAttributesParser(ImmutableMap.<String, List<String>>builder()
                .put(SamlUserAttributesParser.KEY_SSN, singletonList(ssn))
                .put(SamlUserAttributesParser.KEY_FIRST_NAMES, singletonList(firstNames))
                .put(SamlUserAttributesParser.KEY_BY_NAME, singletonList(byName))
                .put(SamlUserAttributesParser.KEY_LAST_NAME, singletonList(lastName))
                .build()).parse();
    }

    @Test
    public void testNormalLoginFlow() {
        final String email = "firstname.lastname@invalid";
        final EmailVerificationResponseDTO fromEmailResult = assertCanStartRegistration(email);

        // Verify nothing persisted yet
        runInTransaction(() -> {
            assertEquals(0, userRepository.count());
            assertEquals(0, personRepository.count());
        });

        final SamlAuthenticationResult authenticationResult =
                new SamlAuthenticationResult(samlUser, fromEmailResult.getTrid());

        registerAccountFeature.fromSso(authenticationResult);

        // Verify SSO response persisted
        assertSsoResponsePersisted(email);

        final CompleteRegistrationRequestDTO completeRequestDTO = new CompleteRegistrationRequestDTO();
        completeRequestDTO.setTrid(fromEmailResult.getTrid());

        final CompleteRegistrationDTO completeDTO =
                registerAccountFeature.startCompleteRegistration(completeRequestDTO);
        assertTrue(completeDTO.isAddressEditable());

        assertCanCompleteRegistration(completeDTO, email);
    }

    @Test
    public void testExistingUserRegisterWithDifferentEmail() {
        final Person existingPerson = model().newPerson();
        existingPerson.setSsn(ssn);
        final SystemUser existingUser = model().newUser(existingPerson);

        persistInNewTransaction();

        final String email = "other." + existingUser.getEmail();
        final EmailVerificationResponseDTO fromEmailResult = assertCanStartRegistration(email);

        runInTransaction(() -> {
            final SystemUser user = userRepository.getOne(existingUser.getId());
            assertTrue(user.isActive());
        });

        final SamlAuthenticationResult authenticationResult =
                new SamlAuthenticationResult(samlUser, fromEmailResult.getTrid());

        registerAccountFeature.fromSso(authenticationResult);

        // Verify SSO response persisted
        assertSsoResponsePersisted(email);

        // Make sure existing user is deactivated
        runInTransaction(() -> {
            final SystemUser user1 = userRepository.getOne(existingUser.getId());
            assertFalse(user1.isActive());

            final SystemUser user2 = userRepository.findByUsernameIgnoreCase(email);
            assertEquals(user1.getPerson().getId(), user2.getPerson().getId());
            assertEquals(user1.getPerson().getSsn(), user2.getPerson().getSsn());
        });

        final CompleteRegistrationRequestDTO completeRequestDTO = new CompleteRegistrationRequestDTO();
        completeRequestDTO.setTrid(fromEmailResult.getTrid());

        final CompleteRegistrationDTO completeDTO =
                registerAccountFeature.startCompleteRegistration(completeRequestDTO);
        assertTrue(completeDTO.isAddressEditable());

        assertCanCompleteRegistration(completeDTO, email);
    }


    private EmailVerificationResponseDTO assertCanStartRegistration(final String email) {
        final RegisterAccountDTO sendEmailDto = new RegisterAccountDTO();
        sendEmailDto.setEmail(email);
        sendEmailDto.setLang("fi");

        final String emailToken = registerAccountFeature.sendEmail(sendEmailDto, request);

        final EmailVerificationDTO emailDto = new EmailVerificationDTO();
        emailDto.setToken(emailToken);
        emailDto.setLang("fi");

        final EmailVerificationResponseDTO fromEmailResult = registerAccountFeature.fromEmail(emailDto, request);

        assertEquals("ok", fromEmailResult.getStatus());
        assertTrue(VetumaTransactionService.isValidTransactionId(fromEmailResult.getTrid()));

        return fromEmailResult;
    }

    private void assertSsoResponsePersisted(final String email) {
        runInTransaction(() -> {
            final SystemUser user = userRepository.findByUsernameIgnoreCase(email);
            assertEquals(firstNames, user.getFirstName());
            assertEquals(lastName, user.getLastName());
            assertEquals(SystemUser.Role.ROLE_USER, user.getRole());
            assertFalse(user.isActive());

            final Person person = user.getPerson();
            assertEquals(ssn, person.getSsn());
            assertEquals(firstNames, person.getFirstName());
            assertEquals(lastName, person.getLastName());
            assertEquals(byName, person.getByName());
        });
    }

    private void assertCanCompleteRegistration(final CompleteRegistrationDTO completeDTO,
                                               final String email) {
        completeDTO.setFirstName(completeDTO.getFirstName() + "A");
        completeDTO.setLastName(completeDTO.getLastName() + "B");
        completeDTO.setByName(completeDTO.getByName() + "C");
        completeDTO.setPassword(UUID.randomUUID().toString());
        completeDTO.setLang("sv");
        completeDTO.setStreetAddress("Street " + zeroPaddedNumber(2));
        completeDTO.setPhoneNumber(phoneNumber());
        completeDTO.setCity("City-" + zeroPaddedNumber(3));
        completeDTO.setPostalCode(zeroPaddedNumber(5));
        completeDTO.setCountry("Country-" + zeroPaddedNumber(3));

        runInTransaction(() -> {
            final VetumaTransaction tx = vetumaTransactionRepository.getOne(completeDTO.getTrid());
            assertEquals(VetumaTransactionStatus.SUCCESS, tx.getStatus());
            assertNotNull(tx.getUser());
            assertEquals(email, tx.getUser().getEmail());
            assertEquals(request.getRemoteAddr(), tx.getRemoteAddress());
        });

        registerAccountFeature.completeAccountRegistration(completeDTO, request);

        // Verify user modifications persisted
        runInTransaction(() -> {
            final SystemUser user = userRepository.findByUsernameIgnoreCase(email);
            assertEquals(email, user.getEmail());
            assertEquals(firstNames, user.getFirstName());
            assertEquals(lastName, user.getLastName());
            assertEquals(SystemUser.Role.ROLE_USER, user.getRole());
            assertEquals("sv_FI", user.getLocale().toString());
            assertTrue(user.isActive());

            final Person person = user.getPerson();

            assertEquals(ssn, person.getSsn());
            assertEquals(email, person.getEmail());
            assertEquals(firstNames, person.getFirstName());
            assertEquals(lastName, person.getLastName());
            assertEquals(completeDTO.getByName(), person.getByName());

            assertNull(person.getMrAddress());
            assertNotNull(person.getOtherAddress());

            assertEquals(completeDTO.getStreetAddress(), person.getOtherAddress().getStreetAddress());
            assertEquals(completeDTO.getCity(), person.getOtherAddress().getCity());
            assertEquals(completeDTO.getPostalCode(), person.getOtherAddress().getPostalCode());
            assertEquals(completeDTO.getCountry(), person.getOtherAddress().getCountry());
            assertEquals("sv", person.getLanguageCode());

            final VetumaTransaction tx = vetumaTransactionRepository.getOne(completeDTO.getTrid());
            assertEquals(VetumaTransactionStatus.FINISHED, tx.getStatus());
            assertEquals(user, tx.getUser());
            assertEquals(request.getRemoteAddr(), tx.getRemoteAddress());
            assertNotNull(tx.getEndTime());

            final EmailToken emailToken = emailTokenRepository.getOne(tx.getEmailToken());
            assertNotNull(emailToken.getRevokedAt());
            assertEquals(EmailTokenType.VERIFY_EMAIL, emailToken.getTokenType());
        });
    }

}
