package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.organization.address.AddressDTO;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.util.DateUtil;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public abstract class MobileAccountDTO {

    private String username;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private Map<String, String> homeMunicipality;

    private AddressDTO address;
    private MobileOrganisationDTO rhy;

    private final SortedSet<Integer> gameDiaryYears = new TreeSet<>();
    private final List<MobileOccupationDTO> occupations = new ArrayList<>();

    private String hunterNumber;
    private LocalDate hunterExamDate;
    private LocalDate huntingCardStart;
    private LocalDate huntingCardEnd;
    private LocalDate huntingBanStart;
    private LocalDate huntingBanEnd;
    private boolean huntingCardValidNow;

    private DateTime timestamp;

    protected void populateWith(
            @Nonnull final String username,
            @Nonnull final Person person,
            @Nonnull final Address address,
            @Nonnull final Riistanhoitoyhdistys rhy,
            @Nonnull final SortedSet<Integer> gameDiaryYears,
            @Nonnull final Collection<MobileOccupationDTO> occupations) {

        // Instead of traversing entity graph here, all the needed entities are
        // lifted up as parameters in order to not introduce hidden N+1 issues.

        setUsername(username);

        setFirstName(person.getFirstName());
        setLastName(person.getLastName());
        setBirthDate(person.parseDateOfBirth());
        setHomeMunicipality(person.getHomeMunicipalityName().asMap());
        setAddress(AddressDTO.from(address));

        getGameDiaryYears().addAll(gameDiaryYears);

        if (rhy != null) {
            setRhy(MobileOrganisationDTO.create(rhy));
        }

        setHunterNumber(person.getHunterNumber());
        setHunterExamDate(person.getHunterExamDate());
        setHuntingCardStart(person.getHuntingCardStart());
        setHuntingCardEnd(person.getHuntingCardEnd());
        setHuntingCardValidNow(person.isHuntingCardValidNow() || person.isHuntingCardValidInFuture());
        if (person.isHuntingBanActiveNow()) {
            setHuntingBanStart(person.getHuntingBanStart());
            setHuntingBanEnd(person.getHuntingBanEnd());
        }

        setTimestamp(DateUtil.now());

        getOccupations().addAll(occupations);
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

    public Map<String, String> getHomeMunicipality() {
        return homeMunicipality;
    }

    public void setHomeMunicipality(final Map<String, String> homeMunicipality) {
        this.homeMunicipality = homeMunicipality;
    }

    public AddressDTO getAddress() {
        return address;
    }

    public void setAddress(final AddressDTO address) {
        this.address = address;
    }

    public MobileOrganisationDTO getRhy() {
        return rhy;
    }

    public void setRhy(final MobileOrganisationDTO rhy) {
        this.rhy = rhy;
    }

    public SortedSet<Integer> getGameDiaryYears() {
        return gameDiaryYears;
    }

    public List<MobileOccupationDTO> getOccupations() {
        return occupations;
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

    public DateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final DateTime timestamp) {
        this.timestamp = timestamp;
    }

}
