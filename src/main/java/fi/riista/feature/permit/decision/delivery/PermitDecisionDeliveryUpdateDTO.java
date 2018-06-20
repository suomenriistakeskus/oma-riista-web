package fi.riista.feature.permit.decision.delivery;

import javax.validation.Valid;
import java.util.List;

public class PermitDecisionDeliveryUpdateDTO {

    private long id;

    @Valid
    private List<PermitDecisionDeliveryDTO> deliveries;

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public List<PermitDecisionDeliveryDTO> getDeliveries() {
        return deliveries;
    }

    public void setDeliveries(final List<PermitDecisionDeliveryDTO> deliveries) {
        this.deliveries = deliveries;
    }
}
