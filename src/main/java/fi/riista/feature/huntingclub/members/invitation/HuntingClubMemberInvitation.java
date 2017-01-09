package fi.riista.feature.huntingclub.members.invitation;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import org.joda.time.DateTime;

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
import javax.validation.constraints.NotNull;

@Entity
@Access(value = AccessType.FIELD)
public class HuntingClubMemberInvitation extends LifecycleEntity<Long> {

    private Long id;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Person person;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private HuntingClub huntingClub;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private OccupationType occupationType;

    @Column
    private DateTime userRejectedTime;

    public HuntingClubMemberInvitation() {
    }

    public HuntingClubMemberInvitation(Person person, HuntingClub huntingClub, OccupationType occupationType) {
        this.person = person;
        this.huntingClub = huntingClub;
        this.occupationType = occupationType;
    }

    public void reSend() {
        this.userRejectedTime = null;
    }

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Access(value = AccessType.PROPERTY)
    @Column(name = "hunting_club_member_invitation_id", nullable = false)
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public HuntingClub getHuntingClub() {
        return huntingClub;
    }

    public void setHuntingClub(HuntingClub huntingClub) {
        this.huntingClub = huntingClub;
    }

    public OccupationType getOccupationType() {
        return occupationType;
    }

    public void setOccupationType(OccupationType occupationType) {
        this.occupationType = occupationType;
    }

    public DateTime getUserRejectedTime() {
        return userRejectedTime;
    }

    public void setUserRejectedTime(DateTime userRejectedTime) {
        this.userRejectedTime = userRejectedTime;
    }
}
