package fi.riista.feature.permit.application.search;

import org.hibernate.validator.constraints.SafeHtml;

public class HarvestPermitApplicationHandlerDTO {

    private long handlerId;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String name;

    public HarvestPermitApplicationHandlerDTO() {
    }

    public HarvestPermitApplicationHandlerDTO(final Long handlerId, final String name) {
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
