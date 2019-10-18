package fi.riista.feature.permit.application.statistics;

import fi.riista.feature.organization.OrganisationNameDTO;

import java.util.Map;

public class HarvestPermitApplicationStatusTableDTO {
    private OrganisationNameDTO rka;

    /**
     * <pre>
     * {
     *   "MOOSELIKE" -> {"H" -> 1, "K" -> 2, "V" -> 3},
     *   "MOOSELIKE_NEW" -> {"H" -> ...                  },
     *   "BIRD" -> {"H" -> ...                  },
     *   ...
     * }
     * </pre>
     */
    private Map<String, Map<String, Integer>> permitCategoryToStatus;

    public OrganisationNameDTO getRka() {
        return rka;
    }

    public void setRka(final OrganisationNameDTO rka) {
        this.rka = rka;
    }

    public Map<String, Map<String, Integer>> getPermitCategoryToStatus() {
        return permitCategoryToStatus;
    }

    public void setPermitCategoryToStatus(Map<String, Map<String, Integer>> permitCategoryToStatus) {
        this.permitCategoryToStatus = permitCategoryToStatus;
    }
}
