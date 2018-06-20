package fi.riista.feature.mail.admin;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.feature.mail.MailService;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.PersonRepository;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.EnumSet;
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
    public void sendMessageToAllRegisteredUsers(final AdminBulkMessageRequestDTO dto) {
        final Set<String> emails = personRepository.findEmailForActiveUserWithRole(EnumSet.of(SystemUser.Role.ROLE_USER));
        send(dto, emails);
    }

    //@Transactional - should not be transactional
    public void sendMessageToAllRegisteredClubContactPersons(final AdminBulkMessageRequestDTO dto) {
        final Set<String> emails = personRepository.findEmailForActiveUserWithOccupationType(OccupationType.SEURAN_YHDYSHENKILO);
        send(dto, emails);
    }

    private void send(final AdminBulkMessageRequestDTO dto, final Set<String> activeUserEmails) {
        LOG.info("Found {} registered users to send mail to", activeUserEmails.size());

        mailService.send(MailMessageDTO.builder()
                .withFrom(mailService.getDefaultFromAddress())
                .withRecipients(activeUserEmails)
                .appendBody(dto.getBody())
                .withSubject(dto.getSubject())
                .withScheduledTimeAfter(Duration.standardHours(DEFAULT_SEND_AFTER_HOURS))
                .build());
    }

    //@Transactional - should not be transactional
    public void sendTestMessage(final AdminBulkMessageRequestDTO dto, final String to) {
        LOG.info("Sending test message to: {}", to);

        mailService.send(MailMessageDTO.builder()
                .withFrom(mailService.getDefaultFromAddress())
                .addRecipient(to)
                .appendBody(dto.getBody())
                .withSubject(dto.getSubject())
                .build());
    }
}
