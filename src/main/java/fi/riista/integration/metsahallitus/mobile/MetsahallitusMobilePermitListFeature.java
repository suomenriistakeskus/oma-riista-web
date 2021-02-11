package fi.riista.integration.metsahallitus.mobile;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.organization.person.Person;
import fi.riista.integration.metsahallitus.permit.MetsahallitusPermit;
import fi.riista.integration.metsahallitus.permit.MetsahallitusPermitImportDTO;
import fi.riista.integration.metsahallitus.permit.MetsahallitusPermitRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
public class MetsahallitusMobilePermitListFeature {

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private MetsahallitusPermitRepository metsahallitusPermitRepository;

    @Transactional(readOnly = true)
    public List<MetsahallitusMobilePermitDTO> listAll() {
        final Person person = activeUserService.requireActivePerson();
        final List<MetsahallitusPermit> list = metsahallitusPermitRepository.findByPerson(person);

        return list.stream()
                .filter(p -> MetsahallitusPermitImportDTO.PAID_CODES.contains(p.getStatus()))
                .map(MetsahallitusMobilePermitDTO::create)
                .collect(toList());
    }
}
