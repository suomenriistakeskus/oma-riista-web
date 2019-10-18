package fi.riista.api.moderator;


import fi.riista.feature.mail.ModeratorBulkMessageFeature;
import fi.riista.feature.mail.admin.AdminBulkMessageRequestDTO;
import fi.riista.feature.mail.admin.AdminBulkTestMessageRequestDTO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class ModeratorBulkMessageController {

    @Resource
    private ModeratorBulkMessageFeature moderatorBulkMessageFeature;

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasPrivilege('SEND_BULK_MESSAGES')")
    @RequestMapping(value = "/api/v1/bulkMail/sendBulkMail", method = RequestMethod.POST)
    public void send(@RequestBody @Validated final AdminBulkMessageRequestDTO requestDTO) {
        moderatorBulkMessageFeature.sendMessageToAllRegisteredUsers(requestDTO);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasPrivilege('SEND_BULK_MESSAGES')")
    @RequestMapping(value = "/api/v1/bulkMail/sendBulkMailToClubContacts", method = RequestMethod.POST)
    public void sendToClubContacts(@RequestBody @Validated final AdminBulkMessageRequestDTO requestDTO) {
        moderatorBulkMessageFeature.sendMessageToAllRegisteredClubContactPersons(requestDTO);
    }

    @RequestMapping(value = "/api/v1/bulkMail/sendTestBulkMail", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasPrivilege('SEND_BULK_MESSAGES')")
    public void sendTest(@RequestBody @Validated final AdminBulkTestMessageRequestDTO requestDTO) {
        moderatorBulkMessageFeature.sendTestMessage(requestDTO, requestDTO.getTestRecipient());
    }
}
