package fi.riista.feature.harvestpermit.season;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.util.LocalisedString;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Access(AccessType.FIELD)
public class HarvestArea extends LifecycleEntity<Long> {

    public enum HarvestAreaType {
        HALLIALUE,
        PORONHOITOALUE
    }

    private static final String ID_COLUMN_NAME = "harvest_area_id";

    private Long id;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "type", nullable = false)
    private HarvestAreaType type;

    @Size(max = 255)
    @Column
    private String officialCode;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String nameFinnish;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String nameSwedish;

    @Column
    private Date beginDate;
    @Column
    private Date endDate;

    @ManyToMany
    @JoinTable(name = "harvest_area_rhys",
            joinColumns = {@JoinColumn(name = ID_COLUMN_NAME, referencedColumnName = ID_COLUMN_NAME)},
            inverseJoinColumns = {@JoinColumn(name = Organisation.ID_COLUMN_NAME, referencedColumnName = Organisation.ID_COLUMN_NAME)}
    )
    private Set<Riistanhoitoyhdistys> rhys = new HashSet<>();

    public HarvestArea() {
    }

    public HarvestArea(HarvestAreaType type, String nameFinnish, String nameSwedish, Set<Riistanhoitoyhdistys> rhys) {
        this.type = type;
        this.nameFinnish = nameFinnish;
        this.nameSwedish = nameSwedish;
        this.rhys = rhys;
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

    public HarvestAreaType getType() {
        return type;
    }

    public void setType(HarvestAreaType type) {
        this.type = type;
    }

    public String getOfficialCode() {
        return officialCode;
    }

    public void setOfficialCode(String officialCode) {
        this.officialCode = officialCode;
    }

    public String getNameFinnish() {
        return nameFinnish;
    }

    public void setNameFinnish(String nameFinnish) {
        this.nameFinnish = nameFinnish;
    }

    public String getNameSwedish() {
        return nameSwedish;
    }

    public void setNameSwedish(String nameSwedish) {
        this.nameSwedish = nameSwedish;
    }

    public LocalisedString getNameLocalisation() {
        return LocalisedString.of(nameFinnish, nameSwedish);
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Set<Riistanhoitoyhdistys> getRhys() {
        return rhys;
    }

    public void setRhys(Set<Riistanhoitoyhdistys> rhys) {
        this.rhys = rhys;
    }
}
