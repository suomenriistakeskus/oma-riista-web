package fi.riista.feature.organization.rhy.annualstats.statechange;

import fi.riista.feature.common.dto.BaseEntityEventDTO;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatistics;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class RhyAnnualStatisticsProgressDTO {

    public static RhyAnnualStatisticsProgressDTO create(@Nonnull final Riistanhoitoyhdistys rhy,
                                                        @Nullable final RhyAnnualStatistics annualStats,
                                                        @Nullable final Person coordinator,
                                                        @Nullable final BaseEntityEventDTO baseEntityEventDTO) {

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
            dto.setAnnualStatsState(annualStats.getState());
            dto.setReadyForInspection(annualStats.isReadyForInspection());
            dto.setCompleteForApproval(annualStats.isCompleteForApproval());
        } else {
            dto.setAnnualStatsState(RhyAnnualStatisticsState.NOT_STARTED);
        }

        if (baseEntityEventDTO != null) {
            dto.setSubmitEvent(baseEntityEventDTO);
        }

        return dto;
    }

    private Long rhyId;
    private String rhyCode;
    private Map<String, String> rhyName;
    private String rhyEmail;

    private Long annualStatsId;
    private RhyAnnualStatisticsState annualStatsState;
    private boolean readyForInspection;
    private boolean completeForApproval;

    private BaseEntityEventDTO submitEvent;

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

    public RhyAnnualStatisticsState getAnnualStatsState() {
        return annualStatsState;
    }

    public void setAnnualStatsState(final RhyAnnualStatisticsState annualStatsState) {
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

    public BaseEntityEventDTO getSubmitEvent() {
        return submitEvent;
    }

    public void setSubmitEvent(final BaseEntityEventDTO submitEvent) {
        this.submitEvent = submitEvent;
    }
}
