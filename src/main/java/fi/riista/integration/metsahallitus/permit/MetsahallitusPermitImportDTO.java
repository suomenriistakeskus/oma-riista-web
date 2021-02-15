package fi.riista.integration.metsahallitus.permit;

import com.google.common.collect.ImmutableList;
import org.hibernate.validator.constraints.SafeHtml;

import java.util.List;
import java.util.StringJoiner;

public class MetsahallitusPermitImportDTO {

    public static final String PAID_1 = "160";
    public static final String PAID_2 = "162";
    public static final List<String> PAID_CODES = ImmutableList.of(PAID_1, PAID_2);

    // Tieto onko lupa maksettu vai peruttu. Muita rivejä ei tulisi siirtää.
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String tilauksenTila;

    // Luvanhaltijan metsästäjänumero
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String metsastajaNumero;

    // Rajapinnassa lähetetään vain henkilötunnuksen alkuosa
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String henkiloTunnus;

    // M-Files tuotteiden pää-/ ja aliryhmät ovat:
    // * ME / PI  = Pienriistaluvat
    // * KA / MMM = Kalastonhoitoluvat
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String lupaTyyppi;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String lupaTyyppiSE;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String lupaTyyppiEN;

    // Luvansaajakohtainen lupanumero
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String luvanTunnus;

    // Vuorokausi vai kausi-lupa?
    // Optional
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String luvanNimi;

    // Optional
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String luvanNimiSE;

    // Optional
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String luvanNimiEN;

    // Metsähallituksen alueen koodi
    // Optional
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String alueNro;

    // Metsähallituksen alueen nimi
    // Optional
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String alueenNimi;

    // Optional
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String alueenNimiSE;

    // Optional
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String alueenNimiEN;

    // Vuosituotteen tai kalenterituotteen alku- ja loppupäivä
    // Luvan aloituspäivän vuosiluku tulee olla edellinen vuosi tai suurempi.
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String alkuPvm;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String loppuPvm;

    // Optional
    // Luvansaajakohtainen linkki saalispalautelomakkeeseen. Toistaiseksi lähetetään tyhjänä.
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String url;

    // Optional
    // Tieto onko saalispalaute lähetetty.
    private Boolean saalispalauteAnnettu;

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

    public Boolean getSaalispalauteAnnettu() {
        return saalispalauteAnnettu;
    }

    public void setSaalispalauteAnnettu(final Boolean saalispalauteAnnettu) {
        this.saalispalauteAnnettu = saalispalauteAnnettu;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", MetsahallitusPermitImportDTO.class.getSimpleName() + "[", "]")
                .add("tilauksenTila='" + tilauksenTila + "'")
                .add("metsastajaNumero='" + metsastajaNumero + "'")
                .add("henkiloTunnus='" + henkiloTunnus + "'")
                .add("lupaTyyppi='" + lupaTyyppi + "'")
                .add("lupaTyyppiSE='" + lupaTyyppiSE + "'")
                .add("lupaTyyppiEN='" + lupaTyyppiEN + "'")
                .add("luvanTunnus='" + luvanTunnus + "'")
                .add("luvanNimi='" + luvanNimi + "'")
                .add("luvanNimiSE='" + luvanNimiSE + "'")
                .add("luvanNimiEN='" + luvanNimiEN + "'")
                .add("alueNro='" + alueNro + "'")
                .add("alueenNimi='" + alueenNimi + "'")
                .add("alueenNimiSE='" + alueenNimiSE + "'")
                .add("alueenNimiEN='" + alueenNimiEN + "'")
                .add("alkuPvm='" + alkuPvm + "'")
                .add("loppuPvm='" + loppuPvm + "'")
                .add("url='" + url + "'")
                .add("saalispalauteAnnettu='" + saalispalauteAnnettu + "'")
                .toString();
    }
}
