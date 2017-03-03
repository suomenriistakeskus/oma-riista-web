package fi.riista.feature.account.registration;

import com.google.common.base.Joiner;
import fi.riista.feature.account.password.ChangePasswordService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.mail.token.EmailToken;
import fi.riista.feature.mail.token.EmailTokenService;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.Locales;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

@Component
public class RegisterAccountFeature {
    private static final Logger LOG = LoggerFactory.getLogger(RegisterAccountFeature.class);

    @Resource
    private ChangePasswordService changePasswordService;

    @Resource
    private SamlRegistrationService samlRegistrationService;

    @Resource
    private RegisterAccountEmailService registerAccountEmailService;

    @Resource
    private VetumaTransactionService vetumaTransactionService;

    @Resource
    private EmailTokenService emailTokenService;

    @Transactional
    public String sendEmail(final RegisterAccountDTO dto, final HttpServletRequest request) {
        return registerAccountEmailService.sendEmail(dto, request);
    }

    @Transactional
    public EmailVerificationResponseDTO fromEmail(final EmailVerificationDTO dto,
                                                  final HttpServletRequest request) {
        final EmailToken emailToken = registerAccountEmailService.getEmailToken(dto);
        final String trid = vetumaTransactionService.startTransaction(emailToken, request);

        return EmailVerificationResponseDTO.ok(trid);
    }

    @Transactional(noRollbackFor = VetumaTransactionException.class)
    public void fromSso(final SamlAuthenticationResult authResult) {
        final VetumaTransaction vetumaTransaction =
                vetumaTransactionService.requirePendingTransaction(authResult.getRelayState());

        if (authResult.isSuccessful()) {
            LOG.info("SAML login OK for TRID={}", authResult.getRelayState());

            final SystemUser user = samlRegistrationService.registerUserAndPerson(
                    vetumaTransaction.getEmail(), authResult.getUserAttributes());

            vetumaTransaction.setStatusSuccess(user);

        } else {
            LOG.warn("SAML login FAILED for TRID={} with errorCodes={} lastErrorReason={}",
                    authResult.getRelayState(),
                    Joiner.on(',').join(authResult.getErrors()),
                    authResult.getLastErrorReason());

            vetumaTransaction.setStatusError();
        }
    }

    @Transactional(noRollbackFor = VetumaTransactionException.class)
    public CompleteRegistrationDTO startCompleteRegistration(final CompleteRegistrationRequestDTO request) {
        return toCompleteRegistrationDTO(vetumaTransactionService.requireSuccessfulTransaction(request.getTrid()));
    }

    private static CompleteRegistrationDTO toCompleteRegistrationDTO(final VetumaTransaction vetumaTransaction) {
        final SystemUser user = Objects.requireNonNull(vetumaTransaction.getUser(), "user is missing");
        final Person person = Objects.requireNonNull(user.getPerson(), "person is missing");

        final CompleteRegistrationDTO dto = new CompleteRegistrationDTO();
        dto.setTrid(vetumaTransaction.getId());
        dto.setFirstName(person.getFirstName());
        dto.setByName(person.getByName());
        dto.setLastName(person.getLastName());
        dto.setSsn(person.getSsn());
        dto.setLang(person.getLanguageCode());
        dto.setPhoneNumber(person.getPhoneNumber());
        dto.setAddressEditable(person.isAddressEditable());

        final Address address = person.getAddress();

        if (address != null) {
            dto.setStreetAddress(address.getStreetAddress());
            dto.setPostalCode(address.getPostalCode());
            dto.setCity(address.getCity());
            dto.setCountry(address.getCountry());
        }

        return dto;
    }

    @Transactional(noRollbackFor = VetumaTransactionException.class)
    public void completeAccountRegistration(final CompleteRegistrationDTO dto, final HttpServletRequest request) {
        final VetumaTransaction vetumaTransaction = vetumaTransactionService
                .requireSuccessfulTransaction(dto.getTrid());
        vetumaTransaction.setStatusFinished();
        emailTokenService.revoke(vetumaTransaction.getEmailToken(), request);

        final SystemUser user = Objects.requireNonNull(vetumaTransaction.getUser(), "user is missing");
        user.setActive(true);
        user.setLocale(Locales.getLocaleByLanguageCode(dto.getLang()));
        changePasswordService.setUserPassword(user, dto.getPassword());

        final Person person = Objects.requireNonNull(user.getPerson(), "person is missing");
        completeAccountRegistration(dto, person);
    }

    private static void completeAccountRegistration(final CompleteRegistrationDTO dto, final Person person) {
        person.setByName(dto.getByName());
        person.setLanguageCode(dto.getLang());

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
