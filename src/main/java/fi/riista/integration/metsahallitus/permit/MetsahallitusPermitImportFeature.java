package fi.riista.integration.metsahallitus.permit;

import fi.riista.util.F;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static fi.riista.util.Collect.indexingBy;

@Component
public class MetsahallitusPermitImportFeature {

    @Resource
    private MetsahallitusPermitRepository metsahallitusPermitRepository;

    @Transactional
    public Set<Long> importPermits(final List<MetsahallitusPermitImportDTO> dtoList,
                                   final MetsahallitusPermitErrorCollector errorCollector) {
        final MetsahallitusPermitParser permitParser = new MetsahallitusPermitParser(errorCollector);
        final Set<String> permitIdentifiers = F.mapNonNullsToSet(dtoList, MetsahallitusPermitImportDTO::getLuvanTunnus);
        final Map<String, MetsahallitusPermit> existingPermitIndex = metsahallitusPermitRepository
                .findByPermitIdentifierIn(permitIdentifiers).stream()
                .collect(indexingBy(MetsahallitusPermit::getPermitIdentifier));

        final List<MetsahallitusPermit> savedOrModifierPermits = F.mapNonNullsToList(dtoList, dto -> {
            final MetsahallitusPermit permitEntity = existingPermitIndex
                    .computeIfAbsent(dto.getLuvanTunnus(), key -> new MetsahallitusPermit());

            if (permitParser.parse(permitEntity, dto)) {
                return permitEntity;
            }

            return null;
        });

        metsahallitusPermitRepository.save(savedOrModifierPermits);
        metsahallitusPermitRepository.flush();

        return F.getUniqueIds(savedOrModifierPermits);
    }
}
