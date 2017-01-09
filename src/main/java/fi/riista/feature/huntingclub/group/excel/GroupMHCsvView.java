package fi.riista.feature.huntingclub.group.excel;

import fi.riista.config.web.CSVHttpResponse;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.DateUtil;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * CSV is used to report shooter list to Metsähallitus
 */
public class GroupMHCsvView extends CSVHttpResponse {

    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd");

    public static final String[] HEADER = new String[]{
            "seuran nimi",
            "ryhmän metsästysvuosi",
            "ryhmän laji",
            "ryhmän nimi",
            "sukunimi",
            "etunimi",
            "metsästäjänumero",
            "ampumakokeen suorituspvm",
            "katuosoite",
            "postinumero",
            "kaupunki",
            "maa",
            "puhelinnumero",
            "sähköposti"
    };

    public GroupMHCsvView(String filename, String[] headerRow, List<String[]> rows) {
        super(filename, headerRow, rows);
    }

    @Override
    public Charset getCharset() {
        return StandardCharsets.ISO_8859_1;
    }

    public static GroupMHCsvView create(HuntingClub club, List<String[]> rows) {
        return new GroupMHCsvView(createFilename(club), HEADER, rows);
    }

    private static String createFilename(final HuntingClub exportedClub) {
        return ContentDispositionUtil.cleanFileName(String.format("%s-%s.csv",
                exportedClub.getNameFinnish(),
                DATE_FORMAT.print(DateUtil.today())));
    }

    public static String[] csvRow(String clubName, String groupName, String speciesName, int year, Person person, boolean shareContactInfo) {
        final Address a = shareContactInfo ? person.getAddress() : null;
        return new String[]{
                clubName,
                Integer.toString(year),
                speciesName,
                groupName,
                person.getLastName(),
                person.getFirstName(),
                person.getHunterNumber(),
                "", // placeholder for passed shooting test date
                a != null ? a.getStreetAddress() : "",
                a != null ? a.getPostalCode() : "",
                a != null ? a.getCity() : "",
                a != null ? a.getCountry() : "",
                shareContactInfo && person.getPhoneNumber() != null ? person.getPhoneNumber() : "",
                shareContactInfo && person.getEmail() != null ? person.getEmail() : ""
        };
    }
}
