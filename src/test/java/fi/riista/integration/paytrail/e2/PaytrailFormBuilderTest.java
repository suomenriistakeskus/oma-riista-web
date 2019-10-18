package fi.riista.integration.paytrail.e2;

import fi.riista.integration.paytrail.e2.model.CallbackUrlSet;
import fi.riista.integration.paytrail.e2.model.Payment;
import fi.riista.integration.paytrail.e2.model.Product;
import fi.riista.util.BigDecimalMoney;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.net.URI;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PaytrailFormBuilderTest {

    // Test material is based on example calculation from https://docs.paytrail.com/en/ch04s03.html
    @Test
    public void testSmoke() {
        final Map<String, String> formFields = new PaytrailFormBuilder(createFormForSmokeTest())
                .includeHardCodedAlgorithmParameter()
                .withMerchantId("13466")
                .withMerchantSecret("6pKF4jkv97zmqBJ3ZL8gUw5DfT2NMQ")
                .withParamsOut("ORDER_NUMBER,PAYMENT_ID,AMOUNT,CURRENCY,PAYMENT_METHOD,TIMESTAMP,STATUS")
                .build();

        assertEquals("13466", formFields.get("MERCHANT_ID"));
        assertEquals("EUR", formFields.get("CURRENCY"));
        assertEquals("http://www.example.com/success", formFields.get("URL_SUCCESS"));
        assertEquals("http://www.example.com/cancel", formFields.get("URL_CANCEL"));
        assertEquals("123456", formFields.get("ORDER_NUMBER"));
        assertNull(formFields.get("AMOUNT"));
        assertEquals("MERCHANT_ID,URL_SUCCESS,URL_CANCEL,ORDER_NUMBER,PARAMS_IN,PARAMS_OUT,ITEM_TITLE[0],ITEM_ID[0],ITEM_QUANTITY[0],ITEM_UNIT_PRICE[0],ITEM_VAT_PERCENT[0],ITEM_DISCOUNT_PERCENT[0],ITEM_TYPE[0],ITEM_TITLE[1],ITEM_ID[1],ITEM_QUANTITY[1],ITEM_UNIT_PRICE[1],ITEM_VAT_PERCENT[1],ITEM_DISCOUNT_PERCENT[1],ITEM_TYPE[1],MSG_UI_MERCHANT_PANEL,URL_NOTIFY,LOCALE,CURRENCY,REFERENCE_NUMBER,PAYMENT_METHODS,PAYER_PERSON_PHONE,PAYER_PERSON_EMAIL,PAYER_PERSON_FIRSTNAME,PAYER_PERSON_LASTNAME,PAYER_COMPANY_NAME,PAYER_PERSON_ADDR_STREET,PAYER_PERSON_ADDR_POSTAL_CODE,PAYER_PERSON_ADDR_TOWN,PAYER_PERSON_ADDR_COUNTRY,VAT_IS_INCLUDED,ALG", formFields.get("PARAMS_IN"));
        assertEquals("ORDER_NUMBER,PAYMENT_ID,AMOUNT,CURRENCY,PAYMENT_METHOD,TIMESTAMP,STATUS", formFields.get("PARAMS_OUT"));
        assertEquals("1", formFields.get("ALG"));
        assertEquals("EAC78AB322614BB98F43FFF2EF55E71075DDC79634EA728C9C842EFF8E0AC0C9", formFields.get("AUTHCODE"));
        assertEquals("http://www.example.com/notify", formFields.get("URL_NOTIFY"));
        assertNull(formFields.get("PARAMS_OUT_NOTIFY"));
        assertEquals("en_US", formFields.get("LOCALE"));
        assertEquals("", formFields.get("REFERENCE_NUMBER"));
        assertEquals("", formFields.get("PAYMENT_METHODS"));
        assertEquals("1", formFields.get("VAT_IS_INCLUDED"));
        assertNull(formFields.get("MSG_SETTLEMENT_PAYER"));
        assertNull(formFields.get("MSG_SETTLEMENT_MERCHANT "));
        assertNull(formFields.get("MSG_UI_PAYMENT_METHOD"));
        assertEquals("Order 123456", formFields.get("MSG_UI_MERCHANT_PANEL"));
        assertEquals("John", formFields.get("PAYER_PERSON_FIRSTNAME"));
        assertEquals("Doe", formFields.get("PAYER_PERSON_LASTNAME"));
        assertEquals("john.doe@example.com", formFields.get("PAYER_PERSON_EMAIL"));
        assertEquals("01234567890", formFields.get("PAYER_PERSON_PHONE"));
        assertEquals("Test street 1", formFields.get("PAYER_PERSON_ADDR_STREET"));
        assertEquals("608009", formFields.get("PAYER_PERSON_ADDR_POSTAL_CODE"));
        assertEquals("Test town", formFields.get("PAYER_PERSON_ADDR_TOWN"));
        assertEquals("AA", formFields.get("PAYER_PERSON_ADDR_COUNTRY"));
        assertEquals("Test company", formFields.get("PAYER_COMPANY_NAME"));
        assertEquals("Product 101", formFields.get("ITEM_TITLE[0]"));
        assertEquals("101", formFields.get("ITEM_ID[0]"));
        assertEquals("2", formFields.get("ITEM_QUANTITY[0]"));
        assertEquals("300.00", formFields.get("ITEM_UNIT_PRICE[0]"));
        assertEquals("15.00", formFields.get("ITEM_VAT_PERCENT[0]"));
        assertEquals("50", formFields.get("ITEM_DISCOUNT_PERCENT[0]"));
        assertEquals("1", formFields.get("ITEM_TYPE[0]"));
        assertEquals("Product 202", formFields.get("ITEM_TITLE[1]"));
        assertEquals("202", formFields.get("ITEM_ID[1]"));
        assertEquals("4", formFields.get("ITEM_QUANTITY[1]"));
        assertEquals("12.50", formFields.get("ITEM_UNIT_PRICE[1]"));
        assertEquals("0", formFields.get("ITEM_VAT_PERCENT[1]"));
        assertEquals("0", formFields.get("ITEM_DISCOUNT_PERCENT[1]"));
        assertEquals("1", formFields.get("ITEM_TYPE[1]"));
    }

    @Nonnull
    private static Payment createFormForSmokeTest() {
        final Product product1 = new Product();
        product1.setTitle("Product 101");
        product1.setId("101");
        product1.setQuantity(2);
        product1.setUnitPrice(new BigDecimalMoney(300, 0));
        product1.setVatPercent(15);
        product1.setDiscountPercent(50);
        product1.setType(1);

        final Product product2 = new Product();
        product2.setTitle("Product 202");
        product2.setId("202");
        product2.setQuantity(4);
        product2.setUnitPrice(new BigDecimalMoney(12, 50));
        product2.setVatPercent(0);
        product2.setDiscountPercent(0);
        product2.setType(1);

        final CallbackUrlSet callbacks = new CallbackUrlSet();
        callbacks.setSuccessUri(URI.create("http://www.example.com/success"));
        callbacks.setCancelUri(URI.create("http://www.example.com/cancel"));
        callbacks.setNotifyUri(URI.create("http://www.example.com/notify"));

        final Payment model = new Payment();
        model.setCallbacks(callbacks);
        model.setOrderNumber("123456");

        model.getProducts().add(product1);
        model.getProducts().add(product2);

        model.setMsgUiMerchantPanel("Order 123456");
        model.setLocale(new Locale("en", "US"));
        model.setCurrency("EUR");
        model.setReferenceNumber("");
        model.setPaymentMethods("");
        model.getPayerPerson().setPhoneNumber("01234567890");
        model.getPayerPerson().setEmail("john.doe@example.com");
        model.getPayerPerson().setFirstName("John");
        model.getPayerPerson().setLastName("Doe");
        model.getPayerPerson().setCompanyName("Test company");
        model.getPayerPerson().setStreetAddress("Test street 1");
        model.getPayerPerson().setPostalCode("608009");
        model.getPayerPerson().setTown("Test town");
        model.getPayerPerson().setCountry("AA");
        model.setVatIsIncluded(true);

        return model;
    }

    @Test
    public void testMinimalExampleFromDocumentation() {
        final CallbackUrlSet callbacks = new CallbackUrlSet();
        callbacks.setSuccessUri(URI.create("http://www.example.com/success"));
        callbacks.setCancelUri(URI.create("http://www.example.com/cancel"));

        final Payment model = new Payment();
        model.setCallbacks(callbacks);
        model.setOrderNumber("123456");
        model.setAmount(new BigDecimalMoney(350, 0));

        final Map<String, String> formFields = new PaytrailFormBuilder(model)
                .withMerchantId("13466")
                .withMerchantSecret("6pKF4jkv97zmqBJ3ZL8gUw5DfT2NMQ")
                .withParamsOut("PAYMENT_ID,TIMESTAMP,STATUS")
                .build();

        assertEquals(8, formFields.size());
        assertEquals("13466", formFields.get("MERCHANT_ID"));
        assertEquals("http://www.example.com/success", formFields.get("URL_SUCCESS"));
        assertEquals("http://www.example.com/cancel", formFields.get("URL_CANCEL"));
        assertEquals("123456", formFields.get("ORDER_NUMBER"));
        assertEquals("350.00", formFields.get("AMOUNT"));
        assertEquals("MERCHANT_ID,URL_SUCCESS,URL_CANCEL,ORDER_NUMBER,PARAMS_IN,PARAMS_OUT,AMOUNT", formFields.get("PARAMS_IN"));
        assertEquals("PAYMENT_ID,TIMESTAMP,STATUS", formFields.get("PARAMS_OUT"));
        assertNull(formFields.get("ALG"));
        assertEquals("BBDF8997A56F97DC0A46C99C88C2EEF9D541AAD59CFF2695D0DD9AF474086D71", formFields.get("AUTHCODE"));
        assertNull(formFields.get("URL_NOTIFY"));
        assertNull(formFields.get("PARAMS_OUT_NOTIFY"));
        assertNull(formFields.get("LOCALE"));
        assertNull(formFields.get("REFERENCE_NUMBER"));
        assertNull(formFields.get("PAYMENT_METHODS"));
        assertNull(formFields.get("VAT_IS_INCLUDED"));
        assertNull(formFields.get("MSG_SETTLEMENT_PAYER"));
        assertNull(formFields.get("MSG_SETTLEMENT_MERCHANT "));
        assertNull(formFields.get("MSG_UI_PAYMENT_METHOD"));
        assertNull(formFields.get("MSG_UI_MERCHANT_PANEL"));
        assertNull(formFields.get("PAYER_PERSON_FIRSTNAME"));
        assertNull(formFields.get("PAYER_PERSON_LASTNAME"));
        assertNull(formFields.get("PAYER_PERSON_EMAIL"));
        assertNull(formFields.get("PAYER_PERSON_PHONE"));
        assertNull(formFields.get("PAYER_PERSON_ADDR_STREET"));
        assertNull(formFields.get("PAYER_PERSON_ADDR_POSTAL_CODE"));
        assertNull(formFields.get("PAYER_PERSON_ADDR_TOWN"));
        assertNull(formFields.get("PAYER_PERSON_ADDR_COUNTRY"));
        assertNull(formFields.get("PAYER_COMPANY_NAME"));
    }
}
