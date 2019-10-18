package fi.riista.feature.permitplanning.hirvityvitys.dto;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class JyvitysExcelApplicationDTO {

    public static final class Builder {
        private String applicant;
        private double appliedAmount;
        private int shooterOnlyClub;
        private int shooterOtherClubPassive;
        private List<JyvitysExcelApplicationVerotuslohkoDTO> lohkoList;
        private List<String> otherRhysInArea;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder withApplicant(final String applicant) {
            this.applicant = applicant;
            return this;
        }

        public Builder withAppliedAmount(final double appliedAmount) {
            this.appliedAmount = appliedAmount;
            return this;
        }

        public Builder withShooterOnlyClub(final int shooterOnlyClub) {
            this.shooterOnlyClub = shooterOnlyClub;
            return this;
        }

        public Builder withShooterOtherClubPassive(final int shooterOtherClubPassive) {
            this.shooterOtherClubPassive = shooterOtherClubPassive;
            return this;
        }

        public Builder withLohkoList(final List<JyvitysExcelApplicationVerotuslohkoDTO> lohkoList) {
            this.lohkoList = lohkoList;
            return this;
        }

        public Builder withOtherRhysInArea(final List<String> otherRhysInArea) {
            this.otherRhysInArea = otherRhysInArea;
            return this;
        }

        public JyvitysExcelApplicationDTO build() {
            return new JyvitysExcelApplicationDTO(applicant, appliedAmount, shooterOnlyClub, shooterOtherClubPassive, lohkoList, otherRhysInArea);
        }
    }

    private final String applicant;
    private final double appliedAmount;
    private final int shooterOnlyClub;

    private final int shooterOtherClubPassive;

    private final List<JyvitysExcelApplicationVerotuslohkoDTO> lohkoList;
    private final List<String> otherRhysInArea;

    private JyvitysExcelApplicationDTO(final @Nonnull String applicant,
                                       final double appliedAmount,
                                       final int shooterOnlyClub,
                                       final int shooterOtherClubPassive,
                                       final @Nonnull List<JyvitysExcelApplicationVerotuslohkoDTO> lohkoList,
                                       final @Nonnull List<String> otherRhysInArea) {
        this.applicant = requireNonNull(applicant);
        this.appliedAmount = appliedAmount;
        this.shooterOnlyClub = shooterOnlyClub;
        this.shooterOtherClubPassive = shooterOtherClubPassive;
        this.lohkoList = ImmutableList.copyOf(lohkoList);
        this.otherRhysInArea = ImmutableList.copyOf(otherRhysInArea);
    }

    public String getApplicant() {
        return applicant;
    }

    public double getAppliedAmount() {
        return appliedAmount;
    }

    public int getShooterOnlyClub() {
        return shooterOnlyClub;
    }

    public int getShooterOtherClubPassive() {
        return shooterOtherClubPassive;
    }

    public List<JyvitysExcelApplicationVerotuslohkoDTO> getLohkoList() {
        return lohkoList;
    }

    public List<String> getOtherRhysInArea() {
        return otherRhysInArea;
    }

    public JyvitysExcelApplicationVerotuslohkoDTO getApplicationVerotuslohkoInfo(final String officialCode) {
        return getLohkoList()
                .stream()
                .filter(dto -> dto.getOfficialCode().equals(officialCode))
                .findAny()
                .orElse(JyvitysExcelApplicationVerotuslohkoDTO.EMPTY);
    }
}
