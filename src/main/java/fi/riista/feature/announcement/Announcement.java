package fi.riista.feature.announcement;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.organization.Organisation;
import javax.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.SafeHtml;

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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Objects;

@Entity
@Access(value = AccessType.FIELD)
public class Announcement extends LifecycleEntity<Long> {

    @NotBlank
    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(nullable = false)
    private String subject;

    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "text", nullable = false)
    private String body;

    @Column(nullable = false)
    private boolean visibleToAll;

    @NotNull
    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private SystemUser fromUser;

    @NotNull
    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Organisation fromOrganisation;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AnnouncementSenderType senderType;

    // Mapped for QueryDSL
    @OneToMany(mappedBy = "announcement")
    private List<AnnouncementSubscriber> subscribers;

    @ManyToOne(fetch = FetchType.LAZY)
    private Organisation rhyMembershipSubscriber;

    private Long id;

    // For Hibernate
    protected Announcement() {
    }

    public Announcement(final String subject,
                        final String body,
                        final SystemUser fromUser,
                        final Organisation fromOrganisation,
                        final AnnouncementSenderType senderType) {
        this.subject = Objects.requireNonNull(subject);
        this.body = Objects.requireNonNull(body);
        this.fromUser = Objects.requireNonNull(fromUser);
        this.fromOrganisation = Objects.requireNonNull(fromOrganisation);
        this.senderType = Objects.requireNonNull(senderType);
    }

    // Accessors -->

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Access(value = AccessType.PROPERTY)
    @Column(name = "announcement_id", nullable = false)
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(final String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(final String body) {
        this.body = body;
    }

    public boolean isVisibleToAll() {
        return visibleToAll;
    }

    public void setVisibleToAll(final boolean visibleToAll) {
        this.visibleToAll = visibleToAll;
    }

    public SystemUser getFromUser() {
        return fromUser;
    }

    public void setFromUser(final SystemUser fromUser) {
        this.fromUser = fromUser;
    }

    public Organisation getFromOrganisation() {
        return fromOrganisation;
    }

    public void setFromOrganisation(final Organisation fromOrganisation) {
        this.fromOrganisation = fromOrganisation;
    }

    public AnnouncementSenderType getSenderType() {
        return senderType;
    }

    public void setSenderType(final AnnouncementSenderType fromOccupationType) {
        this.senderType = fromOccupationType;
    }

    public Organisation getRhyMembershipSubscriber() {
        return rhyMembershipSubscriber;
    }

    public void setRhyMembershipSubscriber(final Organisation rhyMembershipSubscriber) {
        this.rhyMembershipSubscriber = rhyMembershipSubscriber;
    }
}
