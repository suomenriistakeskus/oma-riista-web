package fi.riista.api.mobile;

import fi.riista.feature.gamediary.mobile.MobileGameDiaryFeature;
import fi.riista.feature.gamediary.mobile.MobileHarvestDTO;
import fi.riista.feature.gamediary.mobile.MobileObservationDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class MobileGameDiaryV2ApiResourceTest {

    @Mock
    private MobileGameDiaryFeature feature;

    @InjectMocks
    MobileGameDiaryV2ApiResource api;

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
        verify(feature).createHarvest(captor.capture(), ArgumentMatchers.eq(2));
        assertNull(captor.getValue().getId());
        assertNull(captor.getValue().getRev());
    }

    @Test
    public void testCreateObservationWithNullId() {
        testCreateObservation(null);
    }

    @Test
    public void testCreateObservationWithNonNullId() {
        testCreateObservation(100L);
    }

    private void testCreateObservation(Long id) {
        MobileObservationDTO dto = new MobileObservationDTO();
        dto.setId(id);

        api.createObservation(dto);

        ArgumentCaptor<MobileObservationDTO> captor = ArgumentCaptor.forClass(MobileObservationDTO.class);
        verify(feature).createObservation(captor.capture());
        assertNull(captor.getValue().getId());
    }

    @Test
    public void deleteHarvest_ExceptionsAreCaught() {
        doThrow(new RuntimeException("Whatever reason for delete to fail")).when(feature).deleteHarvest(anyLong());
        api.deleteHarvest(1L);
    }

    @Test
    public void deleteObservation_ExceptionsAreCaught() {
        doThrow(new RuntimeException("Whatever reason for delete to fail")).when(feature).deleteObservation(anyLong());
        api.deleteObservation(1L);
    }
}
