package fi.riista.feature.huntingclub.hunting.rejection;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.Harvest_;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.HuntingClubGroup_;
import fi.riista.util.jpa.CriteriaUtils;

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
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Access(AccessType.FIELD)
@Table(name = "group_harvest_rejection")
public class HarvestRejection extends LifecycleEntity<Long> {

    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hunting_club_group_id", nullable = false)
    private HuntingClubGroup group;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Harvest harvest;

    protected HarvestRejection() {
        // Default constructor for JPA persistence provider
    }

    public HarvestRejection(HuntingClubGroup group, Harvest harvest) {
        setGroup(group);
        setHarvest(harvest);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_harvest_rejection_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public HuntingClubGroup getGroup() {
        return group;
    }

    public void setGroup(HuntingClubGroup group) {
        CriteriaUtils.updateInverseCollection(HuntingClubGroup_.harvestRejections, this, this.group, group);
        this.group = group;
    }

    public Harvest getHarvest() {
        return harvest;
    }

    public void setHarvest(Harvest harvest) {
        CriteriaUtils.updateInverseCollection(Harvest_.groupRejections, this, this.harvest, harvest);
        this.harvest = harvest;
    }

}
