package fi.riista.integration.paytrail.e2;

import fi.riista.integration.paytrail.e2.model.Product;
import fi.riista.util.Collect;
import org.springframework.util.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class PaytrailForm {
    private final List<PaytrailFormField> fields = new LinkedList<>();
    private int productCounter = 0;

    public void addRequiredField(final PaytrailFormFieldType fieldType, final int orderingNumber, final String value) {
        Objects.requireNonNull(value, "field " + fieldType + " is required");
        fields.add(new PaytrailFormField(fieldType, orderingNumber, value));
    }

    public void addRequiredField(final PaytrailFormFieldType fieldType, final String value) {
        Objects.requireNonNull(value, "field " + fieldType + " is required");
        fields.add(new PaytrailFormField(fieldType, value));
    }

    public void addRequiredField(final PaytrailFormFieldType fieldType, final Object value) {
        Objects.requireNonNull(value, "field " + fieldType + " is required");
        fields.add(new PaytrailFormField(fieldType, value.toString()));
    }

    public void addOptionalField(final PaytrailFormFieldType fieldType, final String value) {
        if (StringUtils.hasText(value)) {
            fields.add(new PaytrailFormField(fieldType, value));
        }
    }

    public void addOptionalField(final PaytrailFormFieldType fieldType, final Object value) {
        if (value != null) {
            fields.add(new PaytrailFormField(fieldType, value.toString()));
        }
    }

    public void addOptionalField(final PaytrailFormFieldType fieldType, final int orderingNumber, final String value) {
        if (StringUtils.hasText(value)) {
            fields.add(new PaytrailFormField(fieldType, orderingNumber, value));
        }
    }

    public void addOptionalField(final PaytrailFormFieldType fieldType, final int orderingNumber, final Object value) {
        if (value != null) {
            fields.add(new PaytrailFormField(fieldType, orderingNumber, value.toString()));
        }
    }

    public void addProduct(final Product product) {
        addRequiredField(PaytrailFormFieldType.ITEM_TITLE, productCounter, product.getTitle());
        addOptionalField(PaytrailFormFieldType.ITEM_ID, productCounter, product.getId());
        addOptionalField(PaytrailFormFieldType.ITEM_QUANTITY, productCounter, product.getQuantity());
        addRequiredField(PaytrailFormFieldType.ITEM_UNIT_PRICE, productCounter, product.getUnitPrice().toString());
        addRequiredField(PaytrailFormFieldType.ITEM_VAT_PERCENT, productCounter,
                product.getVatPercent() == 0 ? "0" : String.format("%d.00", product.getVatPercent()));
        addOptionalField(PaytrailFormFieldType.ITEM_DISCOUNT_PERCENT, productCounter, product.getDiscountPercent());
        addOptionalField(PaytrailFormFieldType.ITEM_TYPE, productCounter, product.getType());

        productCounter++;
    }

    public void updateParametersIn() {
        final String paramsIn = fields.stream()
                .map(PaytrailFormField::getFieldName)
                .collect(Collectors.joining(","));

        final ListIterator<PaytrailFormField> listIterator = fields.listIterator();

        while (listIterator.hasNext()) {
            final PaytrailFormField field = listIterator.next();

            if (field.getFieldType() == PaytrailFormFieldType.PARAMS_IN) {
                listIterator.set(new PaytrailFormField(PaytrailFormFieldType.PARAMS_IN, paramsIn));
                break;
            }
        }
    }

    public List<String> getFieldsForAuthCode() {
        updateParametersIn();
        return fields.stream().map(PaytrailFormField::getFieldValue).collect(Collectors.toList());
    }

    public Map<String, String> getFieldsAsMap() {
        return fields.stream().collect(Collect.toMap(PaytrailFormField::getFieldName, PaytrailFormField::getFieldValue, TreeMap::new));
    }
}
