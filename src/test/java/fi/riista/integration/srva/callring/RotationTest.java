package fi.riista.integration.srva.callring;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RotationTest {

    @Test
    public void testRotation() {

        final List<Integer> integers = Lists.newArrayList(1, 2, 3, 4, 5, 6, 7, 8);
        final List<Integer> rotatedTwice = Lists.newArrayList(7, 8, 1, 2, 3, 4, 5, 6);

        Collections.rotate(integers, 2);

        System.out.println(integers);
        assertEquals(rotatedTwice, integers);


    }
}
