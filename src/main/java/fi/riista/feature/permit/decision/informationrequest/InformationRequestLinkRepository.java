package fi.riista.feature.permit.decision.informationrequest;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.otherwisedeceased.OtherwiseDeceased;
import fi.riista.feature.otherwisedeceased.OtherwiseDeceasedRepositoryCustom;
import org.joda.time.DateTime;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InformationRequestLinkRepository extends BaseRepository<InformationRequestLink, Long>, InformationRequestLinkRepositoryCustom {
}
