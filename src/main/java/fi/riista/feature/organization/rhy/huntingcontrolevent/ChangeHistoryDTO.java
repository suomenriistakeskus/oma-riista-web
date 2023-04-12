package fi.riista.feature.organization.rhy.huntingcontrolevent;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.util.F;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import javax.validation.Valid;

import static java.util.Objects.requireNonNull;

public class ChangeHistoryDTO {

    // Inner-classes

    public static class AuthorDTO {
        public static AuthorDTO create(@Nonnull final SystemUser user) {
            final AuthorDTO dto = new AuthorDTO();
            dto.id = user.getId();
            dto.firstName = user.getFirstName();
            dto.lastName = user.getLastName();
            return dto;
        }

        private Long id;

        @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
        private String firstName;

        @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
        private String lastName;

        public Long getId() {
            return id;
        }

        public void setId(final Long id) {
            this.id = id;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(final String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(final String lastName) {
            this.lastName = lastName;
        }

    }

    // Factories

    public static ChangeHistoryDTO create(@Nonnull final HuntingControlEventChange change,
                                          @Nonnull final SystemUser author) {
        requireNonNull(change);
        requireNonNull(author);

        final ChangeHistoryDTO dto = new ChangeHistoryDTO();

        dto.setId(change.getId());
        dto.setModificationTime(change.getChangeHistory().getPointOfTime());
        dto.setChangeType(change.getChangeHistory().getChangeType());
        dto.setAuthor(F.mapNullable(author, ChangeHistoryDTO.AuthorDTO::create));
        dto.setReasonForChange(change.getChangeHistory().getReasonForChange());

        return dto;
    }

    // Attributes

    private Long id;

    private DateTime modificationTime;

    @Valid
    private AuthorDTO author;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String reasonForChange;

    private ChangeHistory.ChangeType changeType;

    // Accessors

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public DateTime getModificationTime() {
        return modificationTime;
    }

    public void setModificationTime(final DateTime modificationTime) {
        this.modificationTime = modificationTime;
    }

    public AuthorDTO getAuthor() {
        return author;
    }

    public void setAuthor(final AuthorDTO author) {
        this.author = author;
    }

    public String getReasonForChange() {
        return reasonForChange;
    }

    public void setReasonForChange(final String reasonForChange) {
        this.reasonForChange = reasonForChange;
    }

    public ChangeHistory.ChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(final ChangeHistory.ChangeType changeType) {
        this.changeType = changeType;
    }
}
