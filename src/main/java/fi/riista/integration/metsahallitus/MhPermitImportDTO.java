package fi.riista.integration.metsahallitus;

import org.hibernate.validator.constraints.SafeHtml;

public class MhPermitImportDTO {

    public static final String PAID = "160";

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String metsastajaNumero;
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String henkiloTunnus;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String lupaTyyppi;
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String lupaTyyppiSE;
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String lupaTyyppiEN;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String luvanTunnus;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String luvanNimi;
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String luvanNimiSE;
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String luvanNimiEN;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String alueNro;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String alueenNimi;
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String alueenNimiSE;
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String alueenNimiEN;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String alkuPvm;
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String loppuPvm;
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String url;
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String tilauksenTila;

    public String getMetsastajaNumero() {
        return metsastajaNumero;
    }

    public void setMetsastajaNumero(String metsastajaNumero) {
        this.metsastajaNumero = metsastajaNumero;
    }

    public String getHenkiloTunnus() {
        return henkiloTunnus;
    }

    public void setHenkiloTunnus(String henkiloTunnus) {
        this.henkiloTunnus = henkiloTunnus;
    }

    public String getLupaTyyppi() {
        return lupaTyyppi;
    }

    public void setLupaTyyppi(String lupaTyyppi) {
        this.lupaTyyppi = lupaTyyppi;
    }

    public String getLupaTyyppiSE() {
        return lupaTyyppiSE;
    }

    public void setLupaTyyppiSE(String lupaTyyppiSE) {
        this.lupaTyyppiSE = lupaTyyppiSE;
    }

    public String getLupaTyyppiEN() {
        return lupaTyyppiEN;
    }

    public void setLupaTyyppiEN(String lupaTyyppiEN) {
        this.lupaTyyppiEN = lupaTyyppiEN;
    }

    public String getLuvanTunnus() {
        return luvanTunnus;
    }

    public void setLuvanTunnus(String luvanTunnus) {
        this.luvanTunnus = luvanTunnus;
    }

    public String getLuvanNimi() {
        return luvanNimi;
    }

    public void setLuvanNimi(String luvanNimi) {
        this.luvanNimi = luvanNimi;
    }

    public String getLuvanNimiSE() {
        return luvanNimiSE;
    }

    public void setLuvanNimiSE(String luvanNimiSE) {
        this.luvanNimiSE = luvanNimiSE;
    }

    public String getLuvanNimiEN() {
        return luvanNimiEN;
    }

    public void setLuvanNimiEN(String luvanNimiEN) {
        this.luvanNimiEN = luvanNimiEN;
    }

    public String getAlueNro() {
        return alueNro;
    }

    public void setAlueNro(String alueNro) {
        this.alueNro = alueNro;
    }

    public String getAlueenNimi() {
        return alueenNimi;
    }

    public void setAlueenNimi(String alueenNimi) {
        this.alueenNimi = alueenNimi;
    }

    public String getAlueenNimiSE() {
        return alueenNimiSE;
    }

    public void setAlueenNimiSE(String alueenNimiSE) {
        this.alueenNimiSE = alueenNimiSE;
    }

    public String getAlueenNimiEN() {
        return alueenNimiEN;
    }

    public void setAlueenNimiEN(String alueenNimiEN) {
        this.alueenNimiEN = alueenNimiEN;
    }

    public String getAlkuPvm() {
        return alkuPvm;
    }

    public void setAlkuPvm(String alkuPvm) {
        this.alkuPvm = alkuPvm;
    }

    public String getLoppuPvm() {
        return loppuPvm;
    }

    public void setLoppuPvm(String loppuPvm) {
        this.loppuPvm = loppuPvm;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTilauksenTila() {
        return tilauksenTila;
    }

    public void setTilauksenTila(String tilauksenTila) {
        this.tilauksenTila = tilauksenTila;
    }

    @Override
    public String toString() {
        return "MhPermitImportDTO{" +
                "metsastajaNumero='" + metsastajaNumero + '\'' +
                ", henkiloTunnus='" + henkiloTunnus + '\'' +
                ", lupaTyyppi='" + lupaTyyppi + '\'' +
                ", lupatyyppiSE='" + lupaTyyppiSE + '\'' +
                ", lupatyyppiEN='" + lupaTyyppiEN + '\'' +
                ", luvanTunnus='" + luvanTunnus + '\'' +
                ", luvanNimi='" + luvanNimi + '\'' +
                ", luvanNimiSE='" + luvanNimiSE + '\'' +
                ", luvanNimiEN='" + luvanNimiEN + '\'' +
                ", alueNro='" + alueNro + '\'' +
                ", alueenNimi='" + alueenNimi + '\'' +
                ", alueenNimiSE='" + alueenNimiSE + '\'' +
                ", alueenNimiEN='" + alueenNimiEN + '\'' +
                ", alkuPvm='" + alkuPvm + '\'' +
                ", loppuPvm='" + loppuPvm + '\'' +
                ", url='" + url + '\'' +
                ", tilauksenTila='" + tilauksenTila + '\'' +
                '}';
    }
}
