package fi.riista.feature.organization.rhy.huntingcontrolevent.mobile;

import fi.riista.feature.account.AccountShootingTestDTO;
import fi.riista.validation.FinnishHunterNumber;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import org.joda.time.LocalDate;

public class MobileHuntingControlHunterInfoDTO {

    @Nonnull
    private String name;

    private LocalDate dateOfBirth;

    private Map<String, String> homeMunicipality;

    @FinnishHunterNumber
    private String hunterNumber;

    private boolean huntingLicenseActive;

    private LocalDate huntingLicenseDateOfPayment;

    private List<AccountShootingTestDTO> shootingTests;

    public static MobileHuntingControlHunterInfoDTO create(
            final String name,
            final LocalDate dateOfBirth,
            final Map<String, String> homeMunicipality,
            final String hunterNumber,
            final boolean huntingCardActive,
            final LocalDate huntingCardDateOfPayment,
            final List<AccountShootingTestDTO> shootingTests) {

        final MobileHuntingControlHunterInfoDTO dto = new MobileHuntingControlHunterInfoDTO();
        dto.setName(name);
        dto.setDateOfBirth(dateOfBirth);
        dto.setHomeMunicipality(homeMunicipality);
        dto.setHunterNumber(hunterNumber);
        dto.setHuntingCardActive(huntingCardActive);
        dto.setHuntingCardDateOfPayment(huntingCardDateOfPayment);
        dto.setShootingTests(shootingTests);
        return dto;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(final LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Map<String, String> getHomeMunicipality() {
        return homeMunicipality;
    }

    public void setHomeMunicipality(final Map<String, String> homeMunicipality) {
        this.homeMunicipality = homeMunicipality;
    }

    public String getHunterNumber() {
        return hunterNumber;
    }

    public void setHunterNumber(final String hunterNumber) {
        this.hunterNumber = hunterNumber;
    }

    public boolean isHuntingCardActive() {
        return huntingLicenseActive;
    }

    public void setHuntingCardActive(final boolean huntingCardActive) {
        this.huntingLicenseActive = huntingCardActive;
    }

    public LocalDate getHuntingCardDateOfPayment() {
        return huntingLicenseDateOfPayment;
    }

    public void setHuntingCardDateOfPayment(final LocalDate huntingCardDateOfPayment) {
        this.huntingLicenseDateOfPayment = huntingCardDateOfPayment;
    }

    public List<AccountShootingTestDTO> getShootingTests() {
        return shootingTests;
    }

    public void setShootingTests(final List<AccountShootingTestDTO> shootingTests) {
        this.shootingTests = shootingTests;
    }
}
