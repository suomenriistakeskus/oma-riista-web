package fi.riista.integration.lupahallinta.parser;

import fi.riista.feature.common.entity.Has2BeginEndDatesDTO;

import java.math.BigDecimal;
import java.util.List;

public class PermitCSVLine {

    public static class SpeciesAmount extends Has2BeginEndDatesDTO {
        private Integer speciesOfficialCode;
        private BigDecimal amount;

        private String restrictionType;
        private BigDecimal restrictionAmount;

        private String referenceNumber;

        public Integer getSpeciesOfficialCode() {
            return speciesOfficialCode;
        }

        public void setSpeciesOfficialCode(final Integer speciesOfficialCode) {
            this.speciesOfficialCode = speciesOfficialCode;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(final BigDecimal amount) {
            this.amount = amount;
        }

        public BigDecimal getRestrictionAmount() {
            return restrictionAmount;
        }

        public void setRestrictionAmount(BigDecimal restrictionAmount) {
            this.restrictionAmount = restrictionAmount;
        }

        public String getRestrictionType() {
            return restrictionType;
        }

        public void setRestrictionType(String restrictionType) {
            this.restrictionType = restrictionType;
        }

        public String getReferenceNumber() {
            return referenceNumber;
        }

        public void setReferenceNumber(String referenceNumber) {
            this.referenceNumber = referenceNumber;
        }
    }

    private String contactPersonSsn;
    private String permitNumber;
    private String permitHolder;
    private List<String> permitPartners;
    private String permitTypeCode;
    private String permitTypeName;
    private String rhyOfficialCode;
    private List<SpeciesAmount> speciesAmounts;
    private String originalPermitNumber;
    private String printingUrl;
    private String htaNumber;
    private List<String> relatedRhys;
    private String permitAreaSize;

    public String getContactPersonSsn() {
        return contactPersonSsn;
    }

    public void setContactPersonSsn(final String contactPersonSsn) {
        this.contactPersonSsn = contactPersonSsn;
    }

    public String getPermitNumber() {
        return permitNumber;
    }

    public void setPermitNumber(final String permitNumber) {
        this.permitNumber = permitNumber;
    }

    public List<String> getPermitPartners() {
        return permitPartners;
    }

    public void setPermitPartners(final List<String> permitPartners) {
        this.permitPartners = permitPartners;
    }

    public String getPermitHolder() {
        return permitHolder;
    }

    public void setPermitHolder(final String permitHolder) {
        this.permitHolder = permitHolder;
    }

    public String getPermitTypeCode() {
        return permitTypeCode;
    }

    public void setPermitTypeCode(final String permitTypeCode) {
        this.permitTypeCode = permitTypeCode;
    }

    public String getPermitTypeName() {
        return permitTypeName;
    }

    public void setPermitTypeName(final String permitTypeName) {
        this.permitTypeName = permitTypeName;
    }

    public String getRhyOfficialCode() {
        return rhyOfficialCode;
    }

    public void setRhyOfficialCode(final String rhyOfficialCode) {
        this.rhyOfficialCode = rhyOfficialCode;
    }

    public List<SpeciesAmount> getSpeciesAmounts() {
        return speciesAmounts;
    }

    public void setSpeciesAmounts(final List<SpeciesAmount> speciesAmounts) {
        this.speciesAmounts = speciesAmounts;
    }

    public String getOriginalPermitNumber() {
        return originalPermitNumber;
    }

    public void setOriginalPermitNumber(String originalPermitNumber) {
        this.originalPermitNumber = originalPermitNumber;
    }

    public String getPrintingUrl() {
        return printingUrl;
    }

    public void setPrintingUrl(String printingUrl) {
        this.printingUrl = printingUrl;
    }

    public String getHtaNumber() {
        return htaNumber;
    }

    public void setHtaNumber(String htaNumber) {
        this.htaNumber = htaNumber;
    }

    public List<String> getRelatedRhys() {
        return relatedRhys;
    }

    public void setRelatedRhys(List<String> relatedRhys) {
        this.relatedRhys = relatedRhys;
    }

    public String getPermitAreaSize() {
        return permitAreaSize;
    }

    public void setPermitAreaSize(String permitAreaSize) {
        this.permitAreaSize = permitAreaSize;
    }
}
