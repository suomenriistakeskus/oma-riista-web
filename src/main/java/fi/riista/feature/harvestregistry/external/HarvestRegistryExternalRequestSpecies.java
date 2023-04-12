package fi.riista.feature.harvestregistry.external;

import fi.riista.feature.common.entity.BaseEntity;

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

@Entity
@Access(value = AccessType.FIELD)
public class HarvestRegistryExternalRequestSpecies extends BaseEntity<Long> {

    private static final String ID_COLUMN_NAME = "harvest_registry_api_request_species_id";

    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false, name = "harvest_registry_api_request_id")
    private HarvestRegistryExternalRequest request;

    @NotNull
    @Column(nullable = false)
    private Integer species;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = ID_COLUMN_NAME, nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public HarvestRegistryExternalRequest getRequest() {
        return request;
    }

    public void setRequest(final HarvestRegistryExternalRequest request) {
        this.request = request;
    }

    public Integer getSpecies() {
        return species;
    }

    public void setSpecies(final Integer species) {
        this.species = species;
    }
}
