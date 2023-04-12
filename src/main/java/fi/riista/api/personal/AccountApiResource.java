package fi.riista.api.personal;

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
import fi.riista.feature.account.todo.AccountPermitTodoCountDTO;
import fi.riista.feature.account.todo.AccountTodoCountDTO;
import fi.riista.feature.account.todo.AccountTodoFeature;
import fi.riista.feature.announcement.show.ListAnnouncementDTO;
import fi.riista.feature.announcement.show.ListAnnouncementFeature;
import fi.riista.feature.huntingclub.members.group.ContactInfoShareAndVisibilityUpdateDTO;
import fi.riista.feature.huntingclub.members.club.ContactInfoShareUpdateDTO;
import fi.riista.feature.huntingclub.members.club.HuntingClubMemberCrudFeature;
import fi.riista.feature.huntingclub.members.group.GroupMemberCrudFeature;
import fi.riista.feature.huntingclub.members.invitation.HuntingClubMemberInvitationDTO;
import fi.riista.feature.huntingclub.members.invitation.HuntingClubMemberInvitationFeature;
import fi.riista.feature.organization.occupation.OccupationContactInfoVisibilityDTO;
import fi.riista.feature.organization.occupation.OccupationCrudFeature;
import fi.riista.feature.organization.occupation.OccupationService;
import fi.riista.feature.organization.rhy.training.OccupationTrainingDTO;
import fi.riista.feature.organization.rhy.training.OccupationTrainingFeature;
import fi.riista.feature.permit.application.bird.amount.BirdPermitApplicationSpeciesAmountDTO;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
import javax.validation.constraints.NotEmpty;
import java.util.List;

import static java.util.Optional.ofNullable;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

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

    @Resource
    private GroupMemberCrudFeature groupMemberCrudFeature;

    @Resource
    private OccupationTrainingFeature occupationTrainingFeature;

    @Resource
    private OccupationService occupationService;

    @CacheControl(policy = {CachePolicy.NO_CACHE, CachePolicy.NO_STORE, CachePolicy.MUST_REVALIDATE})
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public AccountDTO getAccount(final HttpServletRequest request) {
        return accountViewFeature.getActiveAccount(request);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{personId:\\d+}", produces = MediaType.APPLICATION_JSON_VALUE)
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

    @PostMapping(value = "twofactor", produces = MediaType.APPLICATION_JSON_VALUE, consumes =
            MediaType.APPLICATION_JSON_VALUE)
    public ModifyTwoFactorAuthenticationDTO modifyTwoFactorAuthentication(
            @RequestBody @Valid final ModifyTwoFactorAuthenticationDTO dto) {

        return modifyTwoFactorAuthenticationFeature.updateTwoFactorAuthentication(dto);
    }

    @PostMapping(value = "deactivate")
    public void deactivate(@ModelAttribute("personId") final Long personId) {
        accountEditFeature.deactivate(personId);
    }

    @PutMapping(value = "{personId:\\d+}/srva/enable")
    public void enableSrvaFeature(@PathVariable final long personId) {
        accountEditFeature.toggleActivationOfSrvaFeature(personId, true);
    }

    @PutMapping(value = "{personId:\\d+}/srva/disable")
    public void disableSrvaFeature(@PathVariable final long personId) {
        accountEditFeature.toggleActivationOfSrvaFeature(personId, false);
    }

    @PutMapping(value = "{personId:\\d+}/shootingtests/enable")
    public void enableShootingTestFeature(@PathVariable final long personId) {
        accountEditFeature.toggleActivationOfShootingTestFeature(personId, true);
    }

    @PutMapping(value = "{personId:\\d+}/shootingtests/disable")
    public void disableShootingTestFeature(@PathVariable final long personId) {
        accountEditFeature.toggleActivationOfShootingTestFeature(personId, false);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/permittodocount", produces = MediaType.APPLICATION_JSON_VALUE)
    public AccountPermitTodoCountDTO permitTodoCount() {
        return accountTodoFeature.countPermitTodos();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/invitationtodocount", produces = MediaType.APPLICATION_JSON_VALUE)
    public AccountTodoCountDTO invitationTodoCount() {
        return accountTodoFeature.countInvitationTodos();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/srvatodocount", produces = MediaType.APPLICATION_JSON_VALUE)
    public AccountTodoCountDTO srvaTodoCount(@RequestParam final long rhyId) {
        return accountTodoFeature.countSrvaTodos(rhyId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/shootingtesttodocount", produces = MediaType.APPLICATION_JSON_VALUE)
    public AccountTodoCountDTO shootingTestTodoCount(@RequestParam final long rhyId) {
        return accountTodoFeature.countShootingTestTodos(rhyId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/taxationtodocount", produces = MediaType.APPLICATION_JSON_VALUE)
    public AccountTodoCountDTO taxationTodoCount(@RequestParam final long rhyId) {
        return accountTodoFeature.countTaxationTodos(rhyId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/huntingcontroleventtodocount", produces = MediaType.APPLICATION_JSON_VALUE)
    public AccountTodoCountDTO huntingControlEventTodoCount(@RequestParam final long rhyId) {
        return accountTodoFeature.countHuntingControlEventTodos(rhyId);
    }

    @RequestMapping(value = "/{personId:\\d+}/payment/{huntingYear:\\d+}")
    public ResponseEntity<byte[]> paymentPdf(@PathVariable final long personId, @PathVariable final int huntingYear) {
        return hunterPaymentPdfFeature.create(personId, huntingYear);
    }

    @GetMapping(value = "invitation", produces = MediaType.APPLICATION_JSON_VALUE)
    @CacheControl(policy = CachePolicy.NO_CACHE)
    public List<HuntingClubMemberInvitationDTO> myInvitations(@RequestParam(required = false) final Long personId) {
        return huntingClubInvitationFeature.listMyInvitations(personId);
    }

    static class ContactInfoShareList {

        @NotEmpty
        @Valid
        public List<ContactInfoShareUpdateDTO> list;

        public List<ContactInfoShareUpdateDTO> getList() {
            return list;
        }

        public void setList(final List<ContactInfoShareUpdateDTO> list) {
            this.list = list;
        }
    }

    @PutMapping(value = "contactshare")
    public void updateContactInfoSharing(@RequestBody @Valid final ContactInfoShareList updates) {
        huntingClubMemberCrudFeature.updateContactInfoSharing(updates.getList());
    }

    static class ContactInfoShareAndVisibilityList {

        @NotEmpty
        @Valid
        public List<ContactInfoShareAndVisibilityUpdateDTO> list;

        public List<ContactInfoShareAndVisibilityUpdateDTO> getList() {
            return list;
        }

        public void setList(final List<ContactInfoShareAndVisibilityUpdateDTO> list) {
            this.list = list;
        }
    }
    @PutMapping(value = "contactshare-and-visibility")
    public void updateContactInfoSharingAndVisibility(@RequestBody @Valid final ContactInfoShareAndVisibilityList updates) {
        groupMemberCrudFeature.updateContactInfoSharingAndVisibility(updates.getList());
    }

    @GetMapping(value = "announcements")
    public Slice<ListAnnouncementDTO> myAnnouncements(
            @PageableDefault(size = 500, sort = "id", direction = Sort.Direction.ASC) final Pageable pageRequest) {
        return listAnnouncementFeature.listMine(pageRequest);
    }

    @GetMapping(value = "shootingtests", produces = MediaType.APPLICATION_JSON_VALUE)
    @CacheControl(policy = CachePolicy.NO_CACHE)
    public List<AccountShootingTestDTO> myShootingTests(@RequestParam(required = false) final Long personId) {
        return accountShootingTestFeature.listMyShootingTests(personId);
    }

    @PutMapping(value = "/occupation-contact-info-visibility")
    public void updateOccupationContactInfoVisibility(@RequestBody @Validated final List<OccupationContactInfoVisibilityDTO> dtoList) {
        occupationService.updateContactInfoVisibility(dtoList);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/occupation-trainings", produces = APPLICATION_JSON_VALUE)
    public List<OccupationTrainingDTO> listOccupationTrainings(@RequestParam(required = false) final Long personId) {
        return ofNullable(personId)
                .map(occupationTrainingFeature::listForPerson)
                .orElseGet(occupationTrainingFeature::listMine);
    }

    @PostMapping(value = "unregister")
    public void unregister(@ModelAttribute("personId") final Long personId) {
        accountEditFeature.unregister(personId);
    }

    @PostMapping(value = "cancel-unregister")
    public void cancelUnregister(@ModelAttribute("personId") final Long personId) {
        accountEditFeature.cancelUnregister(personId);
    }
}
