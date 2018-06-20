package fi.riista.feature.account.mobile;

import fi.riista.feature.account.AccountShootingTestDTO;
import fi.riista.feature.gamediary.mobile.MobileOccupationDTO;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class MobileAccountV2DTO extends MobileAccountDTO {

    public static MobileAccountV2DTO create(@Nonnull final String username,
                                            @Nonnull final Person person,
                                            @Nonnull final Address address,
                                            @Nonnull final Riistanhoitoyhdistys rhy,
                                            @Nonnull final SortedSet<Integer> harvestYears,
                                            @Nonnull final SortedSet<Integer> observationYears,
                                            @Nonnull final List<MobileOccupationDTO> occupations,
                                            final boolean shootingTestsEnabled,
                                            final String qrCode,
                                            @Nonnull final List<AccountShootingTestDTO> shootingTests) {

        // Instead of traversing entity graph here, all the needed entities are
        // lifted up as parameters in order to not introduce hidden N+1 issues.

        final SortedSet<Integer> gameDiaryYears = new TreeSet<>();
        gameDiaryYears.addAll(harvestYears);
        gameDiaryYears.addAll(observationYears);

        final MobileAccountV2DTO dto = new MobileAccountV2DTO();
        dto.populateWith(username, person, address, rhy, gameDiaryYears, occupations);
        dto.getHarvestYears().addAll(harvestYears);
        dto.getObservationYears().addAll(observationYears);
        dto.setEnableSrva(person.isSrvaEnabled());
        dto.setEnableShootingTests(shootingTestsEnabled);
        dto.setQrCode(qrCode);
        dto.setShootingTests(shootingTests);
        return dto;
    }

    private final SortedSet<Integer> harvestYears = new TreeSet<>();
    private final SortedSet<Integer> observationYears = new TreeSet<>();
    private boolean enableSrva;
    private boolean enableShootingTests;
    private String qrCode;
    private List<AccountShootingTestDTO> shootingTests;

    private MobileAccountV2DTO() {
    }

    // Accessors -->

    public SortedSet<Integer> getHarvestYears() {
        return harvestYears;
    }

    public SortedSet<Integer> getObservationYears() {
        return observationYears;
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

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public List<AccountShootingTestDTO> getShootingTests() {
        return shootingTests;
    }

    public void setShootingTests(List<AccountShootingTestDTO> shootingTests) {
        this.shootingTests = shootingTests;
    }
}
