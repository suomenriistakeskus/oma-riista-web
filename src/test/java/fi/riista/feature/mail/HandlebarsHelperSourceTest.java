package fi.riista.feature.mail;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import fi.riista.config.Constants;
import fi.riista.test.EmbeddedDatabaseTest;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class HandlebarsHelperSourceTest extends EmbeddedDatabaseTest {

    private enum TestEnum {
        VALUE_ONE,
        VALUE_TWO,
        VALUE_THREE
    }

    @Resource
    private Handlebars handlebars;

    private HashMap<String, Object> model;

    @Before
    public void initModel() {
        model = new HashMap<>();
        model.put("Double", new Double(1234.5678910));
        model.put("Integer", new Integer(1234));
        model.put("Float", new Float(1234.5678));
        model.put("null", null);
    }

    @Test
    public void testTimestamp() {
        Date input = new DateTime(2012, 6, 30, 15, 21, Constants.DEFAULT_TIMEZONE).toDate();
        String expected = "2012.06.30 15:21";

        assertEquals(expected, HandlebarsHelperSource.timestamp(input, "yyyy.MM.dd HH:mm"));
    }

    @Test
    public void testUppercase() {
        String input = "just a string that needs to be uppercased.";
        String expected = "JUST A STRING THAT NEEDS TO BE UPPERCASED.";

        assertEquals(expected, HandlebarsHelperSource.toUpperCase(input));
    }

    @Test
    public void testEnumName() {
        TestEnum input = TestEnum.VALUE_TWO;
        String expected = "TestEnum.VALUE_TWO";

        assertEquals(expected, HandlebarsHelperSource.enumName(input));
    }

    @Test
    public void testBoolName() {
        String expected = "Boolean.true";
        assertEquals(expected, HandlebarsHelperSource.boolName(true));

        expected = "Boolean.false";
        assertEquals(expected, HandlebarsHelperSource.boolName(false));
    }

    @Test
    public void testNumber() throws IOException {
        String input = "{{number Double '#.#' locale='fi'}}";
        String expected = "1234,6";

        assertThat(compileHbs(input), containsString(expected));
    }

    @Test
    public void testNumberRemoveDecimals() throws IOException {
        String input = "{{number Float '#' locale='fi'}}";
        String expected = "1235";

        assertThat(compileHbs(input), containsString(expected));
    }

    @Test
    public void testNumberAddDecimals() throws IOException {
        String input = "{{number Integer '#.000' locale='fi'}}";
        String expected = "1234,000";

        assertThat(compileHbs(input), containsString(expected));
    }

    @Test
    public void testNumberEnglishLocale() throws IOException {
        String input = "{{number Double '#.##' locale='en'}}";
        String expected = "1234.57";

        assertThat(compileHbs(input), containsString(expected));
    }

    @Test
    public void testi18nDefaultLocale() throws IOException {
        String input = "{{i18n 'GameAge.ADULT'}}";
        String expected = "Aikuinen";

        assertThat(compileHbs(input), containsString(expected));
    }

    @Test
    public void testi18nFinnish() throws IOException {
        String input = "{{i18n 'GameAge.ADULT' locale='fi'}}";
        String expected = "Aikuinen";

        assertThat(compileHbs(input), containsString(expected));
    }

    @Test
    public void testi18nSwedish() throws IOException {
        String input = "{{i18n 'GameAge.ADULT' locale='sv'}}";
        String expected = "Vuxen";

        assertThat(compileHbs(input), containsString(expected));
    }

    @Test
    public void testi18nNotFound() throws IOException {
        String input = "{{i18n 'ThisStringShould.NotReallyExistInTheResources' locale='fi'}}";
        String expected = "ThisStringShould.NotReallyExistInTheResources";

        assertThat(compileHbs(input), containsString(expected));
    }

    @Test
    public void testi18nDefault() throws IOException {
        String input = "{{i18n 'ThisStringShould.NotReallyExistInTheResources' default='Adult' locale='fi'}}";
        String expected = "Adult";

        assertThat(compileHbs(input), containsString(expected));
    }

    private String compileHbs(String template) throws IOException {
        Context context = Context.newBuilder(model).combine(model).build();
        return handlebars.compileInline(template).apply(context);
    }
}
