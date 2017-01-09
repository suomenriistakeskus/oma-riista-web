package fi.riista.feature.account.registration;

import com.github.jknack.handlebars.Handlebars;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.password.ChangePasswordService;
import fi.riista.feature.RuntimeEnvironmentUtil;
import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.feature.mail.MailService;
import fi.riista.feature.mail.token.EmailToken;
import fi.riista.feature.mail.token.EmailTokenService;
import fi.riista.feature.mail.token.EmailTokenType;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.vetuma.VetumaConfig;
import fi.riista.feature.vetuma.dto.VetumaLoginRequestDTO;
import fi.riista.feature.vetuma.entity.VetumaTransaction;
import fi.riista.feature.vetuma.entity.VetumaTransactionStatus;
import fi.riista.feature.vetuma.repository.VetumaTransactionRepository;
import fi.riista.util.Locales;
import fi.riista.util.Localiser;
import org.joda.time.DateTime;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Component
public class RegisterAccountFeature {

    private static final String TEMPLATE_REGISTER_ACCOUNT = "register_account";
    private static final String TEMPLATE_REGISTER_ACCOUNT_SV = "register_account.sv";

    @Resource
    private ChangePasswordService changePasswordService;

    @Resource
    private EmailTokenService emailTokenService;

    @Resource
    private MailService mailService;

    @Resource
    private Handlebars handlebars;

    @Resource
    private RuntimeEnvironmentUtil runtimeEnvironmentUtil;

    @Resource
    private MessageSource messageSource;

    @Resource
    private VetumaConfig vetumaConfig;

    @Resource
    private VetumaTransactionRepository vetumaTransactionRepository;

    @Transactional
    public void sendEmail(final RegisterAccountDTO dto, final HttpServletRequest request) {
        // Forbid using internal Riistakeskus email addresses
        if (dto.getEmail().endsWith("riista.fi")) {
            throw new IllegalArgumentException("Invalid email domain");
        }

        final String token = emailTokenService.allocateToken(
                EmailTokenType.VERIFY_EMAIL, null, dto.getEmail(), request);

        final URI emailLink = UriComponentsBuilder.fromUri(runtimeEnvironmentUtil.getBackendBaseUri())
                .path("/")
                .fragment("/register/from-email/{token}?lang={lang}")
                .buildAndExpand(token, dto.getLanguageCode())
                .toUri();

        // Unregistered user does not have locale preference set in the system
        //  -> determine locale otherwise
        final Locale locale = MoreObjects.firstNonNull(LocaleContextHolder.getLocale(), Locales.FI);

        final String subject = messageSource.getMessage("registration.email.title", null, locale);

        final Map<String, Object> params = Collections.singletonMap("registerLink", emailLink.toString());

        mailService.sendImmediate(new MailMessageDTO.Builder()
                .withTo(dto.getEmail())
                .withSubject(subject)
                .withHandlebarsBody(handlebars, selectTemplate(), params));
    }

    private static String selectTemplate() {
        return Localiser.select(TEMPLATE_REGISTER_ACCOUNT, TEMPLATE_REGISTER_ACCOUNT_SV);
    }

    @Transactional
    public Map<String, Object> fromEmail(final EmailVerificationDTO dto,
                                         final HttpServletRequest request) {
        // Decrypt and validate token data
        final EmailToken emailToken = emailTokenService.validate(dto.getToken());
        final VetumaTransaction vetumaTransaction = new VetumaTransaction(emailToken, request);
        vetumaTransactionRepository.save(vetumaTransaction);

        // Store token data in session so that it is available after return from Vetuma
        // Secure data from email token
        return ImmutableMap.<String, Object>builder()
                .put("vetumaLoginUrl", vetumaConfig.getVetumaLoginUrl())
                // Signed data to be used as POST data when redirecting to Vetuma for authentication
                .put("vetumaConnection", VetumaLoginRequestDTO.create(
                        vetumaConfig, vetumaTransaction.getId(), dto.getLang(), DateTime.now()))
                .put("languageCode", dto.getLang())
                .put("status", "ok")
                .build();
    }

    @Transactional(readOnly = true)
    public CompleteRegistrationDTO complete(final CompleteRegistrationRequestDTO request) {
        final VetumaTransaction vetumaTransaction = vetumaTransactionRepository.getOne(request.getTrid());
        vetumaTransaction.assertTransactionStatus(VetumaTransactionStatus.SUCCESS);

        final CompleteRegistrationDTO dto = new CompleteRegistrationDTO();
        dto.setTransaction(request.getTrid());

        final SystemUser user = Preconditions.checkNotNull(vetumaTransaction.getUser(), "user is missing");

        if (user.getPerson() != null) {
            final Person person = user.getPerson();

            dto.setFirstName(person.getFirstName());
            dto.setByName(person.getByName());
            dto.setLastName(person.getLastName());
            dto.setSsn(person.getSsn());
            dto.setHomeMunicipality(person.getHomeMunicipalityName().getAnyTranslation(user.getLocale()));
            dto.setFinnishCitizen(person.isFinnishCitizen());
            dto.setLanguageCode(person.getLanguageCode());
            dto.setPhoneNumber(person.getPhoneNumber());

            dto.setAddressEditable(person.isAddressEditable());

            final Address address = person.getAddress();

            if (address != null) {
                dto.setStreetAddress(address.getStreetAddress());
                dto.setPostalCode(address.getPostalCode());
                dto.setCity(address.getCity());
                dto.setCountry(address.getCountry());
            }

        } else {
            throw new IllegalStateException("No person linked with user");
        }

        return dto;
    }

    @Transactional
    public void completeAccountRegistration(final CompleteRegistrationDTO dto, final HttpServletRequest request) {
        final VetumaTransaction vetumaTransaction = vetumaTransactionRepository.getOne(dto.getTransaction());
        vetumaTransaction.setStatusFinished();

        final SystemUser user = Objects.requireNonNull(vetumaTransaction.getUser());
        user.setActive(true);

        emailTokenService.revoke(vetumaTransaction.getEmailToken(), request);
        changePasswordService.setUserPassword(user, dto.getPassword());

        final Person person = user.getPerson();

        if (person == null) {
            throw new IllegalStateException("No person for user");
        }

        person.setByName(dto.getByName());

        if (person.isAddressEditable()) {
            if (person.getOtherAddress() == null) {
                person.setOtherAddress(new Address());
            }
            final Address manualAddress = person.getOtherAddress();
            manualAddress.setStreetAddress(dto.getStreetAddress());
            manualAddress.setPostalCode(dto.getPostalCode());
            manualAddress.setCity(dto.getCity());
            manualAddress.setCountry(dto.getCountry());
        }

        if (StringUtils.hasText(dto.getPhoneNumber())) {
            person.setPhoneNumber(dto.getPhoneNumber());
        }
    }
}
