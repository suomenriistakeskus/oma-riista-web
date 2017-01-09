package fi.riista.feature.vetuma.dto;

import fi.riista.feature.vetuma.VetumaConfig;
import fi.riista.api.external.VetumaResponseController;
import org.joda.time.DateTime;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

public class VetumaLoginRequestDTO extends VetumaLoginBaseDTO {

    public static VetumaLoginRequestDTO create(final VetumaConfig vetumaConfig,
                                               final String transactionId,
                                               final String languageCode,
                                               final DateTime timestamp) {

        final VetumaLoginRequestDTO dto = new VetumaLoginRequestDTO();

        // Pakollinen: Kutsun suojauksessa käytetyn jaetun salaisuuden tunnus
        dto.setRCVID(vetumaConfig.getSharedSecretIdentifier());

        // Pakollinen: VETUMA-palvelua kutsuvan asiointisovelluksen tunnus
        dto.setAPPID(vetumaConfig.getClientApplicationId());

        // Pakollinen: Kutsun aikaleima
        dto.setTimestamp(timestamp);

        // Pakollinen: Oletusmenetelmä tunnistautumiseen 6=TUPAS
        dto.setSO(vetumaConfig.getDefaultAuthenticationMethod());

        // Pakollinen: Käyttäjälle tarjottavat menetelmät tunnistautumiseen
        dto.setSOLIST(vetumaConfig.getAllowedAuthenticationMethods());

        // Pakollinen: Käytettävän VETUMA-palvelun tyypin tunnus (vakio-arvo tunnistautumiskutsuissa)
        dto.setTYPE("LOGIN");

        // Pakollinen: Pyydettävän toiminnon koodi (vakio-arvo tunnistautumiskutsuissa)
        dto.setAU("EXTAUTH");

        // Pakollinen: Käyttöliittymäkieli
        dto.setLG(StringUtils.hasText(languageCode) ? languageCode : "fi");

        // Pakollinen: Kutsun palvelemisessa käytettävän konfiguraation tunnus
        dto.setAP(vetumaConfig.getClientApplicationConfiguration());

        // Valinnainen: Kutsuvan sovelluksen nimi käyttöliittymää varten
        dto.setAPPNAME(vetumaConfig.getClientApplicationName());

        // Valinnainen: Tapahtumatunnus (0-20 merkkiä)
        dto.setTRID(transactionId);

        // Valinnainen: VTJ-kyselypyyntö (tuotteen tunniste)
        dto.setEXTRADATA(vetumaConfig.getVetumaExtraData());

        // Pakollinen: Paluuosoite sovellukseen onnistuneen tapahtuman jälkeen
        dto.setRETURL(vetumaConfig.getVetumaReturnUrl(VetumaResponseController.CALLBACK_VETUMA_SUCCESS));

        // Pakollinen: Paluuosoite sovellukseen käyttäjän peruman tapahtuman jälkeen
        dto.setCANURL(vetumaConfig.getVetumaReturnUrl(VetumaResponseController.CALLBACK_VETUMA_CANCEL));

        // Pakollinen: Virhepaluuosoite sovellukseen
        dto.setERRURL(vetumaConfig.getVetumaReturnUrl(VetumaResponseController.CALLBACK_VETUMA_ERROR));

        // Pakollinen: Kutsun turvatarkiste (message authentication code = MAC)
        dto.setMAC(dto.computeMac(vetumaConfig.getShareSecretKey()));

        return dto;
    }

    // Order number: 2
    private String APPID;

    // Order number: 5
    private String SOLIST;

    // Order number: 6
    private String TYPE;

    // Order number: 7
    private String AU;

    // Order number: 13
    private String AP;

    // Order number: 20
    private String APPNAME;

    @Override
    protected List<String> getDigestInputFields() {
        // Order is important!
        return Arrays.asList(
                RCVID, APPID, TIMESTMP, SO, SOLIST, TYPE, AU, LG, RETURL, CANURL, ERRURL, AP,
                EXTRADATA, APPNAME, TRID);
    }

    public String getAPPID() {
        return APPID;
    }

    public void setAPPID(String appId) {
        this.APPID = appId;
    }

    public String getSOLIST() {
        return SOLIST;
    }

    public void setSOLIST(String soList) {
        this.SOLIST = soList;
    }

    public String getTYPE() {
        return TYPE;
    }

    public void setTYPE(String type) {
        this.TYPE = type;
    }

    public String getAU() {
        return AU;
    }

    public void setAU(String au) {
        this.AU = au;
    }

    public String getAP() {
        return AP;
    }

    public void setAP(String ap) {
        this.AP = ap;
    }

    public String getAPPNAME() {
        return APPNAME;
    }

    public void setAPPNAME(String appName) {
        this.APPNAME = appName;
    }

}
