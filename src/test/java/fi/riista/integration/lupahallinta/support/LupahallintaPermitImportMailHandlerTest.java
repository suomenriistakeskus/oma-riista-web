package fi.riista.integration.lupahallinta.support;

import com.google.common.collect.Sets;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.integration.lupahallinta.HarvestPermitImportResultDTO;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class LupahallintaPermitImportMailHandlerTest extends EmbeddedDatabaseTest {

    @Resource
    private LupahallintaPermitImportMailHandler mailHandler;

    @Test
    public void testCollectRkaOfficialCodes() {
        assertEquals(Sets.newHashSet("200", "500"), LupahallintaPermitImportMailHandler.collectRkaOfficialCodes(Arrays.asList(
                err("2016-1-200-01915-0"),
                err("2016-1-500-00021-2"),
                err("2016-1-500-01080-4")
        )));
    }

    private static HarvestPermitImportResultDTO.PermitParsingError err(String s) {
        return new HarvestPermitImportResultDTO.PermitParsingError(0, s, null);
    }

    @Test
    public void testRkaEmails() {
        RiistakeskuksenAlue rka1 = model().newRiistakeskuksenAlue();
        RiistakeskuksenAlue rka2 = model().newRiistakeskuksenAlue();
        RiistakeskuksenAlue rka3 = model().newRiistakeskuksenAlue();
        assertNotNull(rka1.getEmail());

        persistInNewTransaction();
        runInTransaction(() ->
                assertEquals(Sets.newHashSet(rka1.getEmail(), rka3.getEmail()),
                        mailHandler.rkaEmails(Sets.newHashSet(rka1.getOfficialCode(), rka3.getOfficialCode())))
        );
    }
}
