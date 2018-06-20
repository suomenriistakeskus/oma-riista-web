package fi.riista.integration.metsahallitus;

import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class MhPermitImportRunner {

    @Resource
    private MhPermitImportFeature importFeature;

    public Map<String, Set<String>> importMhPermits(final List<MhPermitImportDTO> permits) {
        final MhPermitImportFeature.Result result = Lists.partition(permits, 1024).stream()
                .map(batch -> importFeature.importPermits(batch))
                .reduce(MhPermitImportFeature.Result.empty(), (a, b) -> a.merge(b));

        importFeature.deleteExcept(result.ids);

        if (!result.errors.isEmpty()) {
            return result.errors;
        }
        return Collections.emptyMap();
    }
}
