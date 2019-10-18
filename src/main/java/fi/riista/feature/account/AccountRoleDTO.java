package fi.riista.feature.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableMap;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

public class AccountRoleDTO {

    private static final String RHY_ID = "rhyId";
    private static final String CLUB_ID = "clubId";
    private static final String NAME_FI = "nameFI";
    private static final String NAME_SV = "nameSV";
    private static final String PERSON_ID = "personId";

    public static AccountRoleDTO fromOccupation(@Nonnull final Occupation occ) {
        Objects.requireNonNull(occ, "occupation must not be null");

        final ImmutableMap.Builder<String, Object> contextBuilder = ImmutableMap.builder();
        Organisation organisation = occ.getOrganisation();

        switch (organisation.getOrganisationType()) {
            case RHY:
                contextBuilder.put(RHY_ID, organisation.getId());
                break;
            case CLUB:
                contextBuilder.put(CLUB_ID, organisation.getId());
                break;
            case CLUBGROUP:
                organisation = organisation.getParentOrganisation();
                contextBuilder.put(CLUB_ID, organisation.getId());
                break;
            default:
                break;
        }

        contextBuilder.put(NAME_FI, organisation.getNameFinnish());
        contextBuilder.put(NAME_SV, organisation.getNameSwedish());

        return new AccountRoleDTO(String.format("occupation:%d", occ.getId()),
                occ.getOccupationType().name(), occ.getCreationTime(), null, contextBuilder.build());
    }

    public static AccountRoleDTO fromUser(@Nonnull final SystemUser user) {
        Objects.requireNonNull(user, "user must not be null");

        return new AccountRoleDTO(String.format("user:%d", user.getId()),
                user.getRole().name(), null, getFullName(user),
                Collections.singletonMap(PERSON_ID, F.getId(user.getPerson())));
    }

    private static String getFullName(final SystemUser user) {
        return user.getPerson() != null ? user.getPerson().getFullName() : user.getFullName();
    }

    public static AccountRoleDTO fromPermit(@Nonnull final HarvestPermit permit) {
        return new AccountRoleDTO(String.format("permit:%d", permit.getId()),
                "PERMIT", permit.getCreationTime(), null,
                ImmutableMap.of(
                        "permitId", permit.getId(),
                        "permitType", permit.getPermitType(),
                        "permitTypeCode", permit.getPermitTypeCode(),
                        "permitNumber", permit.getPermitNumber()));
    }

    private final String id;
    private final String type;
    private final boolean recentlyAdded;
    private final String displayName;
    private final Map<String, Object> context;

    private AccountRoleDTO(@Nonnull final String id,
                           @Nonnull final String type,
                           final Date creationTime,
                           final String displayName,
                           @Nonnull final Map<String, Object> context) {
        this.id = Objects.requireNonNull(id);
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.recentlyAdded = creationTime != null && ageIsLessThanDays(creationTime, Duration.standardDays(30));
        this.displayName = displayName;
        this.context = Objects.requireNonNull(context);
    }

    private static boolean ageIsLessThanDays(final Date creationTime, final Duration minDuration) {
        return new Duration(new DateTime(creationTime), DateUtil.now()).compareTo(minDuration) < 0;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public boolean isRecentlyAdded() {
        return recentlyAdded;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    @JsonIgnore
    public Object getContextValue(final String key) {
        return context.get(key);
    }
}
