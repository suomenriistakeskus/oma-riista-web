package fi.riista.integration.support;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.math.BigDecimal;
import java.text.DecimalFormat;

public class TwoDecimalBigDecimalAdapter extends XmlAdapter<String, BigDecimal> {
    private static DecimalFormat CURRENCY_FORMAT = new DecimalFormat("0.00");

    @Override
    public String marshal(final BigDecimal value) {
        if (value != null) {
            return CURRENCY_FORMAT.format(value);
        }
        return null;
    }

    @Override
    public BigDecimal unmarshal(String s) {
        return new BigDecimal(s);
    }
}
