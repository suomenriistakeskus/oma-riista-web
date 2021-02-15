package fi.riista.feature.harvestpermit.list;

import fi.riista.feature.common.entity.HasID;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.harvestpermit.HarvestPermitContactPersonDTO;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.PermitHolderDTO;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.util.F;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class ListPermitApplicationDTO implements HasID<Long> {

    public static ListPermitApplicationDTO create(final @Nonnull HarvestPermitApplication application) {
        requireNonNull(application);

        final OrganisationNameDTO huntingClub = Optional.ofNullable(application.getHuntingClub())
                .map(OrganisationNameDTO::createWithOfficialCode)
                .orElse(null);

        final PermitHolderDTO permitHolder = PermitHolderDTO.createFrom(application.getPermitHolder());
        final String areaExternalId = Optional.ofNullable(application.getArea())
                .map(HarvestPermitArea::getExternalId)
                .orElse(null);

        final HarvestPermitContactPersonDTO contactPerson = HarvestPermitContactPersonDTO.create(application.getContactPerson());
        final List<ListPermitApplicationSpeciesAmountDTO> speciesAmounts = F.mapNonNullsToList(
                application.getSpeciesAmounts(), ListPermitApplicationSpeciesAmountDTO::create);

        return new ListPermitApplicationDTO(
                application.getId(),
                application.getStatus(),
                application.getHarvestPermitCategory(),
                speciesAmounts,
                application.getSubmitDate(),
                application.getApplicationNumber(),
                application.getApplicationName(),
                areaExternalId,
                permitHolder,
                huntingClub,
                contactPerson);
    }

    private ListPermitApplicationDTO(final @Nonnull Long id,
                                     final @Nonnull HarvestPermitApplication.Status status,
                                     final HarvestPermitCategory harvestPermitCategory,
                                     final @Nonnull List<ListPermitApplicationSpeciesAmountDTO> speciesAmounts,
                                     final DateTime submitDate,
                                     final Integer applicationNumber,
                                     final String applicationName,
                                     final String areaExternalId,
                                     final PermitHolderDTO permitHolder,
                                     final OrganisationNameDTO huntingClub,
                                     final HarvestPermitContactPersonDTO contactPerson) {
        this.id = requireNonNull(id);
        this.status = requireNonNull(status);
        this.harvestPermitCategory = harvestPermitCategory;
        this.speciesAmounts = requireNonNull(speciesAmounts);
        this.submitDate = submitDate;
        this.applicationNumber = applicationNumber;
        this.applicationName = applicationName;
        this.areaExternalId = areaExternalId;
        this.contactPerson = contactPerson;
        this.permitHolder = permitHolder;
        this.huntingClub = huntingClub;
    }

    private final Long id;
    private final HarvestPermitApplication.Status status;
    private final DateTime submitDate;

    private final Integer applicationNumber;
    private final String applicationName;
    private final HarvestPermitCategory harvestPermitCategory;
    private final String areaExternalId;

    private final HarvestPermitContactPersonDTO contactPerson;

    private final PermitHolderDTO permitHolder;
    private final OrganisationNameDTO huntingClub;
    private final List<ListPermitApplicationSpeciesAmountDTO> speciesAmounts;

    @Override
    public Long getId() {
        return id;
    }

    public HarvestPermitApplication.Status getStatus() {
        return status;
    }

    public DateTime getSubmitDate() {
        return submitDate;
    }

    public Integer getApplicationNumber() {
        return applicationNumber;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public HarvestPermitCategory getHarvestPermitCategory() {
        return harvestPermitCategory;
    }

    public String getAreaExternalId() {
        return areaExternalId;
    }

    public HarvestPermitContactPersonDTO getContactPerson() {
        return contactPerson;
    }

    public OrganisationNameDTO getHuntingClub() {
        return huntingClub;
    }

    public PermitHolderDTO getPermitHolder() {
        return permitHolder;
    }

    public List<ListPermitApplicationSpeciesAmountDTO> getSpeciesAmounts() {
        return speciesAmounts;
    }
}
