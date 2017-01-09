package fi.riista.feature.harvestpermit;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.organization.person.Person;

import fi.riista.util.jpa.CriteriaUtils;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

@Entity
@Access(AccessType.FIELD)
public class HarvestPermitContactPerson extends LifecycleEntity<Long> {
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HarvestPermit harvestPermit;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Person contactPerson;

    public HarvestPermitContactPerson() {
    }

    public HarvestPermitContactPerson(HarvestPermit harvestPermit, Person contactPerson) {
        setHarvestPermit(harvestPermit);
        setContactPerson(contactPerson);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "harvest_permit_contact_person_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public HarvestPermit getHarvestPermit() {
        return harvestPermit;
    }

    public void setHarvestPermit(HarvestPermit harvestPermit) {
        CriteriaUtils.updateInverseCollection(HarvestPermit_.contactPersons, this, this.harvestPermit, harvestPermit);
        this.harvestPermit = harvestPermit;
    }

    public Person getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(Person contactPerson) {
        this.contactPerson = contactPerson;
    }
}
