package fi.riista.feature.pub.season;

import fi.riista.feature.harvestpermit.season.HarvestQuota;

public class PublicHarvestQuotaDTO {

    private String nameFinnish;
    private String nameSwedish;
    private Integer quota;
    private Integer usedQuota;
    private boolean huntingSuspended;

    public static PublicHarvestQuotaDTO create(HarvestQuota quota, Integer usedQuota) {
        PublicHarvestQuotaDTO dto = new PublicHarvestQuotaDTO();
        dto.setNameFinnish(quota.getHarvestArea().getNameFinnish());
        dto.setNameSwedish(quota.getHarvestArea().getNameSwedish());

        dto.setQuota(quota.getQuota());
        dto.setUsedQuota(usedQuota);
        dto.setHuntingSuspended(quota.getHuntingSuspended());

        return dto;
    }

    public void setNameFinnish(String nameFinnish) {
        this.nameFinnish = nameFinnish;
    }

    public String getNameFinnish() {
        return nameFinnish;
    }

    public void setNameSwedish(String nameSwedish) {
        this.nameSwedish = nameSwedish;
    }

    public String getNameSwedish() {
        return nameSwedish;
    }

    public void setQuota(Integer quota) {
        this.quota = quota;
    }

    public Integer getQuota() {
        return quota;
    }

    public void setUsedQuota(Integer usedQuota) {
        this.usedQuota = usedQuota;
    }

    public Integer getUsedQuota() {
        return usedQuota;
    }

    public boolean isHuntingSuspended() {
        return huntingSuspended;
    }

    public void setHuntingSuspended(boolean huntingSuspended) {
        this.huntingSuspended = huntingSuspended;
    }
}
