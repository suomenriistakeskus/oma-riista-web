package fi.riista.feature.permit.application;

import com.google.common.base.Preconditions;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.person.Person;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

@Embeddable
@Access(value = AccessType.FIELD)
public class PermitHolder implements Serializable {

    public enum PermitHolderType {
        PERSON,
        RY,
        BUSINESS,
        OTHER
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "permit_holder_type")
    private PermitHolderType type;

    @Size(max = 255)
    @Column(name = "permit_holder_name")
    private String name;

    @Size(max = 255)
    @Column(name = "permit_holder_code")
    private String code;


    // Constructors

    /**
     * For hibernate, do not use directly.
     */
    public PermitHolder() {

    }

    private PermitHolder(String name, String code, PermitHolderType type) {
        this.name = name;
        this.code = code;
        this.type = type;
    }

    public static PermitHolder createHolderForPerson(final Person person) {
        Preconditions.checkNotNull(person);
        return new PermitHolder(person.getFullName(), null, PermitHolderType.PERSON);
    }

    public static PermitHolder createHolderForClub(final HuntingClub club) {
        Preconditions.checkNotNull(club);

        final PermitHolderType type = Optional.ofNullable(club.getSubtype())
                .map(subtype -> PermitHolderType.valueOf(subtype.name()))
                .orElse(PermitHolderType.OTHER);

        return new PermitHolder(club.getNameFinnish(), club.getOfficialCode(), type);
    }

    public static PermitHolder create(String name, String code, PermitHolderType type) {
        return new PermitHolder(name, code, type);
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public PermitHolderType getType() {
        return type;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final PermitHolder that = (PermitHolder) o;
        return type == that.type &&
                Objects.equals(name, that.name) &&
                Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name, code);
    }
}
