package fi.riista.feature.organization.rhy.mobile;

import fi.riista.feature.organization.rhy.training.OccupationTraining;
import fi.riista.util.ListTransformer;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.springframework.stereotype.Component;

@Component
public class MobileOccupationTrainingDTOTransformer extends ListTransformer<OccupationTraining, MobileOccupationTrainingDTO> {

    @Nonnull
    @Override
    protected List<MobileOccupationTrainingDTO> transform(@Nonnull final List<OccupationTraining> list) {
        if (list.isEmpty()) {
            return Collections.emptyList();
        }

        return list.stream().map(training -> {
            return MobileOccupationTrainingDTO.create(training);
        }).collect(Collectors.toList());
    }
}
