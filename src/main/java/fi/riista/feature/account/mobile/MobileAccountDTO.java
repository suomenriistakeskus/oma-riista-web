package fi.riista.feature.account.mobile;

import fi.riista.feature.account.AccountShootingTestDTO;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.address.AddressDTO;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.util.DateUtil;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class MobileAccountDTO {

    public static MobileAccountDTO create(@Nonnull final String username,
                                          @Nonnull final Person person,
                                          @Nullable final Address address,
                                          @Nullable final Riistanhoitoyhdistys rhy,
                                          @Nonnull final SortedSet<Integer> harvestYears,
                                          @Nonnull final SortedSet<Integer> observationYears,
                                          @Nonnull final List<MobileOccupationDTO> occupations,
                                          final boolean shootingTestsEnabled,
                                          final boolean deerPilotUser,
                                          @Nullable final String qrCode,
                                          @Nonnull final List<AccountShootingTestDTO> shootingTests) {

        // Instead of traversing entity graph here, all the needed entities are
        // lifted up as parameters in order to not introduce hidden N+1 issues.

        final MobileAccountDTO dto = new MobileAccountDTO();

        dto.setUsername(username);
        dto.setFirstName(person.getFirstName());
        dto.setLastName(person.getLastName());

        dto.setBirthDate(person.parseDateOfBirth());

        dto.setAddress(AddressDTO.from(address));

        // TODO Introduce home municipality as method parameter.
        dto.setHomeMunicipality(person.getHomeMunicipalityName().asMap());

        if (rhy != null) {
            dto.setRhy(MobileOrganisationDTO.create(rhy));
        }

        dto.setHunterNumber(person.getHunterNumber());
        dto.setHunterExamDate(person.getHunterExamDate());
        dto.setHuntingCardStart(person.getHuntingCardStart());
        dto.setHuntingCardEnd(person.getHuntingCardEnd());
        dto.setHuntingCardValidNow(person.isHuntingCardValidNow() || person.isHuntingCardValidInFuture());
        if (person.isHuntingBanActiveNow()) {
            dto.setHuntingBanStart(person.getHuntingBanStart());
            dto.setHuntingBanEnd(person.getHuntingBanEnd());
        }

        dto.setQrCode(qrCode);

        dto.setTimestamp(DateUtil.now());

        dto.getHarvestYears().addAll(harvestYears);
        dto.getObservationYears().addAll(observationYears);

        // TODO "gameDiaryYears" property is still referenced in mobile apps (Android v2.2.1 and iOS v2.3.2).
        //  Hence, this cannot be removed yet (2020).
        final SortedSet<Integer> gameDiaryYears = new TreeSet<>();
        gameDiaryYears.addAll(harvestYears);
        gameDiaryYears.addAll(observationYears);
        dto.getGameDiaryYears().addAll(gameDiaryYears);

        dto.setShootingTests(shootingTests);

        dto.getOccupations().addAll(occupations);

        dto.setEnableSrva(person.isSrvaEnabled());
        dto.setEnableShootingTests(shootingTestsEnabled);
        dto.setDeerPilotUser(deerPilotUser);

        return dto;
    }

    private String username;
    private String firstName;
    private String lastName;

    private LocalDate birthDate;

    private AddressDTO address;
    private Map<String, String> homeMunicipality;
    private MobileOrganisationDTO rhy;

    private String hunterNumber;
    private LocalDate hunterExamDate;
    private LocalDate huntingCardStart;
    private LocalDate huntingCardEnd;
    private LocalDate huntingBanStart;
    private LocalDate huntingBanEnd;
    private boolean huntingCardValidNow;

    private String qrCode;

    private DateTime timestamp;

    private final SortedSet<Integer> gameDiaryYears = new TreeSet<>();
    private final SortedSet<Integer> harvestYears = new TreeSet<>();
    private final SortedSet<Integer> observationYears = new TreeSet<>();

    private List<AccountShootingTestDTO> shootingTests;

    private final List<MobileOccupationDTO> occupations = new ArrayList<>();

    private boolean enableSrva;
    private boolean enableShootingTests;
    private boolean deerPilotUser;

    private MobileAccountDTO() {
    }

    // Accessors -->

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(final LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public AddressDTO getAddress() {
        return address;
    }

    public void setAddress(final AddressDTO address) {
        this.address = address;
    }

    public Map<String, String> getHomeMunicipality() {
        return homeMunicipality;
    }

    public void setHomeMunicipality(final Map<String, String> homeMunicipality) {
        this.homeMunicipality = homeMunicipality;
    }

    public MobileOrganisationDTO getRhy() {
        return rhy;
    }

    public void setRhy(final MobileOrganisationDTO rhy) {
        this.rhy = rhy;
    }

    public String getHunterNumber() {
        return hunterNumber;
    }

    public void setHunterNumber(final String hunterNumber) {
        this.hunterNumber = hunterNumber;
    }

    public LocalDate getHunterExamDate() {
        return hunterExamDate;
    }

    public void setHunterExamDate(final LocalDate hunterExamDate) {
        this.hunterExamDate = hunterExamDate;
    }

    public LocalDate getHuntingCardStart() {
        return huntingCardStart;
    }

    public void setHuntingCardStart(final LocalDate huntingCardStart) {
        this.huntingCardStart = huntingCardStart;
    }

    public LocalDate getHuntingCardEnd() {
        return huntingCardEnd;
    }

    public void setHuntingCardEnd(final LocalDate huntingCardEnd) {
        this.huntingCardEnd = huntingCardEnd;
    }

    public LocalDate getHuntingBanStart() {
        return huntingBanStart;
    }

    public void setHuntingBanStart(final LocalDate huntingBanStart) {
        this.huntingBanStart = huntingBanStart;
    }

    public LocalDate getHuntingBanEnd() {
        return huntingBanEnd;
    }

    public void setHuntingBanEnd(final LocalDate huntingBanEnd) {
        this.huntingBanEnd = huntingBanEnd;
    }

    public boolean isHuntingCardValidNow() {
        return huntingCardValidNow;
    }

    public void setHuntingCardValidNow(final boolean huntingCardValidNow) {
        this.huntingCardValidNow = huntingCardValidNow;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(final String qrCode) {
        this.qrCode = qrCode;
    }

    public DateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final DateTime timestamp) {
        this.timestamp = timestamp;
    }

    public SortedSet<Integer> getGameDiaryYears() {
        return gameDiaryYears;
    }

    public SortedSet<Integer> getHarvestYears() {
        return harvestYears;
    }

    public SortedSet<Integer> getObservationYears() {
        return observationYears;
    }

    public List<AccountShootingTestDTO> getShootingTests() {
        return shootingTests;
    }

    public void setShootingTests(final List<AccountShootingTestDTO> shootingTests) {
        this.shootingTests = shootingTests;
    }

    public List<MobileOccupationDTO> getOccupations() {
        return occupations;
    }

    public boolean isEnableSrva() {
        return enableSrva;
    }

    public void setEnableSrva(final boolean enableSrva) {
        this.enableSrva = enableSrva;
    }

    public boolean isEnableShootingTests() {
        return enableShootingTests;
    }

    public void setEnableShootingTests(final boolean enableShootingTests) {
        this.enableShootingTests = enableShootingTests;
    }

    public boolean isDeerPilotUser() {
        return deerPilotUser;
    }

    public void setDeerPilotUser(final boolean enableDeerPilot) {
        this.deerPilotUser = enableDeerPilot;
    }
}
