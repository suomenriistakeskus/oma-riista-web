package fi.riista.feature.account.user;

public class SystemUserPrivilegeDTO {

    private final SystemUserPrivilege privilege;
    private final SystemUser.Role role;

    public SystemUserPrivilegeDTO(SystemUserPrivilege privilege, SystemUser.Role role) {
        this.privilege = privilege;
        this.role = role;
    }

    public SystemUserPrivilege getPrivilege() {
        return privilege;
    }

    public SystemUser.Role getRole() {
        return role;
    }
}
