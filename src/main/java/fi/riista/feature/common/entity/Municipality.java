package fi.riista.feature.common.entity;

import fi.riista.util.LocalisedString;
import org.springframework.data.domain.Persistable;

import javax.annotation.Nonnull;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Entity
@Access(value = AccessType.FIELD)
public class Municipality implements HasID<String>, Persistable<String> {
    private String id;

    @NotNull
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String nameFinnish;

    @NotNull
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String nameSwedish;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    public Set<String> getRhyIds() {
        return rhyIds;
    }

    public void setRhyIds(Set<String> rhyIds) {
        this.rhyIds = rhyIds;
    }

    @ElementCollection(fetch = FetchType.LAZY)
    @Column(name = "rhy_official_code")
    @CollectionTable(name = "municipality_rhy", joinColumns = @JoinColumn(name = "official_code"))
    private Set<String> rhyIds = new HashSet<>();

    // Should not be created by application
    Municipality() {
    }

    // For unit-testing only
    public Municipality(String officialCode, String nameFinnish, String nameSwedish) {
        this.id = officialCode;
        this.nameFinnish = nameFinnish;
        this.nameSwedish = nameSwedish;
    }

    @Id
    @Override
    @Pattern(regexp = "^\\d{0,3}$")
    @Access(value = AccessType.PROPERTY)
    @Column(name = "official_code", length = 3, nullable = false, updatable = false)
    public String getId() {
        return id;
    }

    void setId(String id) {
        this.id = id;
    }

    public String getOfficialCode() {
        return getId();
    }

    @Nonnull
    public LocalisedString getNameLocalisation() {
        return LocalisedString.of(nameFinnish, nameSwedish);
    }

    @Override
    public boolean isNew() {
        return this.id != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Municipality)) {
            return false;
        }

        Municipality that = (Municipality) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
