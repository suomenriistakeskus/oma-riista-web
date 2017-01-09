package fi.riista.feature.account.user;

import fi.riista.feature.common.entity.PersistableEnum;

public enum SystemUserPrivilege implements PersistableEnum {
    CHECK_EXTERNAL_AUTHENTICATION,
    CHECK_EXTERNAL_AUTHENTICATION_HUNTERNUMBER,
    CHECK_EXTERNAL_AUTHENTICATION_ADDRESS,
    CHECK_EXTERNAL_AUTHENTICATION_SSN,
    EXPORT_HUNTINGCLUB_AREA,
    EXPORT_LUKE_MOOSE,
    EXPORT_LUPAHALLINTA_HARVESTREPORTS,
    EXPORT_LUPAHALLINTA_HUNTINGCLUBS,
    EXPORT_LUPAHALLINTA_OCCUPATIONS,
    EXPORT_LUPAHALLINTA_MOOSELIKE_HARVESTS,
    EXPORT_LUPAHALLINTA_PERMIT_AREA,
    EXPORT_SRVA_RVR,
    IMPORT_JHT,
    IMPORT_METSASTAJAREKISTERI;

    @Override
    public String getDatabaseValue() {
        return this.name().toLowerCase().replace('_', '.');
    }
}
