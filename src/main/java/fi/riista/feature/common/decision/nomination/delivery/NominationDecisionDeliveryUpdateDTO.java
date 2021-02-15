package fi.riista.feature.common.decision.nomination.delivery;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

public class NominationDecisionDeliveryUpdateDTO {

    private long id;

    @NotNull
    @Valid
    private List<NominationDecisionDeliveryDTO> deliveries;

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public List<NominationDecisionDeliveryDTO> getDeliveries() {
        return deliveries;
    }

    public void setDeliveries(final List<NominationDecisionDeliveryDTO> deliveries) {
        this.deliveries = deliveries;
    }
}
