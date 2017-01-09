package fi.riista.feature.gamediary.srva.method;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.gamediary.srva.SrvaEvent;
import fi.riista.feature.gamediary.srva.SrvaEvent_;
import fi.riista.util.jpa.CriteriaUtils;

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
@Access(AccessType.FIELD)
public class SrvaMethod extends LifecycleEntity<Long> {

    public static final String ID_COLUMN_NAME = "srva_method_id";

    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SrvaMethodEnum name;

    @Column(nullable = false)
    private boolean isChecked;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "srva_event_id")
    private SrvaEvent event;

    public SrvaMethod() {
    }

    public SrvaMethod(SrvaEvent event) {
        setEvent(event);
    }

    public SrvaMethodEnum getName() {
        return name;
    }

    public void setName(SrvaMethodEnum name) {
        this.name = name;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public SrvaEvent getEvent() {
        return event;
    }

    public void setEvent(SrvaEvent event) {
        CriteriaUtils.updateInverseCollection(SrvaEvent_.methods, this, this.event, event);
        this.event = event;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = ID_COLUMN_NAME, nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public boolean isEqualBusinessFields(SrvaMethod other) {
        return other != null && other.getName() == getName() && other.isChecked() == isChecked();
    }

    @Override
    public String toString() {
        return "SrvaMethod{" +
                "id=" + id +
                ", name=" + name +
                ", isChecked=" + isChecked +
                ", event=" + (event == null ? "null" : event.getId()) +
                '}';
    }
}
