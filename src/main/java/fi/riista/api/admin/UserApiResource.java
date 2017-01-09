package fi.riista.api.admin;

import fi.riista.feature.account.user.UserCrudFeature;
import fi.riista.feature.account.user.SystemUserDTO;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.SystemUserPrivilege;
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
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/admin/users", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class UserApiResource {

    @Resource
    private UserCrudFeature crudFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(method = RequestMethod.GET)
    public Page<SystemUserDTO> page(Pageable pageRequest, @RequestParam(required = false) List<SystemUser.Role> roles) {
        if (F.isNullOrEmpty(roles)) {
            return crudFeature.list(pageRequest);
        }
        return crudFeature.listHavingAnyOfRole(roles, pageRequest);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "privileges", method = RequestMethod.GET)
    public SystemUserPrivilege[] listPrivileges() {
        return SystemUserPrivilege.values();
    }


    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "{id:\\d+}", method = RequestMethod.GET)
    public SystemUserDTO read(@PathVariable Long id) {
        return crudFeature.read(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public SystemUserDTO create(@RequestBody @Validated(SystemUserDTO.Create.class) SystemUserDTO dto) {
        return crudFeature.create(dto);
    }

    @RequestMapping(value = "{id:\\d+}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public SystemUserDTO update(@PathVariable Long id, @RequestBody @Validated(SystemUserDTO.Edit.class) SystemUserDTO dto) {
        dto.setId(id);

        return crudFeature.update(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = "{id:\\d+}", method = RequestMethod.DELETE)
    public void delete(@PathVariable Long id) {
        crudFeature.delete(id);
    }
}
