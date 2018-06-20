package fi.riista.feature.announcement;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.occupation.OccupationType;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Entity
@Access(value = AccessType.FIELD)
public class AnnouncementSubscriber extends LifecycleEntity<Long> {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Announcement announcement;

    @ManyToOne(fetch = FetchType.LAZY)
    private Organisation organisation;

    @Column
    @Enumerated(EnumType.STRING)
    private OccupationType occupationType;

    private Long id;

    // For Hibernate
    public AnnouncementSubscriber() {
    }

    public AnnouncementSubscriber(final Announcement announcement,
                                  final Organisation organisation,
                                  final OccupationType occupationType) {
        this.announcement = Objects.requireNonNull(announcement);
        this.organisation = organisation;
        this.occupationType = occupationType;

        if (organisation == null && occupationType == null) {
            throw new IllegalArgumentException("organisation and occupationType cannot both be empty");
        }
    }

    // Accessors -->

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Access(value = AccessType.PROPERTY)
    @Column(name = "announcement_subscriber_id", nullable = false)
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public Announcement getAnnouncement() {
        return announcement;
    }

    public void setAnnouncement(final Announcement announcement) {
        this.announcement = announcement;
    }

    public Organisation getOrganisation() {
        return organisation;
    }

    public void setOrganisation(final Organisation targetOrganisation) {
        this.organisation = targetOrganisation;
    }

    public OccupationType getOccupationType() {
        return occupationType;
    }

    public void setOccupationType(final OccupationType targetOccupationType) {
        this.occupationType = targetOccupationType;
    }
}
