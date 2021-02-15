package fi.riista.feature.common.decision.nomination;

import fi.riista.util.LocalisedString;

import javax.annotation.Nonnull;

import static fi.riista.feature.common.decision.nomination.NominationDecision.NominationDecisionType;
import static java.util.Objects.requireNonNull;

public class NominationDecisionName {
    public static final LocalisedString NOMINATION = LocalisedString.of(
            "RHY:N ERÄIDEN TOIMIHENKILÖIDEN NIMITTÄMINEN",
            "UTNÄMNING TILL FUNKTIONÄRER");
    public static final LocalisedString NOMINATION_CANCELLATION = LocalisedString.of(
            "RHY:N TOIMIHENKILÖN NIMITTÄMISEN PERUUTTAMINEN",
            "ANNULLERING AV UTNÄMNING AV JVF:S FUNKTIONÄR");

    @Nonnull
    public static LocalisedString getDecisionName(final @Nonnull NominationDecisionType decisionType) {
        requireNonNull(decisionType);

        switch (decisionType) {

            case NOMINATION:
                return NOMINATION;
            case NOMINATION_CANCELLATION:
                return NOMINATION_CANCELLATION;
            default:
                throw new IllegalArgumentException("Unknown decision type");
        }
    }

    private NominationDecisionName() {
        throw new AssertionError();
    }
}
