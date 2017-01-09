package fi.riista.feature.vetuma;

import com.google.common.collect.Lists;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.account.password.ChangePasswordService;
import fi.riista.feature.account.audit.AuditService;
import fi.riista.feature.common.repository.MunicipalityRepository;
import fi.riista.feature.RuntimeEnvironmentUtil;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.feature.vetuma.support.VtjData;
import fi.riista.util.Locales;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
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
public class VetumaLoginFeatureTest {

    @InjectMocks
    private VetumaLoginFeature vetumaLoginFeature;

    @Mock
    private PersonRepository personRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ChangePasswordService changePasswordService;
    @Mock
    private RuntimeEnvironmentUtil runtimeEnvironmentUtil;
    @Mock
    private MunicipalityRepository municipalityRepository;
    @Mock
    private AuditService auditService;

    @Captor
    private ArgumentCaptor<Person> personCaptor;
    @Captor
    private ArgumentCaptor<SystemUser> userCaptor;

    private static final String EMAIL = "foo@bar.fi";
    private static final String SSN = "111111-1012";

    private static final boolean FINNISH_CITIZEN = true;
    private static final boolean NOT_FINNISH_CITIZEN = false;

    @Before
    public void setUp() {
        when(personRepository.findBySsn(SSN)).thenReturn(Optional.empty());
        when(personRepository.save(any(Person.class))).thenAnswer(answerFirstArgument());
        when(userRepository.save(any(SystemUser.class))).thenAnswer(answerFirstArgument());
        when(userRepository.findByPerson(any(Person.class))).thenReturn(Collections.<SystemUser> emptyList());
        when(runtimeEnvironmentUtil.getDefaultLocale()).thenReturn(Locales.FI);
        when(municipalityRepository.findOne(any(String.class))).thenReturn(null);
    }

    @Test
    public void testPreviousAccountsAreDeactivated() {
        SystemUser user1 = mockSystemUser(SystemUser.Role.ROLE_USER);
        SystemUser user2 = mockSystemUser(SystemUser.Role.ROLE_ADMIN);
        when(userRepository.findByPerson(any(Person.class))).thenReturn(Lists.newArrayList(user1, user2));

        VtjData vtjData = createVtjData("fi", FINNISH_CITIZEN);
        vetumaLoginFeature.handleSuccess(EMAIL, SSN, vtjData);

        verify(user1).setActive(eq(false));
        verify(user2, never()).setActive(anyBoolean());
    }

    private static SystemUser mockSystemUser(SystemUser.Role role) {
        SystemUser user = mock(SystemUser.class);
        when(user.getRole()).thenReturn(role);
        return user;
    }

    @Test
    public void testNewFinnishSpeakingRegisters() {
        VtjData vtjData = createVtjData("fi", FINNISH_CITIZEN);

        vetumaLoginFeature.handleSuccess(EMAIL, SSN, vtjData);

        assertCommon(vtjData);
    }

    @Test
    public void testNewSwedishSpeakingRegisters() {
        VtjData vtjData = createVtjData("sv", FINNISH_CITIZEN);

        vetumaLoginFeature.handleSuccess(EMAIL, SSN, vtjData);

        assertCommon(vtjData);
    }

    @Test
    public void testNewOtherSpeakingRegisters() {
        VtjData vtjData = createVtjData("99", NOT_FINNISH_CITIZEN);

        vetumaLoginFeature.handleSuccess(EMAIL, SSN, vtjData);

        assertCommon(vtjData);
    }

    @Test
    public void testPersonAddressesNotAffectedByVtj() {
        Address mrAddress = new Address();
        Address otherAddress = new Address();

        Person person = new Person();
        person.setMrAddress(mrAddress);
        person.setOtherAddress(otherAddress);

        when(personRepository.findBySsn(SSN)).thenReturn(Optional.of(person));

        VtjData vtjData = createVtjData("fi", FINNISH_CITIZEN);

        vetumaLoginFeature.handleSuccess(EMAIL, SSN, vtjData);

        assertEquals(mrAddress, person.getMrAddress());
        assertEquals(otherAddress, person.getOtherAddress());
    }

    private void assertCommon(VtjData vtjData) {
        verify(personRepository).save(personCaptor.capture());
        verify(userRepository).save(userCaptor.capture());

        assertPerson(EMAIL, SSN, vtjData);
        assertUser(EMAIL, vtjData);
    }

    private void assertPerson(String email, String ssn, VtjData vtjData) {
        Person person = personCaptor.getValue();
        assertEquals(email, person.getEmail());
        assertEquals(ssn, person.getSsn());
        assertEquals(vtjData.getSukunimi(), person.getLastName());
        assertEquals(vtjData.getEtunimet(), person.getFirstName());
        assertEquals(vtjData.isSuomenKansalainen(), person.isFinnishCitizen());
        assertEquals(vtjData.getKielikoodi(), person.getLanguageCode());
        assertEquals(vtjData.getKuntanumero(), person.getHomeMunicipalityCode());
    }

    private void assertUser(String email, VtjData vtjData) {
        SystemUser user = userCaptor.getValue();
        assertEquals(email, user.getEmail());
        assertEquals(email, user.getUsername());
        assertEquals(vtjData.getSukunimi(), user.getLastName());
        assertEquals(vtjData.getEtunimet(), user.getFirstName());
        if (vtjData.getKielikoodi().startsWith("s")) {
            assertEquals(Locales.SV_LANG, user.getLocale().getLanguage());
        } else {
            assertEquals(Locales.FI_LANG, user.getLocale().getLanguage());
        }
    }

    private static VtjData createVtjData(String kielikoodi, boolean isSuomenKansalainen) {
        VtjData vtjData = new VtjData();
        vtjData.setKielikoodi(kielikoodi);
        vtjData.setSuomenKansalainen(isSuomenKansalainen);
        vtjData.setEtunimet("Etu");
        vtjData.setSukunimi("Suku");
        vtjData.setKuntanumero("123");

        vtjData.setKuntaS("kunta");
        vtjData.setLahiosoiteS("lähiosoite");
        vtjData.setPostitoimipaikkaS("postitoimipaikka");

        vtjData.setKuntaR("kunta ruotsi");
        vtjData.setLahiosoiteR("lähiosoite ruotsi");
        vtjData.setPostitoimipaikkaR("postitoimipaikka ruotsi");

        return vtjData;
    }

    private static <T> Answer<T> answerFirstArgument() {
        return new Answer<T>() {
            @SuppressWarnings("unchecked")
            @Override
            public T answer(InvocationOnMock invocation) {
                return (T) invocation.getArguments()[0];
            }
        };
    }
}
