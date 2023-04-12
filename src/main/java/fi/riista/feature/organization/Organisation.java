package fi.riista.feature.organization;

import com.querydsl.core.annotations.QueryDelegate;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.calendar.Venue;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.util.LocalisedString;

import javax.annotation.Nonnull;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "organisationType", discriminatorType = DiscriminatorType.STRING)
@Entity
@Access(value = AccessType.FIELD)
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"organisationType", "officialCode"})})
public class Organisation extends LifecycleEntity<Long> {

    public static final String ID_COLUMN_NAME = "organisation_id";

    private Long id;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String nameFinnish;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String nameSwedish;

    @Enumerated(EnumType.STRING)
    @Column(name = "organisationType", nullable = false, updatable = false, insertable = false)
    protected OrganisationType organisationType;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(unique = true)
    private Address address;

    @Email
    @Size(max = 255)
    @Column
    private String email;

    @Size(max = 255)
    @Column
    private String phoneNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    protected Organisation parentOrganisation;

    @OneToMany(mappedBy = "parentOrganisation")
    protected Set<Organisation> subOrganisations = new HashSet<>();

    @OneToMany(mappedBy = "organisation")
    protected Set<Occupation> occupations = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "organisation_venue",
            joinColumns = {@JoinColumn(name = ID_COLUMN_NAME, referencedColumnName = ID_COLUMN_NAME)},
            inverseJoinColumns = {@JoinColumn(name = Venue.ID_COLUMN_NAME, referencedColumnName = Venue.ID_COLUMN_NAME)}
    )
    private Set<Venue> venues = new HashSet<>();

    @Size(max = 255)
    @Column(length = 255)
    private String officialCode;

    @Size(max = 255)
    @Column(unique = true, length = 255)
    private String lhOrganisationId;

    @Valid
    @Embedded
    private GeoLocation geoLocation;

    @Column(nullable = false)
    private boolean active = true;

    protected Organisation() {
    }

    protected Organisation(final OrganisationType organisationType) {
        this.organisationType = organisationType;
    }

    @Nonnull
    public LocalisedString getNameLocalisation() {
        return LocalisedString.of(nameFinnish, nameSwedish);
    }

    public void addVenue(final Venue venue) {
        venues.add(venue);
    }

    public void removeVenue(final Venue venue) {
        venues.remove(venue);
    }

    public Optional<Organisation> getClosestAncestorOfType(final OrganisationType type) {
        Objects.requireNonNull(type, "type must not be null");

        Organisation parent = parentOrganisation;

        while (parent != null) {
            if (type.equals(parent.getOrganisationType())) {
                return Optional.of(parent);
            }
            parent = parent.parentOrganisation;
        }

        return Optional.empty();
    }

    public Set<Organisation> getAllParentsAndSelf() {
        final Set<Organisation> result = new HashSet<>();
        Organisation current = this;

        while (current != null) {
            if (!result.add(current)) {
                throw new IllegalStateException("Found recurrent parent");
            }
            current = current.getParentOrganisation();
        }

        return result;
    }

    // QueryDSL delegates -->

    @QueryDelegate(Organisation.class)
    public static fi.riista.util.QLocalisedString nameLocalisation(final QOrganisation org) {
        return new fi.riista.util.QLocalisedString(org.nameFinnish, org.nameSwedish);
    }

    // Accessors -->

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Access(value = AccessType.PROPERTY)
    @Column(name = ID_COLUMN_NAME, nullable = false)
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public OrganisationType getOrganisationType() {
        return organisationType;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(final Address address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(final String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getNameFinnish() {
        return nameFinnish;
    }

    public void setNameFinnish(final String nameFinnish) {
        this.nameFinnish = nameFinnish;
    }

    public String getNameSwedish() {
        return nameSwedish;
    }

    public void setNameSwedish(final String nameSwedish) {
        this.nameSwedish = nameSwedish;
    }

    public Set<Venue> getVenues() {
        return venues;
    }

    public void setVenues(final Set<Venue> venues) {
        this.venues = venues;
    }

    public String getOfficialCode() {
        return officialCode;
    }

    public void setOfficialCode(final String officialCode) {
        this.officialCode = officialCode;
    }

    public String getLhOrganisationId() {
        return lhOrganisationId;
    }

    public void setLhOrganisationId(final String lhOrganisationId) {
        this.lhOrganisationId = lhOrganisationId;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(final GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }

    public Organisation getParentOrganisation() {
        return parentOrganisation;
    }

    public void setParentOrganisation(final Organisation parentOrganisation) {
        this.parentOrganisation = parentOrganisation;
    }

    public Set<Organisation> getSubOrganisations() {
        return subOrganisations;
    }

    public void setSubOrganisations(final Set<Organisation> subOrganisations) {
        this.subOrganisations = subOrganisations;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

}
