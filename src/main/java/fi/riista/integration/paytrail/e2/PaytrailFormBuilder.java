package fi.riista.integration.paytrail.e2;

import fi.riista.api.external.PaytrailController;
import fi.riista.integration.paytrail.auth.PaytrailAuthCodeBuilder;
import fi.riista.integration.paytrail.auth.PaytrailAuthCodeDigest;
import fi.riista.integration.paytrail.auth.PaytrailCredentials;
import fi.riista.integration.paytrail.e2.model.Payment;
import fi.riista.integration.paytrail.e2.model.Product;
import fi.riista.util.LocalisedString;
import org.apache.commons.lang.StringUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

// ref: https://docs.paytrail.com/en/ch04s03.html
public class PaytrailFormBuilder {
    public static Map<String, String> createForm(final PaytrailCredentials paytrailApiCredentials,
                                                 final Payment payment) {
        return new PaytrailFormBuilder(payment)
                .withMerchantId(paytrailApiCredentials.getMerchantId())
                .withMerchantSecret(paytrailApiCredentials.getMerchantSecret())
                .build();
    }

    private static final LocalisedString SUPPORTED_LOCALES = new LocalisedString("fi_FI", "sv_SE", "en_US");

    private final Payment model;
    private String merchantId;
    private String merchantSecret;
    private String paramsOut = PaytrailController.PARAMS_OUT;
    private boolean includeAlgorithm;

    public PaytrailFormBuilder(final Payment model) {
        this.model = Objects.requireNonNull(model);
    }

    public PaytrailFormBuilder withParamsOut(final String paramsOut) {
        this.paramsOut = paramsOut;
        return this;
    }

    public PaytrailFormBuilder withMerchantId(final String merchantId) {
        this.merchantId = merchantId;
        return this;
    }

    public PaytrailFormBuilder withMerchantSecret(final String merchantSecret) {
        this.merchantSecret = merchantSecret;
        return this;
    }

    public PaytrailFormBuilder includeHardCodedAlgorithmParameter() {
        this.includeAlgorithm = true;
        return this;
    }

    private void validateModel() {
        if (model.getCallbacks() == null ||
                model.getCallbacks().getSuccessUri() == null ||
                model.getCallbacks().getCancelUri() == null) {
            throw new IllegalArgumentException("missing callbacks");
        }

        if (StringUtils.isBlank(merchantId)) {
            throw new IllegalArgumentException("missing merchantId");
        }

        if (StringUtils.isBlank(merchantSecret)) {
            throw new IllegalArgumentException("missing merchantSecret");
        }

        if (StringUtils.isBlank(paramsOut)) {
            throw new IllegalArgumentException("missing paramsOut");
        }

        if (model.getProducts().isEmpty() && model.getAmount() == null) {
            throw new IllegalArgumentException("Empty product array and amount not specified");
        }

        if (model.getProducts().isEmpty() && model.getVatIsIncluded() != null) {
            throw new IllegalArgumentException("Vat included given but product array is empty");
        }

        if (!model.getProducts().isEmpty() && model.getAmount() != null) {
            throw new IllegalArgumentException("Amount given but product array not empty");
        }

        final Set<ConstraintViolation<Payment>> constraintViolations = validatePayment(model);

        if (!constraintViolations.isEmpty()) {
            throw new IllegalArgumentException(getErrorMessage(constraintViolations));
        }
    }

    private static Set<ConstraintViolation<Payment>> validatePayment(final Payment payment) {
        final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();
        return validator.validate(payment);
    }

    private static String getErrorMessage(final Set<ConstraintViolation<Payment>> constraintViolations) {
        final StringBuilder errorMessageBuilder = new StringBuilder("Payment model validation has failed: \n");

        for (final ConstraintViolation<?> violation : constraintViolations) {
            final String fieldName = String.format("%s.%s",
                    violation.getRootBeanClass().getSimpleName(),
                    violation.getPropertyPath());
            final String fieldErrorCode = violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName();
            final String fieldErrorMessage;

            if (violation.getMessage() != null && !violation.getMessage().matches("^\\{.+\\}$")) {
                fieldErrorMessage = String.format("%s, was %s",
                        violation.getMessage(),
                        org.springframework.util.StringUtils.quoteIfString(violation.getInvalidValue()));
            } else {
                fieldErrorMessage = "";
            }

            errorMessageBuilder.append(String.format("- %s: %s %s\n", fieldName, fieldErrorCode, fieldErrorMessage));
        }

        return errorMessageBuilder.toString();
    }

    public Map<String, String> build() {
        validateModel();

        final PaytrailForm form = new PaytrailForm();

        form.addRequiredField(PaytrailFormFieldType.MERCHANT_ID, merchantId);
        form.addRequiredField(PaytrailFormFieldType.URL_SUCCESS, model.getCallbacks().getSuccessUri());
        form.addRequiredField(PaytrailFormFieldType.URL_CANCEL, model.getCallbacks().getCancelUri());
        form.addRequiredField(PaytrailFormFieldType.ORDER_NUMBER, model.getOrderNumber());
        form.addRequiredField(PaytrailFormFieldType.PARAMS_IN, "");
        form.addRequiredField(PaytrailFormFieldType.PARAMS_OUT, paramsOut);

        if (model.getAmount() != null) {
            form.addRequiredField(PaytrailFormFieldType.AMOUNT, model.getAmount().formatPaymentAmount());
        }

        for (final Product product : model.getProducts()) {
            form.addProduct(product);
        }

        form.addOptionalField(PaytrailFormFieldType.MSG_SETTLEMENT_PAYER, model.getMsgSettlementPayer());
        form.addOptionalField(PaytrailFormFieldType.MSG_SETTLEMENT_MERCHANT, model.getMsgSettlementMerchant());
        form.addOptionalField(PaytrailFormFieldType.MSG_UI_PAYMENT_METHOD, model.getPaymentMethods());
        form.addOptionalField(PaytrailFormFieldType.MSG_UI_MERCHANT_PANEL, model.getMsgUiMerchantPanel());
        form.addOptionalField(PaytrailFormFieldType.URL_NOTIFY, model.getCallbacks().getNotifyUri());
        if (model.getLocale() != null) {
            form.addRequiredField(PaytrailFormFieldType.LOCALE, SUPPORTED_LOCALES.getAnyTranslation(model.getLocale()));
        }
        form.addOptionalField(PaytrailFormFieldType.CURRENCY, model.getCurrency());

        if (model.getReferenceNumber() != null) {
            form.addRequiredField(PaytrailFormFieldType.REFERENCE_NUMBER, model.getReferenceNumber());
        }

        if (model.getPaymentMethods() != null) {
            form.addRequiredField(PaytrailFormFieldType.PAYMENT_METHODS, model.getPaymentMethods());
        }

        form.addOptionalField(PaytrailFormFieldType.PAYER_PERSON_PHONE, model.getPayerPerson().getPhoneNumber());
        form.addOptionalField(PaytrailFormFieldType.PAYER_PERSON_EMAIL, model.getPayerPerson().getEmail());
        form.addOptionalField(PaytrailFormFieldType.PAYER_PERSON_FIRSTNAME, model.getPayerPerson().getFirstName());
        form.addOptionalField(PaytrailFormFieldType.PAYER_PERSON_LASTNAME, model.getPayerPerson().getLastName());
        form.addOptionalField(PaytrailFormFieldType.PAYER_COMPANY_NAME, model.getPayerPerson().getCompanyName());
        form.addOptionalField(PaytrailFormFieldType.PAYER_PERSON_ADDR_STREET, model.getPayerPerson().getStreetAddress());
        form.addOptionalField(PaytrailFormFieldType.PAYER_PERSON_ADDR_POSTAL_CODE, model.getPayerPerson().getPostalCode());
        form.addOptionalField(PaytrailFormFieldType.PAYER_PERSON_ADDR_TOWN, model.getPayerPerson().getTown());
        form.addOptionalField(PaytrailFormFieldType.PAYER_PERSON_ADDR_COUNTRY, model.getPayerPerson().getCountry());

        if (model.getVatIsIncluded() != null) {
            form.addOptionalField(PaytrailFormFieldType.VAT_IS_INCLUDED, model.getVatIsIncluded() ? "1" : "0");
        }

        if (includeAlgorithm) {
            form.addRequiredField(PaytrailFormFieldType.ALG, "1");
        }

        final String authCode = new PaytrailAuthCodeBuilder(merchantSecret, PaytrailAuthCodeDigest.SHA256)
                .withFields(form.getFieldsForAuthCode())
                .getAuthCode(PaytrailAuthCodeBuilder.SecretAlignment.BEFORE);

        form.addRequiredField(PaytrailFormFieldType.AUTHCODE, authCode);

        return form.getFieldsAsMap();
    }
}
