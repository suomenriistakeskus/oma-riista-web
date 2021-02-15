package fi.riista.integration.paytrail.rest.client;

import fi.riista.config.JaxbConfig;
import fi.riista.integration.paytrail.auth.PaytrailCredentials;
import fi.riista.integration.paytrail.rest.PaytrailRestAdapter;
import fi.riista.integration.paytrail.rest.model.Contact;
import fi.riista.integration.paytrail.rest.model.ContactAddress;
import fi.riista.integration.paytrail.rest.model.CreatePaymentRequest;
import fi.riista.integration.paytrail.rest.model.CreatePaymentResponse;
import fi.riista.integration.paytrail.rest.model.OrderDetails;
import fi.riista.integration.paytrail.rest.model.Product;
import fi.riista.integration.paytrail.rest.model.ProductList;
import fi.riista.integration.paytrail.rest.model.ProductType;
import fi.riista.integration.paytrail.rest.model.UrlSet;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.web.client.RestTemplate;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.net.URI;

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

public class PaytrailRestTemplateTest {

    private static PaytrailCredentials createTestCredentials() {
        // Public test credentials from http://docs.paytrail.com/en/ch03.html
        return new PaytrailCredentials("13466", "6pKF4jkv97zmqBJ3ZL8gUw5DfT2NMQ");
    }

    private static RestTemplate createRestTemplate() {
        final Jaxb2Marshaller marshaller = new JaxbConfig().paytrailMarshaller();

        try {
            marshaller.afterPropertiesSet();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new PaytrailRestTemplate(new SimpleClientHttpRequestFactory(), marshaller, createTestCredentials());
    }

    private static void validateRequest(final CreatePaymentRequest request) {
        final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        assertThat(validator.validate(request), hasSize(0));
    }

    @Test
    public void testSuccessSimple() {
        final CreatePaymentRequest request = new CreatePaymentRequest();
        request.setOrderNumber("12345678");
        request.setCurrency("EUR");
        request.setLocale("fi_FI");
        request.setPrice(new BigDecimal("99"));

        final UrlSet urlSet = new UrlSet();
        request.setUrlSet(urlSet);
        urlSet.setSuccess(URI.create("https://www.esimerkkikauppa.fi/sv/success"));
        urlSet.setFailure(URI.create("https://www.esimerkkikauppa.fi/sv/failure"));
        urlSet.setNotification(URI.create("https://www.esimerkkikauppa.fi/sv/notify"));

        validateRequest(request);

        final RestTemplate restTemplate = createRestTemplate();
        final MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);

        mockServer.expect(MockRestRequestMatchers.requestTo(PaytrailRestAdapter.REST_PAYMENT_URI))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Content-Type", "application/xml"))
                .andExpect(header("Accept", "application/xml"))
                .andExpect(header("Authorization", "Basic MTM0NjY6NnBLRjRqa3Y5N3ptcUJKM1pMOGdVdzVEZlQyTk1R"))
                .andExpect(header("X-Verkkomaksut-Api-Version", "1"))
                .andExpect(content().string("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n" +
                        "<payment>\n" +
                        "    <orderNumber>12345678</orderNumber>\n" +
                        "    <currency>EUR</currency>\n" +
                        "    <locale>fi_FI</locale>\n" +
                        "    <urlSet>\n" +
                        "        <success>https://www.esimerkkikauppa.fi/sv/success</success>\n" +
                        "        <failure>https://www.esimerkkikauppa.fi/sv/failure</failure>\n" +
                        "        <notification>https://www.esimerkkikauppa.fi/sv/notify</notification>\n" +
                        "    </urlSet>\n" +
                        "    <price>99.00</price>\n" +
                        "</payment>\n")
                ).andRespond(withStatus(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_XML)
                .body("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<payment>" +
                        "<orderNumber>12345678</orderNumber>" +
                        "<token>TKASAbNXkrXpKY7Zu7gcbq5sv6dyqxcP</token>" +
                        "<url>https://payment.paytrail.com/payment/load/token/TKASAbNXkrXpKY7Zu7gcbq5sv6dyqxcP</url>" +
                        "</payment>"));

        final CreatePaymentResponse response = restTemplate.postForObject(
                PaytrailRestAdapter.REST_PAYMENT_URI, request, CreatePaymentResponse.class);
        mockServer.verify();

        assertThat(response.getOrderNumber(), equalTo("12345678"));
        assertThat(response.getToken(), equalTo("TKASAbNXkrXpKY7Zu7gcbq5sv6dyqxcP"));
        assertThat(response.getUrl(), equalTo(URI.create("https://payment.paytrail.com/payment/load/token/TKASAbNXkrXpKY7Zu7gcbq5sv6dyqxcP")));
    }

    @Test
    public void testSuccessComplete() {
        final CreatePaymentRequest request = new CreatePaymentRequest();
        request.setOrderNumber("12345678");
        request.setCurrency("EUR");
        request.setLocale("fi_FI");

        final UrlSet urlSet = new UrlSet();
        request.setUrlSet(urlSet);
        urlSet.setSuccess(URI.create("https://www.esimerkkikauppa.fi/sv/success"));
        urlSet.setFailure(URI.create("https://www.esimerkkikauppa.fi/sv/failure"));
        urlSet.setNotification(URI.create("https://www.esimerkkikauppa.fi/sv/notify"));

        final ContactAddress address = new ContactAddress();
        address.setStreet("Testikatu 1");
        address.setCountry("FI");
        address.setPostalCode("00001");
        address.setPostalOffice("Kaupunki");

        final Contact contact = new Contact();
        contact.setFirstName("FirstName");
        contact.setLastName("LastName");
        contact.setEmail("user@localhost");
        contact.setAddress(address);

        final Product product = new Product();
        product.setTitle("Product title ääööåå");
        product.setPrice(new BigDecimal("1024.10"));
        product.setAmount(new BigDecimal("1"));
        product.setVat(new BigDecimal("24.00"));
        product.setDiscount(BigDecimal.ZERO);
        product.setType(ProductType.NORMAL);

        final ProductList products = new ProductList();
        products.setProducts(singletonList(product));

        final OrderDetails orderDetails = new OrderDetails();
        orderDetails.setProducts(products);
        orderDetails.setIncludeVat(Boolean.TRUE);
        orderDetails.setContact(contact);

        request.setOrderDetails(orderDetails);

        validateRequest(request);

        final RestTemplate restTemplate = createRestTemplate();
        final MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);

        mockServer.expect(MockRestRequestMatchers.requestTo(PaytrailRestAdapter.REST_PAYMENT_URI))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Content-Type", "application/xml"))
                .andExpect(header("Accept", "application/xml"))
                .andExpect(header("Authorization", "Basic MTM0NjY6NnBLRjRqa3Y5N3ptcUJKM1pMOGdVdzVEZlQyTk1R"))
                .andExpect(header("X-Verkkomaksut-Api-Version", "1"))
                .andExpect(content().string("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n" +
                        "<payment>\n" +
                        "    <orderNumber>12345678</orderNumber>\n" +
                        "    <currency>EUR</currency>\n" +
                        "    <locale>fi_FI</locale>\n" +
                        "    <urlSet>\n" +
                        "        <success>https://www.esimerkkikauppa.fi/sv/success</success>\n" +
                        "        <failure>https://www.esimerkkikauppa.fi/sv/failure</failure>\n" +
                        "        <notification>https://www.esimerkkikauppa.fi/sv/notify</notification>\n" +
                        "    </urlSet>\n" +
                        "    <orderDetails>\n" +
                        "        <includeVat>1</includeVat>\n" +
                        "        <contact>\n" +
                        "            <email>user@localhost</email>\n" +
                        "            <firstName>FirstName</firstName>\n" +
                        "            <lastName>LastName</lastName>\n" +
                        "            <address>\n" +
                        "                <street>Testikatu 1</street>\n" +
                        "                <postalCode>00001</postalCode>\n" +
                        "                <postalOffice>Kaupunki</postalOffice>\n" +
                        "                <country>FI</country>\n" +
                        "            </address>\n" +
                        "        </contact>\n" +
                        "        <products>\n" +
                        "            <product>\n" +
                        "                <title>Product title ääööåå</title>\n" +
                        "                <amount>1.00</amount>\n" +
                        "                <price>1024.10</price>\n" +
                        "                <vat>24.00</vat>\n" +
                        "                <discount>0.00</discount>\n" +
                        "                <type>1</type>\n" +
                        "            </product>\n" +
                        "        </products>\n" +
                        "    </orderDetails>\n" +
                        "</payment>\n"))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_XML)
                        .body("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<payment>" +
                                "<orderNumber>12345678</orderNumber>" +
                                "<token>TKASAbNXkrXpKY7Zu7gcbq5sv6dyqxcP</token>" +
                                "<url>https://payment.paytrail.com/payment/load/token/TKASAbNXkrXpKY7Zu7gcbq5sv6dyqxcP</url>" +
                                "</payment>"));

        final CreatePaymentResponse response = restTemplate.postForObject(
                PaytrailRestAdapter.REST_PAYMENT_URI, request, CreatePaymentResponse.class);
        mockServer.verify();

        assertThat(response.getOrderNumber(), equalTo("12345678"));
        assertThat(response.getToken(), equalTo("TKASAbNXkrXpKY7Zu7gcbq5sv6dyqxcP"));
        assertThat(response.getUrl(), equalTo(URI.create("https://payment.paytrail.com/payment/load/token/TKASAbNXkrXpKY7Zu7gcbq5sv6dyqxcP")));
    }

    @Test
    public void testFailure() {
        final CreatePaymentRequest requestBody = new CreatePaymentRequest();

        final RestTemplate restTemplate = createRestTemplate();
        final MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);

        mockServer.expect(requestTo(PaytrailRestAdapter.REST_PAYMENT_URI))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withBadRequest()
                        .contentType(MediaType.APPLICATION_XML)
                        .body("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<error>\n" +
                                "  <errorCode>invalid-order-number</errorCode>\n" +
                                "  <errorMessage>Missing or invalid order number</errorMessage>\n" +
                                "</error>"));

        try {
            restTemplate.postForObject(PaytrailRestAdapter.REST_PAYMENT_URI, requestBody, CreatePaymentResponse.class);
            fail("exception not thrown");

        } catch (PaytrailClientException e) {
            assertThat(e.getMessage(), equalTo("Paytrail failure code: invalid-order-number message: Missing or invalid order number"));
            assertThat(e.getErrorCode(), equalTo("invalid-order-number"));
            assertThat(e.getErrorMessage(), equalTo("Missing or invalid order number"));
        }
    }
}
