package fi.riista.feature.organization;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import fi.riista.feature.common.entity.HasOfficialCode;
import fi.riista.util.DateUtil;
import fi.riista.util.LocalisedEnum;
import org.joda.time.LocalDate;

public enum OrganisationType implements HasOfficialCode, LocalisedEnum {

    RK(1),
    //RKH(2),
    RKA(3),
    VRN(4),
    ARN(5),
    RHY(6),
    CLUB(0),
    CLUBGROUP(0);

    private static final ImmutableSet<OrganisationType> AREA_DESCENDANTS = Sets.immutableEnumSet(ARN, RHY);
    private static final ImmutableSet<OrganisationType> ALLOW_LIST_OCCUPATIONS = Sets.immutableEnumSet(RK, RKA, VRN, ARN, RHY);
    private static final ImmutableSet<OrganisationType> ALLOW_GENERIC_OCCUPATION_EDIT = Sets.immutableEnumSet(
            OrganisationType.RHY,
            OrganisationType.RKA,
            OrganisationType.RK,
            OrganisationType.VRN,
            OrganisationType.ARN);

    private final int officialCode;

    OrganisationType(int officialCode) {
        this.officialCode = officialCode;
    }

    @Override
    public int getOfficialCode() {
        return officialCode;
    }

    public boolean isDescendantOfRiistakeskusArea() {
        return AREA_DESCENDANTS.contains(this);
    }

    public boolean allowListOccupations() {
        return ALLOW_LIST_OCCUPATIONS.contains(this);
    }

    public boolean allowGenericOccupationEdit() {
        return ALLOW_GENERIC_OCCUPATION_EDIT.contains(this);
    }

    public LocalDate getBeginDateForNewOccupation() {
        // by default club membership begins from begin of the current hunting year
        return this == OrganisationType.CLUB ? DateUtil.huntingYearBeginDate(DateUtil.huntingYear()) : null;
    }
}
