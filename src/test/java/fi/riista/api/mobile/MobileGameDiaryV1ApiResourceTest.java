package fi.riista.api.mobile;

import fi.riista.feature.gamediary.mobile.MobileGameDiaryV1Feature;
import fi.riista.feature.gamediary.mobile.MobileHarvestDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class MobileGameDiaryV1ApiResourceTest {

    @Mock
    private MobileGameDiaryV1Feature feature;

    @InjectMocks
    private MobileGameDiaryV1ApiResource api;

    @Test
    public void testCreateHarvestWithNullIdAndRev() {
        testCreateHarvest(null, null);
    }

    @Test
    public void testCreateHarvestWithNonNullIdAndRev() {
        testCreateHarvest(100L, 200);
    }

    private void testCreateHarvest(Long id, Integer rev) {
        MobileHarvestDTO dto = new MobileHarvestDTO();
        dto.setId(id);
        dto.setRev(rev);

        api.createHarvest(dto);

        ArgumentCaptor<MobileHarvestDTO> captor = ArgumentCaptor.forClass(MobileHarvestDTO.class);
        verify(feature).createHarvest(captor.capture());
        assertNull(captor.getValue().getId());
        assertNull(captor.getValue().getRev());
    }

    @Test
    public void deleteHarvest_ExceptionsAreCaught() {
        doThrow(new RuntimeException("Whatever reason for delete to fail")).when(feature).deleteHarvest(anyLong());
        api.deleteHarvest(1L);
    }
}
