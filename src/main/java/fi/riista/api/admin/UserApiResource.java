package fi.riista.api.admin;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.SystemUserDTO;
import fi.riista.feature.account.user.SystemUserPrivilege;
import fi.riista.feature.account.user.SystemUserPrivilegeDTO;
import fi.riista.feature.account.user.UserCrudFeature;
import fi.riista.util.F;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/v1/admin/users", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class UserApiResource {

    @Resource
    private UserCrudFeature crudFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(method = RequestMethod.GET)
    public Page<SystemUserDTO> page(final Pageable pageRequest,
                                    final @RequestParam(required = false) List<SystemUser.Role> roles) {
        if (F.isNullOrEmpty(roles)) {
            return crudFeature.list(pageRequest);
        }
        return crudFeature.listHavingAnyOfRole(roles, pageRequest);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "privileges", method = RequestMethod.GET)
    public List<SystemUserPrivilegeDTO> listPrivileges() {
        return Arrays.stream(SystemUserPrivilege.values()).map(
                value -> new SystemUserPrivilegeDTO(value, value.getRole())).collect(Collectors.toList());
    }


    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "{id:\\d+}", method = RequestMethod.GET)
    public SystemUserDTO read(@PathVariable final Long id) {
        return crudFeature.read(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public SystemUserDTO create(@RequestBody @Validated(SystemUserDTO.Create.class) final SystemUserDTO dto) {
        return crudFeature.create(dto);
    }

    @RequestMapping(value = "{id:\\d+}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public SystemUserDTO update(@PathVariable final Long id,
                                @RequestBody @Validated(SystemUserDTO.Edit.class) final SystemUserDTO dto) {
        dto.setId(id);

        return crudFeature.update(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = "{id:\\d+}", method = RequestMethod.DELETE)
    public void delete(@PathVariable final Long id) {
        crudFeature.delete(id);
    }
}
