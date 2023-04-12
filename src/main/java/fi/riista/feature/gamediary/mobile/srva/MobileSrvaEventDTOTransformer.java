package fi.riista.feature.gamediary.mobile.srva;

import fi.riista.feature.error.MessageExposableValidationException;
import fi.riista.feature.gamediary.srva.SrvaEvent;
import fi.riista.feature.gamediary.srva.SrvaEventSpecVersion;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class MobileSrvaEventDTOTransformer extends MobileSrvaEventDTOTransformerBase {

    @Resource
    private MobileSrvaEventDTOTransformerSpecV1 mobileSrvaEventDTOTransformerSpecV1;

    @Resource
    private MobileSrvaEventDTOTransformerSpecV2 mobileSrvaEventDTOTransformerSpecV2;

    @Override
    protected List<MobileSrvaEventDTO> transform(List<SrvaEvent> srvaEvents, SrvaEventSpecVersion srvaEventSpecVersion) {
        if (srvaEventSpecVersion.equals(SrvaEventSpecVersion._1)) {
            return mobileSrvaEventDTOTransformerSpecV1.transform(srvaEvents, srvaEventSpecVersion);
        } else if (srvaEventSpecVersion.equals(SrvaEventSpecVersion._2)) {
            return mobileSrvaEventDTOTransformerSpecV2.transform(srvaEvents, srvaEventSpecVersion);
        }
        throw new MessageExposableValidationException("Unsupported SrvaEventSpecVersion");
    }
}
