package fi.riista.feature.harvestpermit.season;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.util.LocalisedString;
import org.locationtech.jts.geom.Geometry;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_GREY_SEAL;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_RINGED_SEAL;

@Entity
@Access(AccessType.FIELD)
public class HarvestArea extends LifecycleEntity<Long> {

    public enum HarvestAreaType {
        HALLIALUE,
        PORONHOITOALUE,
        NORPPAALUE;

        public static HarvestAreaType getValidTypeFor(final int speciesCode){
            switch (speciesCode){
                case OFFICIAL_CODE_BEAR:
                    return PORONHOITOALUE;
                case OFFICIAL_CODE_GREY_SEAL:
                    return HALLIALUE;
                case OFFICIAL_CODE_RINGED_SEAL:
                    return NORPPAALUE;
                default:
                    return null;
            }
        }
    }

    public enum HarvestAreaDetailedType {
        PORONHOITOALUE_ITAINEN,
        PORONHOITOALUE_LANTINEN;

        public static HarvestAreaDetailedType getByName(final String name) {
            if (name.equals("Itäinen poronhoitoalue")) {
                return PORONHOITOALUE_ITAINEN;
            } else if (name.equals("Läntinen poronhoitoalue")) {
                return PORONHOITOALUE_LANTINEN;
            }

            return null;
        }
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

    // Mapped to entity field only in tests.
    @Transient
    @Column(name = "geom")
    private Geometry geometry;

    public HarvestArea() {
    }

    // For testing
    public HarvestArea(final HarvestAreaType type,
                       final String nameFinnish,
                       final String nameSwedish,
                       final Geometry geometry) {
        this.type = type;
        this.nameFinnish = nameFinnish;
        this.nameSwedish = nameSwedish;
        this.geometry = geometry;
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

    public Geometry getGeometry() {
        return geometry;
    }
}
