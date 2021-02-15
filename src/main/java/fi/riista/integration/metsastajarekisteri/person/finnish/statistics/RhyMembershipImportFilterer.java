package fi.riista.integration.metsastajarekisteri.person.finnish.statistics;

import fi.riista.integration.metsastajarekisteri.InnofactorImportFileLine;
import org.springframework.batch.item.ItemProcessor;

import static fi.riista.integration.metsastajarekisteri.person.finnish.statistics.RhyMembershipImportMode.Phase.NOT_APPLICABLE;
import static fi.riista.util.DateUtil.now;

public class RhyMembershipImportFilterer implements ItemProcessor<InnofactorImportFileLine,
        InnofactorImportFileLine> {

    @Override
    public InnofactorImportFileLine process(final InnofactorImportFileLine innofactorImportFileLine) throws Exception {
        if (RhyMembershipImportMode.getPhase(now()) == NOT_APPLICABLE) {
            return null;
        }
        return innofactorImportFileLine;
    }
}
