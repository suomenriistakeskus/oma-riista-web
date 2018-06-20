package fi.riista.feature.permit.decision;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PermitDecisionDocumentTransformerTest {

    private static String transform(final String input) {
        return PermitDecisionDocumentTransformer.MARKDOWN_TO_HTML.transform(input);
    }

    @Test
    public void testSmoke() {
        final String input = "*Ensimmäinen* kappale\nja toinen rivi.\n\nToinen kappale.\n" +
                "\n" +
                "| Eläin            | Määrä        | Aika                    |\n" +
                "| ---------------- | -----------  | ---------------------   |\n" +
                "| hirvi            | 24.3         | 1.9.2018 - 31.7.2018    |\n" +
                "| valkohäntäpeura  | 52.4         | 1.10.2018 - 31.7.2018   |\n" +
                "| ---------------- | ------------ | ----------------------- |\n" +
                "\n" +
                "Kolmas kappale.";

        final String expected = "<p><em>Ensimmäinen</em> kappale<br />\n" +
                "ja toinen rivi.</p>\n" +
                "<p>Toinen kappale.</p>\n" +
                "<table>\n" +
                "<thead>\n" +
                "<tr><th>Eläin</th><th>Määrä</th><th>Aika</th></tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr><td>hirvi</td><td>24.3</td><td>1.9.2018 - 31.7.2018</td></tr>\n" +
                "<tr><td>valkohäntäpeura</td><td>52.4</td><td>1.10.2018 - 31.7.2018</td></tr>\n" +
                "<tr><td>----------------</td><td>------------</td><td>-----------------------</td></tr>\n" +
                "</tbody>\n" +
                "</table>\n" +
                "<p>Kolmas kappale.</p>\n";

        assertEquals(expected, transform(input));
    }

    @Test
    public void testMinimalTable() {
        final String  input = "Eläin | Määrä | Aika\n" +
                "---|---|---\n" +
                "hirvi|0.5|1.9.2018 - 31.7.2018\n" +
                "valkohäntäpeura|200.5|31.2.2018 - 20.8.2018\n";

        final String expected = "<table>\n" +
                "<thead>\n" +
                "<tr><th>Eläin</th><th>Määrä</th><th>Aika</th></tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr><td>hirvi</td><td>0.5</td><td>1.9.2018 - 31.7.2018</td></tr>\n" +
                "<tr><td>valkohäntäpeura</td><td>200.5</td><td>31.2.2018 - 20.8.2018</td></tr>\n" +
                "</tbody>\n" +
                "</table>\n";

        assertEquals(expected, transform(input));
    }

}
