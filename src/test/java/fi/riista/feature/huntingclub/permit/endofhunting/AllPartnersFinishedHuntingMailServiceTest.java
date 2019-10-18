package fi.riista.feature.huntingclub.permit.endofhunting;

import com.google.common.collect.Sets;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.Locales;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.net.URI;
import java.util.Locale;

public class AllPartnersFinishedHuntingMailServiceTest extends EmbeddedDatabaseTest {

    @Resource
    private AllPartnersFinishedHuntingMailService mailService;

    @Test
    public void testSmokeFinnish() {
        final AllPartnersFinishedHuntingDTO data = createModel(Locales.FI);

        mailService.sendEmailInternal(Sets.newHashSet("test@invalid"), data);
    }

    @Test
    public void testSmokeSwedish() {
        final AllPartnersFinishedHuntingDTO data = createModel(Locales.SV);

        mailService.sendEmailInternal(Sets.newHashSet("test@invalid"), data);
    }

    @Nonnull
    private AllPartnersFinishedHuntingDTO createModel(final Locale locale) {
        final GameSpecies gameSpecies = model().newGameSpeciesMoose();
        final HarvestPermit harvestPermit = model().newHarvestPermit();
        return AllPartnersFinishedHuntingDTO.create(harvestPermit, gameSpecies, URI.create("http://www.google.com"), locale);
    }

}
