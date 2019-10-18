package fi.riista.feature.account.user;

import fi.riista.feature.common.entity.PersistableEnum;

public enum SystemUserPrivilege implements PersistableEnum {
    CHECK_EXTERNAL_AUTHENTICATION(SystemUser.Role.ROLE_REST),
    CHECK_EXTERNAL_AUTHENTICATION_HUNTERNUMBER(SystemUser.Role.ROLE_REST),
    CHECK_EXTERNAL_AUTHENTICATION_ADDRESS(SystemUser.Role.ROLE_REST),
    CHECK_EXTERNAL_AUTHENTICATION_SSN(SystemUser.Role.ROLE_REST),
    CHECK_EXTERNAL_AUTHENTICATION_DATE_OF_BIRTH(SystemUser.Role.ROLE_REST),
    EXPORT_HUNTINGCLUB_AREA(SystemUser.Role.ROLE_REST),
    EXPORT_LUKE_MOOSE(SystemUser.Role.ROLE_REST),
    EXPORT_LUKE_COMMON(SystemUser.Role.ROLE_REST),
    EXPORT_RVR_COMMON(SystemUser.Role.ROLE_REST),
    EXPORT_METSAHALLITUS_HARVEST(SystemUser.Role.ROLE_REST),
    EXPORT_LUPAHALLINTA_HARVESTREPORTS(SystemUser.Role.ROLE_REST),
    EXPORT_LUPAHALLINTA_HUNTINGCLUBS(SystemUser.Role.ROLE_REST),
    EXPORT_LUPAHALLINTA_OCCUPATIONS(SystemUser.Role.ROLE_REST),
    EXPORT_LUPAHALLINTA_MOOSELIKE_HARVESTS(SystemUser.Role.ROLE_REST),
    EXPORT_SHOOTING_TEST_REGISTRY(SystemUser.Role.ROLE_REST),
    EXPORT_SRVA_RVR(SystemUser.Role.ROLE_REST),
    EXPORT_MR_JHT(SystemUser.Role.ROLE_REST),
    IMPORT_JHT(SystemUser.Role.ROLE_REST),
    IMPORT_METSAHALLITUS_PERMITS(SystemUser.Role.ROLE_REST),
    IMPORT_METSASTAJAREKISTERI(SystemUser.Role.ROLE_REST),
    SEND_BULK_MESSAGES(SystemUser.Role.ROLE_MODERATOR),
    ALTER_INVOICE_PAYMENT(SystemUser.Role.ROLE_MODERATOR),
    MODERATE_RHY_ANNUAL_STATISTICS(SystemUser.Role.ROLE_MODERATOR),
    HARVEST_REGISTRY(SystemUser.Role.ROLE_MODERATOR),
    EXPORT_HABIDES_REPORTS(SystemUser.Role.ROLE_MODERATOR);

    private final SystemUser.Role role;

    SystemUserPrivilege(final SystemUser.Role role) {
        this.role = role;
    }

    @Override
    public String getDatabaseValue() {
        return this.name().toLowerCase().replace('_', '.');
    }

    public SystemUser.Role getRole() {
        return role;
    }
}
