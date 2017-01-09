package fi.riista.feature.gis.rhy;

import java.math.BigDecimal;

public class GISRiistanhoitoyhdistys {
    private Integer gid;
    private String officialCode;
    private String nimiFi;
    private String nimiSv;
    private BigDecimal vesiAla;
    private BigDecimal maaAla;
    private BigDecimal kokoAla;

    public Integer getGid() {
        return gid;
    }

    public void setGid(Integer gid) {
        this.gid = gid;
    }

    public String getOfficialCode() {
        return officialCode;
    }

    public void setOfficialCode(String officialCode) {
        this.officialCode = officialCode;
    }

    public String getNimiFi() {
        return nimiFi;
    }

    public void setNimiFi(String nimiFi) {
        this.nimiFi = nimiFi;
    }

    public String getNimiSv() {
        return nimiSv;
    }

    public void setNimiSv(String nimiSv) {
        this.nimiSv = nimiSv;
    }

    public BigDecimal getVesiAla() {
        return vesiAla;
    }

    public void setVesiAla(BigDecimal vesiAla) {
        this.vesiAla = vesiAla;
    }

    public BigDecimal getMaaAla() {
        return maaAla;
    }

    public void setMaaAla(BigDecimal maaAla) {
        this.maaAla = maaAla;
    }

    public BigDecimal getKokoAla() {
        return kokoAla;
    }

    public void setKokoAla(BigDecimal kokoAla) {
        this.kokoAla = kokoAla;
    }
}
