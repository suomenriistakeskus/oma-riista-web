package fi.riista.feature.organization.jht.nomination;

import com.google.common.base.Preconditions;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.common.entity.PersistableEnum;
import fi.riista.feature.common.entity.PersistableEnumConverter;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.util.DateUtil;
import fi.riista.util.LocalisedEnum;
import org.joda.time.LocalDate;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Converter;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.util.EnumSet;
import java.util.Objects;

@Entity
@Access(value = AccessType.FIELD)
public class OccupationNomination extends LifecycleEntity<Long> {

    @Converter
    public static class NominationStatusConverter implements PersistableEnumConverter<NominationStatus> {
    }

    public enum NominationStatus implements LocalisedEnum, PersistableEnum {
        EHDOLLA("A"),
        ESITETTY("E"),
        HYLATTY("H"),
        NIMITETTY("N");

        private final String databaseValue;

        NominationStatus(final String databaseValue) {
            this.databaseValue = databaseValue;
        }

        @Override
        public String getDatabaseValue() {
            return databaseValue;
        }

        public boolean isFinal() {
            return this == HYLATTY || this == NIMITETTY;
        }
    }

    private Long id;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OccupationType occupationType;

    @NotNull
    @Column(nullable = false, length = 1)
    @Convert(converter = NominationStatusConverter.class)
    private NominationStatus nominationStatus;

    @Column
    private LocalDate nominationDate;

    @Column
    private LocalDate decisionDate;

    @NotNull
    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Riistanhoitoyhdistys rhy;

    @NotNull
    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Person person;

    // Person who made the proposal of the nomination (coordinator in rhy)
    @ManyToOne(fetch = FetchType.LAZY)
    private Person rhyPerson;

    @ManyToOne(fetch = FetchType.LAZY)
    private SystemUser moderatorUser;

    @ManyToOne(fetch = FetchType.LAZY)
    private Occupation occupation;

    @Transient
    public boolean canCancel() {
        return EnumSet.of(NominationStatus.ESITETTY, NominationStatus.EHDOLLA).contains(this.nominationStatus);
    }

    @Transient
    private void assertStatus(final NominationStatus allowed) {
        assertStatus(EnumSet.of(allowed));
    }

    @Transient
    private void assertStatus(final EnumSet<NominationStatus> allowed) {
        Preconditions.checkState(allowed.contains(this.nominationStatus),
                "nominationStatus should be %s was %s",
                allowed, this.nominationStatus);
    }

    public void propose(final Person rhyPerson) {
        assertStatus(NominationStatus.EHDOLLA);

        this.nominationStatus = NominationStatus.ESITETTY;
        this.nominationDate = DateUtil.today();
        this.rhyPerson = Objects.requireNonNull(rhyPerson, "rhyPerson is null");
    }

    public void acceptByModerator(final SystemUser moderatorUser, final Occupation occupation) {
        assertStatus(NominationStatus.ESITETTY);

        Preconditions.checkState(getPerson().isAdult(), "cannot accept if person is under-aged");
        Preconditions.checkState(!getPerson().isHuntingBanActiveNow(), "cannot accept if hunting ban is active");

        this.moderatorUser = Objects.requireNonNull(moderatorUser, "moderatorUser is null");
        this.occupation = Objects.requireNonNull(occupation, "occupation is null");
        this.nominationStatus = NominationStatus.NIMITETTY;
        this.decisionDate = DateUtil.today();
    }

    public void rejectByModerator(final SystemUser moderatorUser) {
        assertStatus(NominationStatus.ESITETTY);

        this.moderatorUser = Preconditions.checkNotNull(moderatorUser, "moderatorUser is null");
        this.nominationStatus = NominationStatus.HYLATTY;
        this.decisionDate = DateUtil.today();
    }

    // Accessors -->

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Access(value = AccessType.PROPERTY)
    @Column(name = "occupation_nomination_id", nullable = false)
    public Long getId() {
        return this.id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public OccupationType getOccupationType() {
        return occupationType;
    }

    public void setOccupationType(final OccupationType occupationType) {
        this.occupationType = occupationType;
    }

    public NominationStatus getNominationStatus() {
        return nominationStatus;
    }

    public void setNominationStatus(final NominationStatus nominationStatus) {
        this.nominationStatus = nominationStatus;
    }

    public LocalDate getNominationDate() {
        return nominationDate;
    }

    public void setNominationDate(final LocalDate nominationDate) {
        this.nominationDate = nominationDate;
    }

    public LocalDate getDecisionDate() {
        return decisionDate;
    }

    public void setDecisionDate(final LocalDate decisionDate) {
        this.decisionDate = decisionDate;
    }

    public Riistanhoitoyhdistys getRhy() {
        return rhy;
    }

    public void setRhy(final Riistanhoitoyhdistys rhy) {
        this.rhy = rhy;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(final Person person) {
        this.person = person;
    }

    public Person getRhyPerson() {
        return rhyPerson;
    }

    public void setRhyPerson(final Person rhyPerson) {
        this.rhyPerson = rhyPerson;
    }

    public SystemUser getModeratorUser() {
        return moderatorUser;
    }

    public void setModeratorUser(final SystemUser moderatorUser) {
        this.moderatorUser = moderatorUser;
    }

    public Occupation getOccupation() {
        return occupation;
    }

    public void setOccupation(final Occupation occupation) {
        this.occupation = occupation;
    }
}
