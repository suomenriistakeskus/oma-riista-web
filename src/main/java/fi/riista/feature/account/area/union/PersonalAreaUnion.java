package fi.riista.feature.account.area.union;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.permit.area.HarvestPermitArea;
import javax.validation.constraints.NotBlank;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Access(value = AccessType.FIELD)
public class PersonalAreaUnion extends LifecycleEntity<Long> {

    private Long id;

    @NotNull
    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Person person;

    @NotNull
    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HarvestPermitArea harvestPermitArea;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String name;

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Access(value = AccessType.PROPERTY)
    @Column(name = "personal_area_union_id", nullable = false)
    public Long getId() {
        return this.id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(final Person person) {
        this.person = person;
    }

    public HarvestPermitArea getHarvestPermitArea() {
        return harvestPermitArea;
    }

    public void setHarvestPermitArea(final HarvestPermitArea harvestPermitArea) {
        this.harvestPermitArea = harvestPermitArea;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
