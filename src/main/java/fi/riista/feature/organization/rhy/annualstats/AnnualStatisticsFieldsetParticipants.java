package fi.riista.feature.organization.rhy.annualstats;

import io.vavr.Tuple2;

import java.util.List;

public interface AnnualStatisticsFieldsetParticipants {
    Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> listMissingParticipants();

}
