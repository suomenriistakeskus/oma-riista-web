package fi.riista.api.mobile;

import fi.riista.feature.gamediary.mobile.srva.MobileSrvaCrudFeature;
import fi.riista.feature.gamediary.mobile.srva.MobileSrvaEventDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class MobileSrvaApiResourceTest {

    @Mock
    private MobileSrvaCrudFeature feature;

    @InjectMocks
    private MobileSrvaApiResource api;

    @Test
    public void testCreateSrvaWithNullIdAndRev() {
        testCreateSrva(null, null);
    }

    @Test
    public void testCreateSrvaWithNonNullIdAndRev() {
        testCreateSrva(100L, 200);
    }

    private void testCreateSrva(Long id, Integer rev) {
        MobileSrvaEventDTO dto = new MobileSrvaEventDTO();
        dto.setId(id);
        dto.setRev(rev);

        api.createSrvaEvent(dto);

        ArgumentCaptor<MobileSrvaEventDTO> captor = ArgumentCaptor.forClass(MobileSrvaEventDTO.class);
        verify(feature).createSrvaEvent(captor.capture());
        assertNull(captor.getValue().getId());
        assertNull(captor.getValue().getRev());
    }
}
