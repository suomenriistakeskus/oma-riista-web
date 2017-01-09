package fi.riista.feature.huntingclub.hunting.rejection;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.Observation_;
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
@Table(name = "group_observation_rejection")
public class ObservationRejection extends LifecycleEntity<Long> {

    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hunting_club_group_id", nullable = false)
    private HuntingClubGroup group;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Observation observation;

    protected ObservationRejection() {
        // Default constructor for JPA persistence provider
    }

    public ObservationRejection(final HuntingClubGroup group, final Observation observation) {
        setGroup(group);
        setObservation(observation);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_observation_rejection_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public HuntingClubGroup getGroup() {
        return group;
    }

    public void setGroup(final HuntingClubGroup group) {
        CriteriaUtils.updateInverseCollection(HuntingClubGroup_.observationRejections, this, this.group, group);
        this.group = group;
    }

    public Observation getObservation() {
        return observation;
    }

    public void setObservation(final Observation observation) {
        CriteriaUtils.updateInverseCollection(Observation_.groupRejections, this, this.observation, observation);
        this.observation = observation;
    }

}
