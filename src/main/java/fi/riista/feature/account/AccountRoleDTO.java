package fi.riista.feature.account;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.organization.occupation.Occupation;

import javax.annotation.Nonnull;
import java.util.Objects;

public class AccountRoleDTO {

    public static class ContextDTO {

        private Long personId;

        private Long rhyId;

        private Long clubId;

        private String nameFI;

        private String nameSV;

        public Long getPersonId() {
            return personId;
        }

        public void setPersonId(Long personId) {
            this.personId = personId;
        }

        public Long getRhyId() {
            return rhyId;
        }

        public void setRhyId(Long rhyId) {
            this.rhyId = rhyId;
        }

        public Long getClubId() {
            return clubId;
        }

        public void setClubId(Long clubId) {
            this.clubId = clubId;
        }

        public String getNameFI() {
            return nameFI;
        }

        public void setNameFI(String nameFI) {
            this.nameFI = nameFI;
        }

        public String getNameSV() {
            return nameSV;
        }

        public void setNameSV(String nameSV) {
            this.nameSV = nameSV;
        }
    }

    private String id;

    private String type;

    private String displayName;

    private final ContextDTO context = new ContextDTO();

    public AccountRoleDTO() {
    }

    protected AccountRoleDTO(@Nonnull final String idPrefix, @Nonnull final Long id, @Nonnull final Enum<?> type) {
        final long nonNullId = Objects.requireNonNull(id, idPrefix + " id must not be null");
        this.id = String.format("%s:%d", idPrefix, nonNullId);
        this.type = Objects.requireNonNull(type, "type must not be null").name();
    }

    public static AccountRoleDTO fromUser(@Nonnull final SystemUser user) {
        Objects.requireNonNull(user, "user must not be null");
        return new AccountRoleDTO("user", user.getId(), user.getRole());
    }

    public static AccountRoleDTO fromOccupation(@Nonnull final Occupation occ) {
        Objects.requireNonNull(occ, "occupation must not be null");
        return new AccountRoleDTO("occupation", occ.getId(), occ.getOccupationType());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String name) {
        this.displayName = name;
    }

    public ContextDTO getContext() {
        return context;
    }

}
