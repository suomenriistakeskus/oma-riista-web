package fi.riista.feature.common;

import fi.riista.api.municipality.MunicipalityDTO;
import fi.riista.feature.common.entity.Municipality;
import fi.riista.feature.common.repository.MunicipalityRepository;
import fi.riista.util.F;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Component
public class MunicipalityFeature {

    @Resource
    private MunicipalityRepository municipalityRepository;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    @Transactional(readOnly = true)
    public List<MunicipalityDTO> listMunicipalities() {
        final List<Municipality> municipalities = municipalityRepository.findAll();
        return F.mapNonNullsToList(municipalities, MunicipalityDTO::from);
    }
}
