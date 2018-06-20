package fi.riista.feature.organization.rhy.annualstats;

import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class RhyAnnualStatisticsProgressDTO {

    public static RhyAnnualStatisticsProgressDTO create(@Nonnull final Riistanhoitoyhdistys rhy,
                                                        @Nullable final RhyAnnualStatistics annualStats,
                                                        @Nullable final Person coordinator) {

        requireNonNull(rhy, "rhy is null");

        final RhyAnnualStatisticsProgressDTO dto = new RhyAnnualStatisticsProgressDTO();

        dto.setRhyId(rhy.getId());
        dto.setRhyCode(rhy.getOfficialCode());
        dto.setRhyName(rhy.getNameLocalisation().asMap());

        final String rhyEmail = Optional.ofNullable(rhy.getEmail())
                .orElseGet(() -> Optional.ofNullable(coordinator).map(Person::getEmail).orElse(null));
        dto.setRhyEmail(rhyEmail);

        if (annualStats != null) {
            dto.setAnnualStatsId(annualStats.getId());
            dto.setAnnualStatsState(RhyAnnualStatisticsProgress.from(annualStats.getState()));
            dto.setReadyForInspection(annualStats.isReadyForInspection());
            dto.setCompleteForApproval(annualStats.isCompleteForApproval());
        } else {
            dto.setAnnualStatsState(RhyAnnualStatisticsProgress.NOT_CREATED);
        }

        return dto;
    }

    public enum RhyAnnualStatisticsProgress {

        NOT_CREATED,
        IN_PROGRESS,
        UNDER_INSPECTION,
        APPROVED;

        public static RhyAnnualStatisticsProgress from(@Nullable final RhyAnnualStatisticsState state) {
            switch (state) {
                case IN_PROGRESS:
                    return IN_PROGRESS;
                case UNDER_INSPECTION:
                    return UNDER_INSPECTION;
                case APPROVED:
                    return APPROVED;
                default:
                    // When null
                    return NOT_CREATED;
            }
        }
    }

    private Long rhyId;
    private String rhyCode;
    private Map<String, String> rhyName;
    private String rhyEmail;

    private Long annualStatsId;
    private RhyAnnualStatisticsProgress annualStatsState;
    private boolean readyForInspection;
    private boolean completeForApproval;

    // Accessors -->

    public Long getRhyId() {
        return rhyId;
    }

    public void setRhyId(final Long rhyId) {
        this.rhyId = rhyId;
    }

    public String getRhyCode() {
        return rhyCode;
    }

    public void setRhyCode(final String rhyCode) {
        this.rhyCode = rhyCode;
    }

    public Map<String, String> getRhyName() {
        return rhyName;
    }

    public void setRhyName(final Map<String, String> rhyName) {
        this.rhyName = rhyName;
    }

    public String getRhyEmail() {
        return rhyEmail;
    }

    public void setRhyEmail(final String rhyEmail) {
        this.rhyEmail = rhyEmail;
    }

    public Long getAnnualStatsId() {
        return annualStatsId;
    }

    public void setAnnualStatsId(final Long annualStatsId) {
        this.annualStatsId = annualStatsId;
    }

    public RhyAnnualStatisticsProgress getAnnualStatsState() {
        return annualStatsState;
    }

    public void setAnnualStatsState(final RhyAnnualStatisticsProgress annualStatsState) {
        this.annualStatsState = annualStatsState;
    }

    public boolean isReadyForInspection() {
        return readyForInspection;
    }

    public void setReadyForInspection(final boolean readyForInspection) {
        this.readyForInspection = readyForInspection;
    }

    public boolean isCompleteForApproval() {
        return completeForApproval;
    }

    public void setCompleteForApproval(final boolean completeForApproval) {
        this.completeForApproval = completeForApproval;
    }
}
