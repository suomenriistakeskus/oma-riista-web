package fi.riista.feature.organization.jht.mobile;

import fi.riista.feature.organization.jht.training.JHTTraining;
import fi.riista.util.ListTransformer;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.springframework.stereotype.Component;


@Component
public class MobileJHTTrainingDTOTransformer extends ListTransformer<JHTTraining, MobileJHTTrainingDTO> {

    @Nonnull
    @Override
    protected List<MobileJHTTrainingDTO> transform(@Nonnull final List<JHTTraining> list) {
        if (list.isEmpty()) {
            return Collections.emptyList();
        }

        return list.stream().map(training -> {
            return MobileJHTTrainingDTO.create(training);
        }).collect(Collectors.toList());
    }
}
