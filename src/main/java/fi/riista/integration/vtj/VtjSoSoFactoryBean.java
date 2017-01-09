package fi.riista.integration.vtj;

import com.sun.xml.ws.developer.JAXWSProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.tempuri.SoSo;
import org.tempuri.SoSoSoap;

import javax.annotation.Resource;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import java.net.URL;
import java.security.KeyStore;

@Component
public class VtjSoSoFactoryBean {

    private static final Logger LOG = LoggerFactory.getLogger(VtjSoSoFactoryBean.class);

    private static final QName VTJ_SERVICE__QNAME = new QName("http://tempuri.org/", "SoSo");
    private static final String WSDL_LOCATION = "wsdl/vtj-haku.wsdl";

    @Resource
    private VtjConfig vtjConfig;

    @Resource
    private ResourceLoader resourceLoader;

    @Bean
    public SoSoSoap getSoSo() throws Exception {
        final URL wsdlLocation = new ClassPathResource(WSDL_LOCATION).getURL();
        final SoSo soso = new SoSo(wsdlLocation, VTJ_SERVICE__QNAME);
        final SoSoSoap sosoSoap = soso.getSoSoSoap();
        configureEndpoint(sosoSoap);
        return sosoSoap;
    }

    private void configureEndpoint(SoSoSoap service) throws Exception {
        final SSLContext sc = SSLContext.getInstance("TLS"); // SSLv3
        sc.init(loadKeyManagers(), loadTrustManagers(), null);

        final BindingProvider bindingProvider = (BindingProvider) service;
        bindingProvider.getRequestContext().put(JAXWSProperties.SSL_SOCKET_FACTORY, sc.getSocketFactory());
        bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, vtjConfig.getEndpointAddress());

        LOG.info("Using endpoint address: {}", vtjConfig.getEndpointAddress());
    }

    private javax.net.ssl.TrustManager[] loadTrustManagers() throws Exception {
        final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        final KeyStore ts = KeyStore.getInstance(KeyStore.getDefaultType());
        ts.load(vtjConfig.getTrustStoreResource().getInputStream(), vtjConfig.getTrustStorePassword());

        LOG.info("Trust store contains {} certificates", ts.size());

        tmf.init(ts);
        return tmf.getTrustManagers();
    }

    private javax.net.ssl.KeyManager[] loadKeyManagers() throws Exception {
        if (!vtjConfig.useClientX509Certificate()) {
            LOG.info("Not using client certificate");
            return null;
        }

        LOG.info("Using client certificate");

        final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        final org.springframework.core.io.Resource keyStore = resourceLoader.getResource(vtjConfig.getKeyStoreLocation());
        ks.load(keyStore.getInputStream(), vtjConfig.getKeyStorePassword());
        kmf.init(ks, vtjConfig.getKeyStorePassword());
        return kmf.getKeyManagers();
    }
}
