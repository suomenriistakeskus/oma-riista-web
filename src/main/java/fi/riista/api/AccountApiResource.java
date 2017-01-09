package fi.riista.api;

import fi.riista.feature.account.AccountEditFeature;
import fi.riista.feature.account.todo.AccountTodoFeature;
import fi.riista.feature.account.AccountViewFeature;
import fi.riista.feature.account.payment.HunterPaymentPdfFeature;
import fi.riista.feature.account.AccountDTO;
import fi.riista.feature.account.todo.AccountSrvaTodoCountDTO;
import fi.riista.feature.account.todo.AccountTodoCountDTO;
import fi.riista.feature.account.password.ChangePasswordDTO;
import fi.riista.feature.announcement.show.ListAnnouncementDTO;
import fi.riista.feature.announcement.show.ListAnnouncementFeature;
import fi.riista.feature.announcement.show.ListAnnouncementRequest;
import fi.riista.feature.huntingclub.members.club.ContactInfoShareUpdateDTO;
import fi.riista.feature.huntingclub.members.club.HuntingClubMemberCrudFeature;
import fi.riista.feature.huntingclub.members.invitation.HuntingClubMemberInvitationDTO;
import fi.riista.feature.huntingclub.members.invitation.HuntingClubMemberInvitationFeature;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(value = AccountApiResource.ACCOUNT_RESOURCE_URL)
public class AccountApiResource {

    public static final String ACCOUNT_RESOURCE_URL = "/api/v1/account";

    @Resource
    private AccountEditFeature accountEditFeature;

    @Resource
    private AccountViewFeature accountViewFeature;

    @Resource
    private AccountTodoFeature accountTodoFeature;

    @Resource
    private HunterPaymentPdfFeature hunterPaymentPdfFeature;

    @Resource
    private HuntingClubMemberCrudFeature huntingClubMemberCrudFeature;

    @Resource
    private HuntingClubMemberInvitationFeature huntingClubInvitationFeature;

    @Resource
    private ListAnnouncementFeature listAnnouncementFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public AccountDTO getAccount(HttpServletRequest request) {
        return accountEditFeature.getActiveAccount(request);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "{personId:\\d+}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public AccountDTO getAccountForPerson(@PathVariable Long personId) {
        return accountViewFeature.getAccount(personId);
    }

    @RequestMapping(method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void saveAccount(@RequestBody @Valid AccountDTO dto) {
        accountEditFeature.updateActiveAccount(dto);
    }

    @RequestMapping(value = "other", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void saveAccountForPerson(@RequestBody @Valid AccountDTO dto) {
        accountEditFeature.updateOtherUserAccount(dto);
    }

    @RequestMapping(value = "password", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@RequestBody @Valid ChangePasswordDTO dto) {
        accountEditFeature.changeActiveUserPassword(dto);
    }

    @RequestMapping(value = "deactivate", method = RequestMethod.POST)
    public void deactivate(@ModelAttribute("personId") Long personId) {
        accountEditFeature.deactivate(personId);
    }

    @RequestMapping(value = "srva/enable", method = RequestMethod.PUT)
    public void srvaEnable() {
        accountEditFeature.updateSrvaEnabled(true);
    }

    @RequestMapping(value = "srva/disable", method = RequestMethod.PUT)
    public void srvaDisable() {
        accountEditFeature.updateSrvaEnabled(false);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/todocount", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public AccountTodoCountDTO todoCount() {
        return accountTodoFeature.todoCount();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/srvatodocount", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public AccountSrvaTodoCountDTO srvaTodoCount(@RequestParam final long rhyId) {
        return accountTodoFeature.srvaTodoCount(rhyId);
    }

    @RequestMapping(value = "/{personId:\\d+}/payment/{huntingYear:\\d+}")
    public ResponseEntity<byte[]> paymentPdf(@PathVariable long personId,
                                             @PathVariable int huntingYear) {
        return hunterPaymentPdfFeature.create(personId, huntingYear);
    }

    @RequestMapping(
            method = RequestMethod.GET,
            value = "invitation",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @CacheControl(policy = CachePolicy.NO_CACHE)
    public List<HuntingClubMemberInvitationDTO> myInvitations(@RequestParam(required = false) Long personId) {
        return huntingClubInvitationFeature.myInvitations(personId);
    }

    @RequestMapping(
            method = RequestMethod.PUT,
            value = "contactshare")
    public void updateContactInfoSharing(@RequestBody @Valid List<ContactInfoShareUpdateDTO> updates) {
        huntingClubMemberCrudFeature.updateContactInfoSharing(updates);
    }

    @RequestMapping(
            method = RequestMethod.GET,
            value = "announcements")
    public Slice<ListAnnouncementDTO> myAnnouncements(
            @PageableDefault(size = 500, sort = "id", direction = Sort.Direction.ASC) Pageable pageRequest) {
        final ListAnnouncementRequest request = new ListAnnouncementRequest();
        request.setDirection(ListAnnouncementRequest.Direction.RECEIVED);
        return listAnnouncementFeature.list(request, pageRequest);
    }
}
