package fi.riista.feature.gamediary.todo;

import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.harvest.HarvestDTOTransformer;
import fi.riista.feature.gamediary.harvest.Harvest;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class GameDiaryTodoHarvestDTO {
    private final List<HarvestDTO> reportRequired;
    private final List<HarvestDTO> rejectedFromPermit;
    private final List<HarvestDTO> pendingApprovalToPermit;

    private GameDiaryTodoHarvestDTO(final List<HarvestDTO> reportRequired,
                                    final List<HarvestDTO> rejectedFromPermit,
                                    final List<HarvestDTO> pendingApprovalToPermit) {
        this.reportRequired = reportRequired;
        this.rejectedFromPermit = rejectedFromPermit;
        this.pendingApprovalToPermit = pendingApprovalToPermit;
    }

    public List<HarvestDTO> getReportRequired() {
        return reportRequired;
    }

    public List<HarvestDTO> getRejectedFromPermit() {
        return rejectedFromPermit;
    }

    public List<HarvestDTO> getPendingApprovalToPermit() {
        return pendingApprovalToPermit;
    }

    public static Builder create(HarvestDTOTransformer transformer) {
        return new Builder(transformer);
    }

    public static class Builder {
        private final HarvestDTOTransformer transformer;
        private List<HarvestDTO> reportRequired;
        private List<HarvestDTO> rejectedFromPermit;
        private List<HarvestDTO> pendingApprovalToPermit;

        public Builder(final HarvestDTOTransformer transformer) {
            this.transformer = Objects.requireNonNull(transformer);
        }

        public Builder withReportRequired(final List<Harvest> reportRequired) {
            this.reportRequired = transformer.apply(reportRequired);
            return this;
        }

        public Builder withRejectedFromPermit(final List<Harvest> rejectedFromPermit) {
            this.rejectedFromPermit = transformer.apply(rejectedFromPermit);
            return this;
        }

        public Builder withPendingApprovalToPermit(final List<Harvest> pendingApprovalToPermit) {
            this.pendingApprovalToPermit = transformer.apply(pendingApprovalToPermit);
            return this;
        }

        public GameDiaryTodoHarvestDTO build() {
            if (reportRequired == null) {
                reportRequired = Collections.emptyList();
            }

            if (rejectedFromPermit == null) {
                rejectedFromPermit = Collections.emptyList();
            }

            if (pendingApprovalToPermit == null) {
                pendingApprovalToPermit = Collections.emptyList();
            }

            return new GameDiaryTodoHarvestDTO(reportRequired, rejectedFromPermit, pendingApprovalToPermit);
        }
    }
}
