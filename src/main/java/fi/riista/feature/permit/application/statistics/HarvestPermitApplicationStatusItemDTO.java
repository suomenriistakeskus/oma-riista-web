package fi.riista.feature.permit.application.statistics;

import java.util.Map;

public class HarvestPermitApplicationStatusItemDTO {
    private String category;
    private Map<String, Integer> statuses;

    public static HarvestPermitApplicationStatusItemDTO create(final String category,
                                                               final Map<String, Integer> statuses) {
        final HarvestPermitApplicationStatusItemDTO dto = new HarvestPermitApplicationStatusItemDTO();
        dto.setCategory(category);
        dto.setStatuses(statuses);
        return dto;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(final String category) {
        this.category = category;
    }

    public Map<String, Integer> getStatuses() {
        return statuses;
    }

    public void setStatuses(final Map<String, Integer> statuses) {
        this.statuses = statuses;
    }
}
