package fi.riista.api;

import fi.riista.feature.account.AccountAddressDTO;
import fi.riista.feature.account.AccountDTO;
import fi.riista.feature.account.AccountEditFeature;
import fi.riista.feature.account.AccountOtherInfoDTO;
import fi.riista.feature.account.AccountShootingTestDTO;
import fi.riista.feature.account.AccountShootingTestFeature;
import fi.riista.feature.account.AccountViewFeature;
import fi.riista.feature.account.ModifyTwoFactorAuthenticationDTO;
import fi.riista.feature.account.ModifyTwoFactorAuthenticationFeature;
import fi.riista.feature.account.password.ChangePasswordDTO;
import fi.riista.feature.account.payment.HunterPaymentPdfFeature;
import fi.riista.feature.account.todo.AccountSrvaTodoCountDTO;
import fi.riista.feature.account.todo.AccountTodoCountDTO;
import fi.riista.feature.account.todo.AccountTodoFeature;
import fi.riista.feature.announcement.show.ListAnnouncementDTO;
import fi.riista.feature.announcement.show.ListAnnouncementFeature;
import fi.riista.feature.announcement.show.ListAnnouncementRequest;
import fi.riista.feature.huntingclub.members.club.ContactInfoShareUpdateDTO;
import fi.riista.feature.huntingclub.members.club.HuntingClubMemberCrudFeature;
import fi.riista.feature.huntingclub.members.invitation.HuntingClubMemberInvitationDTO;
import fi.riista.feature.huntingclub.members.invitation.HuntingClubMemberInvitationFeature;
import fi.riista.feature.organization.address.AddressDTO;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
    private ModifyTwoFactorAuthenticationFeature modifyTwoFactorAuthenticationFeature;

    @Resource
    private HunterPaymentPdfFeature hunterPaymentPdfFeature;

    @Resource
    private HuntingClubMemberCrudFeature huntingClubMemberCrudFeature;

    @Resource
    private HuntingClubMemberInvitationFeature huntingClubInvitationFeature;

    @Resource
    private ListAnnouncementFeature listAnnouncementFeature;

    @Resource
    private AccountShootingTestFeature accountShootingTestFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public AccountDTO getAccount(final HttpServletRequest request) {
        return accountViewFeature.getActiveAccount(request);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{personId:\\d+}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public AccountDTO getAccountForPerson(@PathVariable final long personId) {
        return accountViewFeature.getAccount(personId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(value = "me/address", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void updateAddress(@RequestBody @Valid final AccountAddressDTO dto) {
        accountEditFeature.updateAddress(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(value = "me/other", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void updateOtherInfo(@RequestBody @Valid final AccountOtherInfoDTO dto) {
        accountEditFeature.updateOtherInfo(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(value = "{personId:\\d+}/address", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void updateAddress(@PathVariable final long personId,
                              @RequestBody @Valid final AccountAddressDTO dto) {
        accountEditFeature.updateAddress(dto, personId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(value = "{personId:\\d+}/other", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void updateOtherInfo(@PathVariable final long personId,
                                @RequestBody @Valid final AccountOtherInfoDTO dto) {
        accountEditFeature.updateOtherInfo(dto, personId);
    }

    @PostMapping(value = "password", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@RequestBody @Valid final ChangePasswordDTO dto) {
        accountEditFeature.changeActiveUserPassword(dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "twofactor", produces = MediaType.APPLICATION_JSON_VALUE)
    public ModifyTwoFactorAuthenticationDTO getTwoFactorAuthentication() {
        return modifyTwoFactorAuthenticationFeature.getTwoFactorAuthentication();
    }

    @PostMapping(value = "twofactor", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ModifyTwoFactorAuthenticationDTO modifyTwoFactorAuthentication(
            @RequestBody @Valid final ModifyTwoFactorAuthenticationDTO dto) {

        return modifyTwoFactorAuthenticationFeature.updateTwoFactorAuthentication(dto);
    }

    @PostMapping(value = "deactivate")
    public void deactivate(@ModelAttribute("personId") final Long personId) {
        accountEditFeature.deactivate(personId);
    }

    @PutMapping(value = "srva/enable")
    public void enableSrvaFeature() {
        accountEditFeature.toggleActivationOfSrvaFeature(true);
    }

    @PutMapping(value = "srva/disable")
    public void disableSrvaFeature() {
        accountEditFeature.toggleActivationOfSrvaFeature(false);
    }

    @PutMapping(value = "shootingtests/enable")
    public void enableShootingTestFeature() {
        accountEditFeature.toggleActivationOfShootingTestFeature(true);
    }

    @PutMapping(value = "shootingtests/disable")
    public void disableShootingTestFeature() {
        accountEditFeature.toggleActivationOfShootingTestFeature(false);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/todocount", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public AccountTodoCountDTO todoCount() {
        return accountTodoFeature.countTodos();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/srvatodocount", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public AccountSrvaTodoCountDTO srvaTodoCount(@RequestParam final long rhyId) {
        return accountTodoFeature.countSrvaTodos(rhyId);
    }

    @RequestMapping(value = "/{personId:\\d+}/payment/{huntingYear:\\d+}")
    public ResponseEntity<byte[]> paymentPdf(@PathVariable final long personId, @PathVariable final int huntingYear) {
        return hunterPaymentPdfFeature.create(personId, huntingYear);
    }

    @GetMapping(value = "invitation", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @CacheControl(policy = CachePolicy.NO_CACHE)
    public List<HuntingClubMemberInvitationDTO> myInvitations(@RequestParam(required = false) final Long personId) {
        return huntingClubInvitationFeature.listMyInvitations(personId);
    }

    @PutMapping(value = "contactshare")
    public void updateContactInfoSharing(@RequestBody @Valid final List<ContactInfoShareUpdateDTO> updates) {
        huntingClubMemberCrudFeature.updateContactInfoSharing(updates);
    }

    @GetMapping(value = "announcements")
    public Slice<ListAnnouncementDTO> myAnnouncements(
            @PageableDefault(size = 500, sort = "id", direction = Sort.Direction.ASC) final Pageable pageRequest) {

        final ListAnnouncementRequest request = new ListAnnouncementRequest();
        request.setDirection(ListAnnouncementRequest.Direction.RECEIVED);
        return listAnnouncementFeature.list(request, pageRequest);
    }

    @GetMapping(value = "shootingtests", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @CacheControl(policy = CachePolicy.NO_CACHE)
    public List<AccountShootingTestDTO> myShootingTests(@RequestParam(required = false) final Long personId) {
        return accountShootingTestFeature.listMyShootingTests(personId);
    }
}
