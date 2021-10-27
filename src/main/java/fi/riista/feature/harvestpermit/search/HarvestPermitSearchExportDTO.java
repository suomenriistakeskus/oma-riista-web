package fi.riista.feature.harvestpermit.search;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitContactPerson;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountDTO;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import fi.riista.util.LocalisedString;
import fi.riista.validation.FinnishHunterNumber;
import fi.riista.validation.FinnishHuntingPermitNumber;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class HarvestPermitSearchExportDTO {

    static class ContactPersonDTO {

        @FinnishHunterNumber
        private final String hunterNumber;

        @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
        private final String firstName;

        @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
        private final String lastName;

        @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
        private final String phoneNumber;

        @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
        private final String email;

        public ContactPersonDTO(final Person person) {
            this.hunterNumber = person.getHunterNumber();
            this.firstName = person.getFirstName();
            this.lastName = person.getLastName();
            this.phoneNumber = person.getPhoneNumber();
            this.email = person.getEmail();
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

        public String getFullName() {
            return String.format("%s %s", getFirstName(), getLastName());
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public String getEmail() {
            return email;
        }
    }

    public static List<HarvestPermitSearchExportDTO> create(@Nonnull final List<HarvestPermit> permits,
                                                            @Nonnull final Map<Long, List<HarvestPermitContactPerson>> contactPersonsByPermitId,
                                                            @Nonnull final Map<Long, Person> personsById,
                                                            @Nonnull final Map<Long, List<HarvestPermitSpeciesAmount>> speciesAmountsByPermitId,
                                                            @Nonnull final Map<Long, LocalisedString> rkaNameByRhyId,
                                                            @Nonnull final Map<Long, GeoLocation> locationByPermitId) {

        Objects.requireNonNull(permits, "permits must not be null");
        Objects.requireNonNull(contactPersonsByPermitId, "contactPersonsByPermitId must not be null");
        Objects.requireNonNull(personsById, "personsById must not be null");
        Objects.requireNonNull(speciesAmountsByPermitId, "speciesAmountsByPermitId must not be null");
        Objects.requireNonNull(rkaNameByRhyId, "rkaNameByRhyId must not be null");
        Objects.requireNonNull(locationByPermitId, "locationByPermitId must not be null");

        return permits.stream()
                .map(permit -> HarvestPermitSearchExportDTO.create(permit, contactPersonsByPermitId, personsById,
                                                                   speciesAmountsByPermitId, rkaNameByRhyId, locationByPermitId))
                .collect(toList());
    }

    public static HarvestPermitSearchExportDTO create(@Nonnull final HarvestPermit permit,
                                                      @Nonnull final Map<Long, List<HarvestPermitContactPerson>> contactPersonsByPermitId,
                                                      @Nonnull final Map<Long, Person> personsById,
                                                      @Nonnull final Map<Long, List<HarvestPermitSpeciesAmount>> speciesAmountsByPermitId,
                                                      @Nonnull final Map<Long, LocalisedString> rkaNameByRhyId,
                                                      @Nonnull final Map<Long, GeoLocation> locationByPermitId) {

        Objects.requireNonNull(permit, "permit must not be null");
        Objects.requireNonNull(contactPersonsByPermitId, "contactPersonsByPermitId must not be null");
        Objects.requireNonNull(personsById, "personsById must not be null");
        Objects.requireNonNull(speciesAmountsByPermitId, "speciesAmountsByPermitId must not be null");
        Objects.requireNonNull(rkaNameByRhyId, "rkaNameByRhyId must not be null");
        Objects.requireNonNull(locationByPermitId, "locationByPermitId must not be null");

        final HarvestPermitSearchExportDTO dto = new HarvestPermitSearchExportDTO();
        dto.setPermitNumber(permit.getPermitNumber());
        dto.setPermitType(permit.getPermitType());
        dto.setSpeciesAmounts(F.mapNonNullsToList(
                speciesAmountsByPermitId.getOrDefault(permit.getId(), emptyList()),
                HarvestPermitSpeciesAmountDTO::create));

        final List<ContactPersonDTO> contactDTOs = contactPersonsByPermitId.getOrDefault(permit.getId(), emptyList())
                .stream()
                .map(HarvestPermitContactPerson::getContactPerson)
                .filter(Objects::nonNull)
                .map(Person::getId)
                .map(personsById::get)
                .map(ContactPersonDTO::new)
                .collect(toList());

        Optional.ofNullable(permit.getOriginalContactPerson()).ifPresent(
                c -> contactDTOs.add(new ContactPersonDTO(personsById.get(c.getId()))));

        dto.setContacts(contactDTOs);
        dto.setPermitHolderName(F.mapNullable(permit.getPermitHolder(), PermitHolder::getName));
        dto.setPermitHolderType(F.mapNullable(permit.getPermitHolder(), PermitHolder::getType));

        dto.setHarvestReportState(permit.getHarvestReportState());
        dto.setRka(rkaNameByRhyId.get(permit.getRhy().getId()));

        updateValidity(dto);

        dto.setLatitude(F.mapNullable(locationByPermitId.get(permit.getId()), GeoLocation::getLatitude));
        dto.setLongitude(F.mapNullable(locationByPermitId.get(permit.getId()), GeoLocation::getLongitude));

        return dto;
    }

    private static void updateValidity(final HarvestPermitSearchExportDTO dto) {
        if (dto.getSpeciesAmounts() == null) {
            dto.setValidity(HarvestPermitValidity.UNKNOWN);
        } else {
            final Optional<LocalDate> earliestBeginDate = dto.getSpeciesAmounts().stream()
                    .map(HarvestPermitSpeciesAmountDTO::getBeginDate)
                    .min(LocalDate::compareTo);

            final Optional<LocalDate> latestEndDate = dto.getSpeciesAmounts().stream()
                    .map(s -> s.getEndDate2() == null ? s.getEndDate() : s.getEndDate2())
                    .max(LocalDate::compareTo);

            final LocalDate today = DateUtil.today();

            if (!earliestBeginDate.isPresent() || !latestEndDate.isPresent()) {
                dto.setValidity(HarvestPermitValidity.UNKNOWN);
            } else if (earliestBeginDate.get().isAfter(today)) {
                dto.setValidity(HarvestPermitValidity.FUTURE);
            } else if (latestEndDate.get().isAfter(today) || latestEndDate.get().isEqual(today)) {
                dto.setValidity(HarvestPermitValidity.ACTIVE);
            } else {
                dto.setValidity(HarvestPermitValidity.PASSED);
            }
        }
    }

    @FinnishHuntingPermitNumber
    private String permitNumber;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String permitType;

    private List<HarvestPermitSpeciesAmountDTO> speciesAmounts;

    private List<ContactPersonDTO> contacts;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String permitHolderName;

    private PermitHolder.PermitHolderType permitHolderType;

    private HarvestReportState harvestReportState;

    private HarvestPermitValidity validity;

    private LocalisedString rka;

    private Integer latitude;
    private Integer longitude;

    // Constructor

    private HarvestPermitSearchExportDTO() {
    }

    // Getters and setters

    public String getPermitNumber() {
        return permitNumber;
    }

    public void setPermitNumber(final String permitNumber) {
        this.permitNumber = permitNumber;
    }

    public String getPermitType() {
        return permitType;
    }

    public void setPermitType(final String permitType) {
        this.permitType = permitType;
    }

    public List<HarvestPermitSpeciesAmountDTO> getSpeciesAmounts() {
        return speciesAmounts;
    }

    public void setSpeciesAmounts(final List<HarvestPermitSpeciesAmountDTO> speciesAmounts) {
        this.speciesAmounts = speciesAmounts;
    }

    public List<ContactPersonDTO> getContacts() {
        return contacts;
    }

    public void setContacts(final List<ContactPersonDTO> contacts) {
        this.contacts = contacts;
    }

    public String getPermitHolderName() {
        return permitHolderName;
    }

    public void setPermitHolderName(final String permitHolderName) {
        this.permitHolderName = permitHolderName;
    }

    public PermitHolder.PermitHolderType getPermitHolderType() {
        return permitHolderType;
    }

    public void setPermitHolderType(final PermitHolder.PermitHolderType permitHolderType) {
        this.permitHolderType = permitHolderType;
    }

    public HarvestReportState getHarvestReportState() {
        return harvestReportState;
    }

    public void setHarvestReportState(final HarvestReportState harvestReportState) {
        this.harvestReportState = harvestReportState;
    }

    public HarvestPermitValidity getValidity() {
        return validity;
    }

    public void setValidity(final HarvestPermitValidity validity) {
        this.validity = validity;
    }

    public LocalisedString getRka() {
        return rka;
    }

    public void setRka(final LocalisedString rka) {
        this.rka = rka;
    }

    public Integer getLatitude() {
        return latitude;
    }

    public void setLatitude(final Integer latitude) {
        this.latitude = latitude;
    }

    public Integer getLongitude() {
        return longitude;
    }

    public void setLongitude(final Integer longitude) {
        this.longitude = longitude;
    }
}
