package fi.riista.api.admin;


import fi.riista.feature.mail.admin.AdminBulkMessageFeature;
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
public class AdminBulkMessageController {

    @Resource
    private AdminBulkMessageFeature adminBulkMessageFeature;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/api/v1/admin/sendBulkMail", method = RequestMethod.POST)
    public void send(@RequestBody @Validated AdminBulkMessageRequestDTO requestDTO) {
        adminBulkMessageFeature.sendMessageToAllRegisteredUsers(requestDTO);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/api/v1/admin/sendBulkMailToClubContacts", method = RequestMethod.POST)
    public void sendToClubContacts(@RequestBody @Validated AdminBulkMessageRequestDTO requestDTO) {
        adminBulkMessageFeature.sendMessageToAllRegisteredClubContactPersons(requestDTO);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/api/v1/admin/sendTestBulkMail", method = RequestMethod.POST)
    public void sendTest(@RequestBody @Validated AdminBulkTestMessageRequestDTO requestDTO) {
        adminBulkMessageFeature.sendTestMessage(requestDTO, requestDTO.getTestRecipient());
    }
}
