package fi.riista.feature.harvestpermit.report.email;

import com.github.jknack.handlebars.Handlebars;
import com.google.common.collect.Sets;
import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.report.HarvestReport;
import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.Locales;
import org.hamcrest.Matchers;
import org.jsoup.Jsoup;
import org.junit.Test;
import org.springframework.context.MessageSource;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.countMatches;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class HarvestReportListNotificationTest extends EmbeddedDatabaseTest {

    @Resource
    private Handlebars handlebars;

    @Resource
    private MessageSource messageSource;

    @Test
    public void testCount() {
        GameSpecies speciesHarakka = createGameSpecies("harakka");
        GameSpecies speciesKorppi = createGameSpecies("korppi");
        GameSpecies speciesvaris = createGameSpecies("varis");

        Set<Harvest> harvests = Sets.newHashSet(
                createHarvestAndSpecimen(speciesHarakka, 1, GameGender.FEMALE, GameAge.ADULT),
                createHarvestAndSpecimen(speciesvaris, 3, GameGender.UNKNOWN, GameAge.ADULT),
                createHarvestAndSpecimen(speciesKorppi, 4, GameGender.MALE, GameAge.UNKNOWN),
                createHarvestAndSpecimen(speciesHarakka, 1, GameGender.FEMALE, GameAge.YOUNG));

        persistInNewTransaction();

        Map<GameSpecies, HarvestReportListNotification.SpecimenSummary> summaries = HarvestReportListNotification.SpecimenSummary.create(harvests);

        SpecimenSummaryAssert.with(summaries.get(speciesHarakka))
                .total(2)
                .female(2).male(0).genderUnknown(0)
                .adult(1).young(1).ageUnknown(0);

        SpecimenSummaryAssert.with(summaries.get(speciesvaris))
                .total(3)
                .female(0).male(0).genderUnknown(3)
                .adult(3).young(0).ageUnknown(0);

        SpecimenSummaryAssert.with(summaries.get(speciesKorppi))
                .total(4)
                .female(0).male(4).genderUnknown(0)
                .adult(0).young(0).ageUnknown(4);
    }

    private static class SpecimenSummaryAssert {
        private final HarvestReportListNotification.SpecimenSummary sum;

        static SpecimenSummaryAssert with(HarvestReportListNotification.SpecimenSummary sum) {
            return new SpecimenSummaryAssert(sum);
        }

        SpecimenSummaryAssert(HarvestReportListNotification.SpecimenSummary sum) {
            this.sum = sum;
        }

        private SpecimenSummaryAssert total(int expected) {
            assertEquals(expected, sum.getTotal());
            return this;
        }

        private SpecimenSummaryAssert adult(int expected) {
            assertEquals(expected, sum.getAgeAdult());
            return this;
        }

        private SpecimenSummaryAssert young(int expected) {
            assertEquals(expected, sum.getAgeYoung());
            return this;
        }

        private SpecimenSummaryAssert ageUnknown(int expected) {
            assertEquals(expected, sum.getAgeUnknown());
            return this;
        }

        private SpecimenSummaryAssert female(int expected) {
            assertEquals(expected, sum.getGenderFemale());
            return this;
        }

        private SpecimenSummaryAssert male(int expected) {
            assertEquals(expected, sum.getGenderMale());
            return this;
        }

        private SpecimenSummaryAssert genderUnknown(int expected) {
            assertEquals(expected, sum.getGenderUnknown());
            return this;
        }
    }

    @Test
    public void testReportUsingPermit() {
        Harvest harvest = createHarvestAndSpecimen("harakka", 1);

        HarvestReport harvestReport = model().newHarvestReport(harvest, HarvestReport.State.SENT_FOR_APPROVAL);
        setupAuthorData(harvestReport);

        harvestReport.addHarvest(createHarvestAndSpecimen("varis", 2));
        harvestReport.addHarvest(createHarvestAndSpecimen("korppi", 3));
        harvestReport.addHarvest(createHarvestAndSpecimen("räkättirastas", 4));

        HarvestPermit permit = model().newHarvestPermit();

        persistInNewTransaction();

        final MailMessageDTO.Builder notification = new HarvestReportListNotification(handlebars, messageSource)
                .withReport(harvestReport)
                .withPermit(permit)
                .withSummaries(HarvestReportListNotification.SpecimenSummary.create(harvestReport.getHarvests()))
                .withEmail("test@example.com")
                .build();

        MailMessageDTO mailMessage = notification.withFrom("default@example.com").build();

        final String body = Jsoup.parse(mailMessage.getBody()).text();

        // Each species name needs to be listed exactly twice in the message body:
        // Once in the summary table and once in the species specific summary.
        assertEquals(countMatches(body, "harakka"), 2);
        assertEquals(countMatches(body, "varis"), 2);
        assertEquals(countMatches(body, "korppi"), 2);
        assertEquals(countMatches(body, "räkättirastas"), 2);

        // Each harvest statistic that is not null should appear in the tables.
        DecimalFormat weightFormat = new DecimalFormat("#.#", new DecimalFormatSymbols(Locales.FI));

        List<String> words =
                harvestReport.getHarvests().stream()
                        .map(Harvest::getSortedSpecimens)
                        .flatMap(List::stream)
                        .map(h -> {
                            /**
                             * Mapping each harvest specimen entry in the order they will appear in the extra info tables.
                             * Gender is listed first, then age, and then weight.
                             */
                            String gender = "-";
                            String age = "-";
                            String weight = h.getWeight() == null ? "-" : weightFormat.format(h.getWeight());

                            if (h.getGender() != null) {
                                switch (h.getGender()) {
                                    case MALE:
                                        gender = "Uros";
                                        break;
                                    case FEMALE:
                                        gender = "Naaras";
                                        break;
                                    case UNKNOWN:
                                        gender = "Tuntematon";
                                        break;
                                }
                            }

                            if (h.getAge() != null) {
                                switch (h.getAge()) {
                                    case ADULT:
                                        age = "Aikuinen";
                                        break;
                                    case YOUNG:
                                        age = "Alle 1 v";
                                        break;
                                    case UNKNOWN:
                                        age = "Tuntematon";
                                        break;
                                }
                            }

                            return Arrays.asList(gender, age, weight);
                        })
                        .flatMap(List::stream)
                        .collect(Collectors.toList());

        assertThat(body, Matchers.stringContainsInOrder(words));
    }

    private static void setupAuthorData(HarvestReport harvestReport) {
        Person author = harvestReport.getAuthor();
        author.setFirstName("Etunimi");
        author.setLastName("Sukunimi");
        author.setEmail("Etunimi.Sukunimi@invalid");
        author.setPhoneNumber("0400 112233");
        Address address = new Address();
        address.setStreetAddress("kotikatu 123");
        address.setCity("Kotikaupunki");
        address.setCountry("Suomi");
        author.setMrAddress(address);
    }

    private Harvest createHarvestAndSpecimen(String name, int amount) {
        GameSpecies species = createGameSpecies(name);

        Harvest harvest = model().newHarvest(species);
        harvest.setAmount(amount);
        for (int i = 0; i < amount; i++) {
            HarvestSpecimen sp = model().newHarvestSpecimen(harvest);
            if (i % 2 == 0) {
                sp.setGender(null);
                sp.setAge(null);
            } else if (i % 3 == 0) {
                sp.setWeight(null);
            }
        }
        return harvest;
    }

    private Harvest createHarvestAndSpecimen(GameSpecies species, int amount, GameGender gender, GameAge age) {
        Harvest harvest = model().newHarvest(species);
        harvest.setAmount(amount);
        for (int i = 0; i < amount; i++) {
            HarvestSpecimen sp = model().newHarvestSpecimen(harvest);
            sp.setGender(gender);
            sp.setAge(age);
        }
        return harvest;
    }

    private GameSpecies createGameSpecies(String name) {
        GameSpecies g = model().newGameSpecies();
        g.setNameFinnish(name);
        return g;
    }
}
