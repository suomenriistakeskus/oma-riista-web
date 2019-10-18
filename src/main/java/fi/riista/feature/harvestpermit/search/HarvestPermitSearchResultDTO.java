package fi.riista.feature.harvestpermit.search;

import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitContactPerson;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountDTO;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import fi.riista.validation.FinnishHunterNumber;
import fi.riista.validation.FinnishHuntingPermitNumber;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

public class HarvestPermitSearchResultDTO extends BaseEntityDTO<Long> {

    static class HarvestPermitContactPersonDTO {

        @FinnishHunterNumber
        private final String hunterNumber;

        @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
        private final String firstName;

        @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
        private final String lastName;

        public HarvestPermitContactPersonDTO(final Person person) {
            this.hunterNumber = person.getHunterNumber();
            this.firstName = person.getFirstName();
            this.lastName = person.getLastName();
        }

        public String getHunterNumber() {
            return hunterNumber;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }
    }

    @Nonnull
    public static List<HarvestPermitSearchResultDTO> create(@Nonnull final List<HarvestPermit> permits) {
        return F.mapNonNullsToList(permits, HarvestPermitSearchResultDTO::create);
    }

    @Nonnull
    public static HarvestPermitSearchResultDTO create(@Nonnull final HarvestPermit permit) {
        Objects.requireNonNull(permit, "permit must not be null");

        final HarvestPermitSearchResultDTO dto = new HarvestPermitSearchResultDTO();
        DtoUtil.copyBaseFields(permit, dto);

        dto.setPermitNumber(permit.getPermitNumber());
        dto.setPermitType(permit.getPermitType());
        dto.setHarvestReportState(permit.getHarvestReportState());
        dto.setSpeciesAmounts(F.mapNonNullsToList(permit.getSpeciesAmounts(), HarvestPermitSpeciesAmountDTO::create));
        dto.setContactPersons(createContactPersonDTOs(permit));

        return dto;
    }

    private static List<HarvestPermitContactPersonDTO> createContactPersonDTOs(@Nonnull final HarvestPermit permit) {
        Objects.requireNonNull(permit, "permit must not be null");

        final List<HarvestPermitContactPersonDTO> dtos = permit.getContactPersons().stream()
                .map(HarvestPermitContactPerson::getContactPerson)
                .map(HarvestPermitContactPersonDTO::new)
                .collect(toList());
        dtos.add(new HarvestPermitContactPersonDTO(permit.getOriginalContactPerson()));

        return dtos;
    }

    private Long id;
    private Integer rev;

    @FinnishHuntingPermitNumber
    private String permitNumber;

    private HarvestReportState harvestReportState;

    private List<HarvestPermitSpeciesAmountDTO> speciesAmounts;

    private List<HarvestPermitContactPersonDTO> contactPersons;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String permitType;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return rev;
    }

    @Override
    public void setRev(Integer rev) {
        this.rev = rev;
    }

    public String getPermitNumber() {
        return permitNumber;
    }

    public void setPermitNumber(String permitNumber) {
        this.permitNumber = permitNumber;
    }

    public HarvestReportState getHarvestReportState() {
        return harvestReportState;
    }

    public void setHarvestReportState(final HarvestReportState harvestReportState) {
        this.harvestReportState = harvestReportState;
    }

    public void setSpeciesAmounts(List<HarvestPermitSpeciesAmountDTO> speciesAmounts) {
        this.speciesAmounts = speciesAmounts;
    }

    public List<HarvestPermitSpeciesAmountDTO> getSpeciesAmounts() {
        return speciesAmounts;
    }

    public List<HarvestPermitContactPersonDTO> getContactPersons() {
        return contactPersons;
    }

    public void setContactPersons(List<HarvestPermitContactPersonDTO> contactPersons) {
        this.contactPersons = contactPersons;
    }

    public void setPermitType(String permitType) {
        this.permitType = permitType;
    }

    public String getPermitType() {
        return permitType;
    }
}
