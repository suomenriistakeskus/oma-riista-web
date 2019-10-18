package fi.riista.feature.harvest;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

public class LegalHarvestCertificateDTO {

    public static class DerogationDTO {
        private final String permitHolderName;
        private final LocalDate publishDate;
        private final String permitNumber;

        public String getPermitHolderName() {
            return permitHolderName;
        }

        public LocalDate getPublishDate() {
            return publishDate;
        }

        public String getPermitNumber() {
            return permitNumber;
        }

        public DerogationDTO(final String permitHolderName, final LocalDate publishDate, final String permitNumber) {
            this.permitHolderName = permitHolderName;
            this.publishDate = publishDate;
            this.permitNumber = permitNumber;
        }
    }

    private final LocalDate currentDate;
    private final LocalDateTime pointOfTime;
    private final String shooterName;
    private final String hunterNumber;
    private final String rhy;
    private final int longitude;
    private final int latitude;
    private final String species;
    private final String gender;
    private final double weight;
    private final LocalDate approvedDate;
    private final String approver;
    private final DerogationDTO derogation;

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public LocalDateTime getPointOfTime() {
        return pointOfTime;
    }

    public String getShooterName() {
        return shooterName;
    }

    public String getHunterNumber() {
        return hunterNumber;
    }

    public String getRhy() {
        return rhy;
    }

    public int getLongitude() {
        return longitude;
    }

    public int getLatitude() {
        return latitude;
    }

    public String getSpecies() {
        return species;
    }

    public String getGender() {
        return gender;
    }

    public double getWeight() {
        return weight;
    }

    public LocalDate getApprovedDate() {
        return approvedDate;
    }

    public String getApprover() {
        return approver;
    }

    public DerogationDTO getDerogation() {
        return derogation;
    }

    private LegalHarvestCertificateDTO(final LocalDate currentDate, final LocalDateTime pointOfTime,
                                       final String shooterName, final String hunterNumber, final String rhy,
                                       final int longitude, final int latitude, final String species,
                                       final String gender, final double weight, final LocalDate approvedDate,
                                       final String approver, final DerogationDTO derogation) {
        this.currentDate = currentDate;
        this.pointOfTime = pointOfTime;
        this.shooterName = shooterName;
        this.hunterNumber = hunterNumber;
        this.rhy = rhy;
        this.longitude = longitude;
        this.latitude = latitude;
        this.species = species;
        this.gender = gender;
        this.weight = weight;
        this.approvedDate = approvedDate;
        this.approver = approver;
        this.derogation = derogation;
    }

    public static final class Builder {
        private LocalDate currentDate;
        private LocalDateTime pointOfTime;
        private String shooterName;
        private String hunterNumber;
        private String rhy;
        private int longitude;
        private int latitude;
        private String species;
        private String gender;
        private double weight;
        private LocalDate approvedDate;
        private String approver;
        private DerogationDTO derogation;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder withCurrentDate(LocalDate currentDate) {
            this.currentDate = currentDate;
            return this;
        }

        public Builder withPointOfTime(LocalDateTime pointOfTime) {
            this.pointOfTime = pointOfTime;
            return this;
        }

        public Builder withShooterName(String shooterName) {
            this.shooterName = shooterName;
            return this;
        }

        public Builder withHunterNumber(String hunterNumber) {
            this.hunterNumber = hunterNumber;
            return this;
        }

        public Builder withRhy(String rhy) {
            this.rhy = rhy;
            return this;
        }

        public Builder withLongitude(int longitude) {
            this.longitude = longitude;
            return this;
        }

        public Builder withLatitude(int latitude) {
            this.latitude = latitude;
            return this;
        }

        public Builder withSpecies(String species) {
            this.species = species;
            return this;
        }

        public Builder withGender(String gender) {
            this.gender = gender;
            return this;
        }

        public Builder withWeight(double weight) {
            this.weight = weight;
            return this;
        }

        public Builder withApprovedDate(LocalDate approvedDate) {
            this.approvedDate = approvedDate;
            return this;
        }

        public Builder withApprover(String approver) {
            this.approver = approver;
            return this;
        }

        public Builder withDerogation(DerogationDTO derogation) {
            this.derogation = derogation;
            return this;
        }

        public LegalHarvestCertificateDTO build() {
            return new LegalHarvestCertificateDTO(currentDate, pointOfTime, shooterName, hunterNumber, rhy, longitude
                    , latitude, species, gender, weight, approvedDate, approver, derogation);
        }
    }
}
