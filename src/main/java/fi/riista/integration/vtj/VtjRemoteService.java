package fi.riista.integration.vtj;

import com.google.common.base.Preconditions;

import fi.vrk.xml.schema.vtjkysely.VTJHenkiloVastaussanoma;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tempuri.SoSoSoap;
import org.tempuri.TeeHenkilonTunnusKyselyResponse.TeeHenkilonTunnusKyselyResult;
import org.w3c.dom.Element;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import java.util.Objects;

@Component
public class VtjRemoteService {

    private static final Logger LOG = LoggerFactory.getLogger(VtjRemoteService.class);

    private static final String OK = "0000";
    private static final String NOT_FOUND = "0001";
    private static final String PASSIVE = "0002";

    private SoSoSoap soso;
    private VtjConfig vtjConfig;

    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    public VtjRemoteService(SoSoSoap soso, VtjConfig vtjConfig) {
        this.soso = soso;
        this.vtjConfig = vtjConfig;
    }

    public VTJHenkiloVastaussanoma.Henkilo search(String loppukayttaja, String ssn) throws Exception {
        if (StringUtils.isBlank(vtjConfig.getUsername())) {
            LOG.info("vtj username is empty, do nothing");

            return null;
        }

        final TeeHenkilonTunnusKyselyResult result = doQuery(loppukayttaja, ssn);

        if (result == null) {
            return null;
        }

        return validate(parseResponse(result));
    }

    private TeeHenkilonTunnusKyselyResult doQuery(String loppukayttaja, String ssn) {
        String soSoNimi = "PERUSJHHS2";
        String kayttajatunnus = vtjConfig.getUsername();
        String salasana = vtjConfig.getPassword();
        String laskutustiedot = null;
        String henkilotunnus = ssn;
        String sahkoinenAsiointitunnus = null;
        String varmenteenMyontaja = null;
        String x509Certificate = null;
        String varmenteenVoimassaolotarkistus = null;
        String varmenteenSulkulistatarkistus = null;
        String tunnistusportaali = null;
        String vara1 = null;

        return soso.teeHenkilonTunnusKysely(
                soSoNimi,
                kayttajatunnus,
                salasana,
                loppukayttaja,
                laskutustiedot,
                henkilotunnus,
                sahkoinenAsiointitunnus,
                varmenteenMyontaja,
                x509Certificate,
                varmenteenVoimassaolotarkistus,
                varmenteenSulkulistatarkistus,
                tunnistusportaali,
                vara1);
    }

    private static VTJHenkiloVastaussanoma parseResponse(TeeHenkilonTunnusKyselyResult result) throws JAXBException {
        Preconditions.checkArgument(result.getContent().size() == 1,
                "TeeHenkilonTunnusKyselyResult content size should be 1, size:" + result.getContent().size());

        Element data = (Element) result.getContent().get(0);
        JAXBContext jaxbContext = JAXBContext.newInstance(VTJHenkiloVastaussanoma.class);
        Unmarshaller u = jaxbContext.createUnmarshaller();
        JAXBElement<VTJHenkiloVastaussanoma> d = u.unmarshal(data, VTJHenkiloVastaussanoma.class);

        return d.getValue();
    }

    private static VTJHenkiloVastaussanoma.Henkilo validate(VTJHenkiloVastaussanoma resp) {
        Objects.requireNonNull(resp);
        Objects.requireNonNull(resp.getPaluukoodi());
        Objects.requireNonNull(resp.getPaluukoodi().getKoodi());

        switch (resp.getPaluukoodi().getKoodi()) {
            case OK:
                return validateHenkilo(resp);

            case NOT_FOUND:
                LOG.info("SSN not found, returning null");
                return null;

            case PASSIVE:
                LOG.info("SSN is found but passive, returning null");
                return null;

            default:
                String msg = String.format("VTJ returned error, koodi:%s msg:%s",
                        resp.getPaluukoodi().getKoodi(), resp.getPaluukoodi());
                LOG.warn(msg);
                throw new RuntimeException(msg);
        }
    }

    private static VTJHenkiloVastaussanoma.Henkilo validateHenkilo(VTJHenkiloVastaussanoma resp) {
        VTJHenkiloVastaussanoma.Henkilo henkilo = resp.getHenkilo();

        if (henkilo == null) {
            LOG.info("SSN found but henkilo is null, returning null");

            return null;
        }

        if (!"1".equals(henkilo.getHenkilotunnus().getVoimassaolokoodi())) {
            LOG.info("SSN is found but it is passive, returning null");

            return null;
        }

        return henkilo;
    }
}
