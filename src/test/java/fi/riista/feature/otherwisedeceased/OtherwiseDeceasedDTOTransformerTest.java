package fi.riista.feature.otherwisedeceased;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class OtherwiseDeceasedDTOTransformerTest extends EmbeddedDatabaseTest {

    private static int MAX_QUERY_COUNT = 8;

    @Resource
    private OtherwiseDeceasedDTOTransformer transformer;

    @Test
    public void numberOfDatabaseQueriesIsConstant() {
        final List<OtherwiseDeceased> entities = new ArrayList<>();
        IntStream.range(0, 100).forEach(i -> entities.add(generateAndPersistNewEntity()));
        assertMaxQueryCount(MAX_QUERY_COUNT, () -> {
            transformer.transform(entities);
        });
    }

    private OtherwiseDeceased generateAndPersistNewEntity() {
        final EntitySupplier es = getEntitySupplier();
        final OtherwiseDeceased entity = es.newOtherwiseDeceased(DateUtil.now());
        final SystemUser user = createNewUser();
        persistInNewTransaction();
        IntStream.range(0, 10).forEach(i -> es.newOtherwiseDeceasedChange(entity, user));
        IntStream.range(0, 10).forEach(i -> es.newOtherwiseDeceasedAttachment(entity));
        persistInNewTransaction();
        return entity;
    }
}
