package fi.riista.integration.lupahallinta.club;

import com.google.common.base.MoreObjects;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gis.hta.GISHirvitalousalue;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.F;

import java.util.Optional;

public class LHHuntingClubCSVRow {
    private static final GeoLocation EMPTY_GEOLOCATION = new GeoLocation();
    private static final Address EMPTY_ADDRESS = new Address();

    public static LHHuntingClubCSVRow create(final HuntingClub club, final String clubRhy, final String clubRka,
                                             final Person contactPerson, final String personRhy, final String personRka) {
        final GeoLocation geoLocation = F.firstNonNull(club.getGeoLocation(), EMPTY_GEOLOCATION);
        final Address address = F.firstNonNull(contactPerson.getAddress(), EMPTY_ADDRESS);

        LHHuntingClubCSVRow row = new LHHuntingClubCSVRow();
        row.setNimiSuomi(club.getNameFinnish());
        row.setNimiRuotsi(club.getNameSwedish());
        row.setValittuAlue(clubRka);
        row.setAsiakasNumero(club.getOfficialCode());
        row.setRhy(clubRhy);
        row.setHirvitalousAlue(Optional.ofNullable(club.getMooseArea()).map(GISHirvitalousalue::getNumber).orElse(null));
        row.setpKoordinaatti(geoLocation.getLatitude());
        row.setiKoordinaatti(geoLocation.getLongitude());
        row.setPintaAla(club.getHuntingAreaSize() != null ? club.getHuntingAreaSize().intValue() : 0);
        row.setYhteysHenkilo(contactPerson.getFullName());
        row.setValittuAlue2(personRka);
        row.setHetu(contactPerson.getSsn());
        row.setOsoite(address.getStreetAddress());
        row.setOsoite2("");
        row.setPostinumeroJaPostitoimipaikka(address.getPostalCode() + " " + address.getCity());
        row.setRhy2(personRhy);
        row.setTitteli("");
        row.setPuhelin1(contactPerson.getPhoneNumber());
        row.setPuhelin2("");
        row.setSahkoposti(contactPerson.getEmail());
        row.setKieli(getLanguageCode(contactPerson));
        return row;
    }

    private static String getLanguageCode(Person contactPerson) {
        return "sv".equals(contactPerson.getLanguageCode()) ? "SWE": "FIN";
    }

    // Organisaation nimi (FIN)
    private String nimiSuomi;

    // Organisaation nimi (SWE)
    private String nimiRuotsi;

    // Valittu alue
    private String valittuAlue;

    // Asiakasnumero
    private String asiakasNumero;

    // RHY
    private String rhy;

    // Hirvitalousalue
    private String hirvitalousAlue;

    // Organisaation P-koordinaatti
    private Integer pKoordinaatti;

    // Organisaation I-koordinaatti
    private Integer iKoordinaatti;

    // Pinta-ala
    private Integer pintaAla;

    // Yhteyshenkilö
    private String yhteysHenkilo;

    // Valittu alue
    private String valittuAlue2;

    // Henkilötunnus
    private String hetu;

    // Osoite
    private String osoite;

    // Osoite 2
    private String osoite2;

    // Postinumero ja postitoimipaikka
    private String postinumeroJaPostitoimipaikka;

    // RHY
    private String rhy2;

    // Titteli
    private String titteli;

    // Puhelin 1
    private String puhelin1;

    // Puhelin 2
    private String puhelin2;

    // Sähköpostiosoite
    private String sahkoposti;

    // Kieli
    private String kieli;

    public String getNimiSuomi() {
        return nimiSuomi;
    }

    public void setNimiSuomi(final String nimiSuomi) {
        this.nimiSuomi = nimiSuomi;
    }

    public String getNimiRuotsi() {
        return nimiRuotsi;
    }

    public void setNimiRuotsi(final String nimiRuotsi) {
        this.nimiRuotsi = nimiRuotsi;
    }

    public String getValittuAlue() {
        return valittuAlue;
    }

    public void setValittuAlue(final String valittuAlue) {
        this.valittuAlue = valittuAlue;
    }

    public String getAsiakasNumero() {
        return asiakasNumero;
    }

    public void setAsiakasNumero(final String asiakasNumero) {
        this.asiakasNumero = asiakasNumero;
    }

    public String getRhy() {
        return rhy;
    }

    public void setRhy(final String rhy) {
        this.rhy = rhy;
    }

    public String getHirvitalousAlue() {
        return hirvitalousAlue;
    }

    public void setHirvitalousAlue(final String hirvitalousAlue) {
        this.hirvitalousAlue = hirvitalousAlue;
    }

    public Integer getpKoordinaatti() {
        return pKoordinaatti;
    }

    public void setpKoordinaatti(final Integer pKoordinaatti) {
        this.pKoordinaatti = pKoordinaatti;
    }

    public Integer getiKoordinaatti() {
        return iKoordinaatti;
    }

    public void setiKoordinaatti(final Integer iKoordinaatti) {
        this.iKoordinaatti = iKoordinaatti;
    }

    public Integer getPintaAla() {
        return pintaAla;
    }

    public void setPintaAla(final Integer pintaAla) {
        this.pintaAla = pintaAla;
    }

    public String getYhteysHenkilo() {
        return yhteysHenkilo;
    }

    public void setYhteysHenkilo(final String yhteysHenkilo) {
        this.yhteysHenkilo = yhteysHenkilo;
    }

    public String getValittuAlue2() {
        return valittuAlue2;
    }

    public void setValittuAlue2(final String valittuAlue2) {
        this.valittuAlue2 = valittuAlue2;
    }

    public String getHetu() {
        return hetu;
    }

    public void setHetu(final String hetu) {
        this.hetu = hetu;
    }

    public String getOsoite() {
        return osoite;
    }

    public void setOsoite(final String osoite) {
        this.osoite = osoite;
    }

    public String getOsoite2() {
        return osoite2;
    }

    public void setOsoite2(final String osoite2) {
        this.osoite2 = osoite2;
    }

    public String getPostinumeroJaPostitoimipaikka() {
        return postinumeroJaPostitoimipaikka;
    }

    public void setPostinumeroJaPostitoimipaikka(final String postinumeroJaPostitoimipaikka) {
        this.postinumeroJaPostitoimipaikka = postinumeroJaPostitoimipaikka;
    }

    public String getRhy2() {
        return rhy2;
    }

    public void setRhy2(final String rhy2) {
        this.rhy2 = rhy2;
    }

    public String getTitteli() {
        return titteli;
    }

    public void setTitteli(final String titteli) {
        this.titteli = titteli;
    }

    public String getPuhelin1() {
        return puhelin1;
    }

    public void setPuhelin1(final String puhelin1) {
        this.puhelin1 = puhelin1;
    }

    public String getPuhelin2() {
        return puhelin2;
    }

    public void setPuhelin2(final String puhelin2) {
        this.puhelin2 = puhelin2;
    }

    public String getSahkoposti() {
        return sahkoposti;
    }

    public void setSahkoposti(final String sahkoposti) {
        this.sahkoposti = sahkoposti;
    }

    public String getKieli() {
        return kieli;
    }

    public void setKieli(final String kieli) {
        this.kieli = kieli;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("nimiSuomi", nimiSuomi)
                .add("nimiRuotsi", nimiRuotsi)
                .add("valittuAlue", valittuAlue)
                .add("asiakasNumero", asiakasNumero)
                .add("rhy", rhy)
                .add("hirvitalousAlue", hirvitalousAlue)
                .add("pKoordinaatti", pKoordinaatti)
                .add("iKoordinaatti", iKoordinaatti)
                .add("pintaAla", pintaAla)
                .add("yhteysHenkilo", yhteysHenkilo)
                .add("valittuAlue2", valittuAlue2)
                .add("hetu", hetu)
                .add("osoite", osoite)
                .add("osoite2", osoite2)
                .add("postinumeroJaPostitoimipaikka", postinumeroJaPostitoimipaikka)
                .add("rhy2", rhy2)
                .add("titteli", titteli)
                .add("puhelin1", puhelin1)
                .add("puhelin2", puhelin2)
                .add("sahkoposti", sahkoposti)
                .add("kieli", kieli)
                .toString();
    }
}
