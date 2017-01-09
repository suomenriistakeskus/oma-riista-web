package fi.riista.feature.mail.admin;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.feature.mail.MailService;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.PersonRepository;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

@Component
public class AdminBulkMessageFeature {
    private static final Logger LOG = LoggerFactory.getLogger(AdminBulkMessageFeature.class);

    // For safety messages are scheduled for delivery after few hours
    private static final int DEFAULT_SEND_AFTER_HOURS = 2;

    @Resource
    private MailService mailService;

    @Resource
    private PersonRepository personRepository;

    //@Transactional - should not be transactional
    public AdminBulkMessageResponseDTO sendMessageToAllRegisteredUsers(final AdminBulkMessageRequestDTO dto) {
        final Set<String> emails = personRepository.findEmailForActiveUserWithRole(EnumSet.of(SystemUser.Role.ROLE_USER));
        return send(dto, emails);
    }

    //@Transactional - should not be transactional
    public AdminBulkMessageResponseDTO sendMessageToAllRegisteredClubContactPersons(final AdminBulkMessageRequestDTO dto) {
        final Set<String> emails = personRepository.findEmailForActiveUserWithOccupationType(OccupationType.SEURAN_YHDYSHENKILO);
        return send(dto, emails);
    }

    private AdminBulkMessageResponseDTO send(final AdminBulkMessageRequestDTO dto, final Set<String> activeUserEmails) {
        LOG.info("Found {} registered users to send mail to", activeUserEmails.size());

        long successCount = 0;
        long errorCount = 0;

        // By default delay sending by two hours
        final DateTime defaultSendAfter = DateTime.now().plusHours(DEFAULT_SEND_AFTER_HOURS);
        final DateTime sendAfterTime = Optional.ofNullable(dto.getSendAfter()).orElse(defaultSendAfter);

        for (final String email : activeUserEmails) {
            try {
                sendMessage(dto, email, sendAfterTime);
                successCount++;

            } catch (RuntimeException e) {
                errorCount++;
                LOG.error("Could not send message", e);
            }
        }

        LOG.info("Sent all messages with {} errors out of {} total.", errorCount, successCount);

        return new AdminBulkMessageResponseDTO(successCount, errorCount);
    }

    //@Transactional - should not be transactional
    public AdminBulkMessageResponseDTO sendTestMessage(final AdminBulkMessageRequestDTO dto, final String to) {
        long successCount = 0;
        long errorCount = 0;
        LOG.info("Sending test message to: {}", to);
        try {
            sendMessage(dto, to, DateTime.now());
            successCount++;
        } catch (RuntimeException e) {
            errorCount++;
            LOG.error("Could not send message", e);
        }
        LOG.info("Sent test message with {} errors out of {} total.", errorCount, successCount);
        return new AdminBulkMessageResponseDTO(successCount, errorCount);
    }

    private void sendMessage(final AdminBulkMessageRequestDTO dto, final String email, final DateTime sendAfterTime) {
        final MailMessageDTO.Builder builder = new MailMessageDTO.Builder()
                .withTo(email)
                .withBody(dto.getBody())
                .withSubject(dto.getSubject());

        mailService.sendLater(builder, sendAfterTime);
    }
}
