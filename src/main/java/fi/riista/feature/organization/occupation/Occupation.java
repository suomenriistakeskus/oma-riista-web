package fi.riista.feature.organization.occupation;

import com.querydsl.core.annotations.QueryDelegate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimeExpression;
import fi.riista.feature.common.entity.HasBeginAndEndDate;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.person.ContactInfoShare;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDate;

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
import javax.validation.constraints.Size;
import java.util.Objects;

import static com.querydsl.core.types.dsl.DateTimeExpression.currentDate;

@Entity
@Access(value = AccessType.FIELD)
public class Occupation extends LifecycleEntity<Long> implements HasBeginAndEndDate {

    public static final boolean FOREIGN_PERSON_ELIGIBLE_FOR_OCCUPATION = false;

    private Long id;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Person person;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Organisation organisation;

    @Column
    private LocalDate beginDate;

    @Column
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private OccupationType occupationType;

    @Column
    private Integer callOrder;

    @Column
    private Integer qualificationYear;

    @Size(max = 255)
    @Column(length = 255)
    private String additionalInfo;

    @Enumerated(EnumType.STRING)
    @Column
    private ContactInfoShare contactInfoShare;

    public Occupation() {
    }

    public Occupation(Person p, Organisation o, OccupationType t) {
        this.person = p;
        setOrganisationAndOccupationType(o, t);
    }

    public Occupation(Person p, Organisation o, OccupationType t, ContactInfoShare share, Integer callOrder) {
        this.person = p;
        this.callOrder = callOrder;
        this.contactInfoShare = share;
        setOrganisationAndOccupationType(o, t);
    }

    public Occupation(Person p, Organisation o, OccupationType t, ContactInfoShare share) {
        this.person = p;
        this.contactInfoShare = share;
        setOrganisationAndOccupationType(o, t);
    }

    public void setOrganisationAndOccupationType(Organisation organisation, OccupationType occupationType) {
        Objects.requireNonNull(organisation, "Occupation.organisation must not be null");
        Objects.requireNonNull(occupationType, "Occupation.occupationType must not be null");
        if (!occupationType.isApplicableFor(organisation.getOrganisationType())) {
            throw new OccupationNotApplicableForOrganisationException();
        }
        this.organisation = organisation;
        this.occupationType = occupationType;
    }

    public boolean isValidNow() {
        return DateUtil.overlapsInclusive(getBeginDate(), getEndDate(), DateUtil.today());
    }

    // Accessors -->

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Access(value = AccessType.PROPERTY)
    @Column(name = "occupation_id", nullable = false)
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

    public Organisation getOrganisation() {
        return organisation;
    }

    @Override
    public LocalDate getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(LocalDate beginDate) {
        this.beginDate = beginDate;
    }

    @Override
    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public OccupationType getOccupationType() {
        return occupationType;
    }

    public Integer getCallOrder() {
        return callOrder;
    }

    public void setCallOrder(Integer callOrder) {
        this.callOrder = callOrder;
    }

    public Integer getQualificationYear() {
        return qualificationYear;
    }

    public void setQualificationYear(Integer qualificationYear) {
        this.qualificationYear = qualificationYear;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public ContactInfoShare getContactInfoShare() {
        return contactInfoShare;
    }

    public void setContactInfoShare(ContactInfoShare contactInfoShare) {
        this.contactInfoShare = contactInfoShare;
    }

    // Querydsl delegates -->

    @QueryDelegate(Occupation.class)
    public static BooleanExpression validAndNotDeleted(QOccupation occupation) {
        return notDeleted(occupation).and(valid(occupation));
    }

    @QueryDelegate(Occupation.class)
    public static BooleanExpression valid(QOccupation occupation) {
        return validOnDate(occupation, currentDate(LocalDate.class));
    }

    @QueryDelegate(Occupation.class)
    public static BooleanExpression validOnDate(QOccupation occupation, DateTimeExpression<LocalDate> date) {
        return date.between(occupation.beginDate.coalesce(currentDate()), occupation.endDate.coalesce(currentDate()));
    }

    @QueryDelegate(Occupation.class)
    public static BooleanExpression notDeleted(QOccupation occupation) {
        return occupation.lifecycleFields.deletionTime.isNull();
    }
}
