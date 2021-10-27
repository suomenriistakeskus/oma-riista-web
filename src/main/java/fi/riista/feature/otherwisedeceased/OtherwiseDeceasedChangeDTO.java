package fi.riista.feature.otherwisedeceased;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.otherwisedeceased.OtherwiseDeceasedChange.ChangeType;
import fi.riista.util.F;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import javax.validation.Valid;

import static java.util.Objects.requireNonNull;

public class OtherwiseDeceasedChangeDTO {

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

    public static OtherwiseDeceasedChangeDTO create(@Nonnull final OtherwiseDeceasedChange entity,
                                                    final SystemUser user) {

        requireNonNull(entity);

        final OtherwiseDeceasedChangeDTO dto = new OtherwiseDeceasedChangeDTO();

        dto.setId(entity.getId());
        dto.setModificationTime(entity.getPointOfTime());
        dto.setChangeType(entity.getChangeType());
        dto.setAuthor(F.mapNullable(user, AuthorDTO::create));
        dto.setReasonForChange(entity.getReasonForChange());

        return dto;
    }

    // Attributes

    private Long id;

    private DateTime modificationTime;

    @Valid
    private AuthorDTO author;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String reasonForChange;

    private ChangeType changeType;

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

    public ChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(final ChangeType changeType) {
        this.changeType = changeType;
    }
}
