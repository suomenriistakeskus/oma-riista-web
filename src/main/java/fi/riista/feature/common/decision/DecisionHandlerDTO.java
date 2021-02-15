package fi.riista.feature.common.decision;

import org.hibernate.validator.constraints.SafeHtml;

public class DecisionHandlerDTO {

    private long handlerId;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String name;

    public DecisionHandlerDTO() {
    }

    public DecisionHandlerDTO(final long handlerId, final String name) {
        this.handlerId = handlerId;
        this.name = name;
    }

    public long getHandlerId() {
        return handlerId;
    }

    public void setHandlerId(final long handlerId) {
        this.handlerId = handlerId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
