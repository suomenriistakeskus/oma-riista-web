package fi.riista.feature.permit;

import fi.riista.feature.permit.PermitNumberUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PermitNumberUtilTest {

    @Test
    public void test() {
        assertEquals("2013-1-010-12345-7", PermitNumberUtil.createPermitNumber(
                2013, 1, 1012345
        ));
    }
}
