package fi.riista.integration.support;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class BooleanOneZeroAdapter extends XmlAdapter<String, Boolean> {

    private static final String BOOLEAN_TRUE = "1";
    private static final String BOOLEAN_FALSE = "0";

    @Override
    public Boolean unmarshal(final String value) {
        if (BOOLEAN_TRUE.equals(value)) {
            return Boolean.TRUE;
        }

        if (BOOLEAN_FALSE.equals(value)) {
            return Boolean.FALSE;
        }

        return null;
    }

    @Override
    public String marshal(final Boolean value) {
        if (value != null) {
            return value ? BOOLEAN_TRUE : BOOLEAN_FALSE;
        }

        return null;
    }
}
