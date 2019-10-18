package fi.riista.feature.harvestpermit.list;

import fi.riista.feature.common.entity.HasID;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitContactPerson;
import fi.riista.feature.harvestpermit.HarvestPermitContactPersonDTO;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountDTO;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.util.F;

import javax.annotation.Nonnull;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class ListHarvestPermitDTO implements HasID<Long> {

    @Nonnull
    public static ListHarvestPermitDTO create(final @Nonnull HarvestPermit permit) {
        requireNonNull(permit);

        final List<HarvestPermitSpeciesAmountDTO> speciesAmounts = F.mapNonNullsToList(
                permit.getSpeciesAmounts(), HarvestPermitSpeciesAmountDTO::create);

        final List<HarvestPermitContactPersonDTO> contactPersons = permit.getContactPersons().stream()
                .map(HarvestPermitContactPerson::getContactPerson)
                .map(HarvestPermitContactPersonDTO::create)
                .collect(toList());
        contactPersons.add(HarvestPermitContactPersonDTO.create(permit.getOriginalContactPerson()));

        return new ListHarvestPermitDTO(
                permit.getId(),
                permit.getPermitType(),
                permit.getPermitNumber(),
                speciesAmounts,
                contactPersons,
                permit.getHarvestReportState());
    }

    private ListHarvestPermitDTO(final @Nonnull Long id,
                                 final @Nonnull String permitType,
                                 final @Nonnull String permitNumber,
                                 final @Nonnull List<HarvestPermitSpeciesAmountDTO> speciesAmounts,
                                 final @Nonnull List<HarvestPermitContactPersonDTO> contactPersons,
                                 final HarvestReportState harvestReportState) {
        this.id = requireNonNull(id);
        this.permitType = requireNonNull(permitType);
        this.permitNumber = requireNonNull(permitNumber);
        this.speciesAmounts = requireNonNull(speciesAmounts);
        this.contactPersons = requireNonNull(contactPersons);
        this.harvestReportState = harvestReportState;
    }

    private final Long id;
    private final String permitType;
    private final String permitNumber;
    private final HarvestReportState harvestReportState;
    private final List<HarvestPermitSpeciesAmountDTO> speciesAmounts;
    private final List<HarvestPermitContactPersonDTO> contactPersons;

    @Override
    public Long getId() {
        return id;
    }

    public String getPermitType() {
        return permitType;
    }

    public String getPermitNumber() {
        return permitNumber;
    }

    public HarvestReportState getHarvestReportState() {
        return harvestReportState;
    }

    public List<HarvestPermitSpeciesAmountDTO> getSpeciesAmounts() {
        return speciesAmounts;
    }

    public List<HarvestPermitContactPersonDTO> getContactPersons() {
        return contactPersons;
    }
}
