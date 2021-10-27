package fi.riista.feature.common.decision.nomination.excel;

import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.common.decision.nomination.NominationDecisionDTO;
import fi.riista.feature.common.decision.nomination.NominationDecisionDTOTransformer;
import fi.riista.feature.common.decision.nomination.NominationDecisionRepository;
import fi.riista.feature.common.decision.nomination.NominationDecisionSearchDTO;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Locale;

@Service
public class NominationDecisionExcelFeature {

    @Resource
    private NominationDecisionRepository nominationDecisionRepository;

    @Resource
    private NominationDecisionDTOTransformer nominationDecisionDTOTransformer;

    @Resource
    private MessageSource messageSource;

    @Transactional(readOnly = true)
    public NominationDecisionExcelView export(final NominationDecisionSearchDTO dto, final Locale locale) {
        final List<NominationDecisionDTO> dtos =
                nominationDecisionDTOTransformer.apply(nominationDecisionRepository.search(dto));

        return new NominationDecisionExcelView(dtos, new EnumLocaliser(messageSource, locale));
    }
}
