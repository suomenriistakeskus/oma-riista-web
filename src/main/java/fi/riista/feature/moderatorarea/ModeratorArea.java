package fi.riista.feature.moderatorarea;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.util.RandomStringUtil;
import javax.validation.constraints.NotBlank;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.security.SecureRandom;

@Entity
@Access(value = AccessType.FIELD)
public class ModeratorArea extends LifecycleEntity<Long> {

    @NotNull
    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private SystemUser moderator;

    @NotNull
    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private RiistakeskuksenAlue rka;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int year;

    @Size(min = 8, max = 255)
    @Column
    private String externalId;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional = false)
    @JoinColumn(nullable = false, unique = true)
    private GISZone zone;

    private Long id;

    // Helpers

    @Transient
    public void generateAndStoreExternalId(final SecureRandom random) {
        if (this.externalId != null) {
            throw new IllegalStateException("Cannot update existing externalId");
        }
        this.externalId = RandomStringUtil.generateExternalId(random);
    }

    // Accessors

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Access(value = AccessType.PROPERTY)
    @Column(name = "moderator_area_id", nullable = false)
    public Long getId() {
        return this.id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public SystemUser getModerator() {
        return moderator;
    }

    public void setModerator(final SystemUser moderator) {
        this.moderator = moderator;
    }

    public RiistakeskuksenAlue getRka() {
        return rka;
    }

    public void setRka(final RiistakeskuksenAlue rka) {
        this.rka = rka;
    }

    public int getYear() {
        return year;
    }

    public void setYear(final int year) {
        this.year = year;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(final String externalId) {
        this.externalId = externalId;
    }

    public GISZone getZone() {
        return zone;
    }

    public void setZone(final GISZone zone) {
        this.zone = zone;
    }
}
