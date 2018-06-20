package fi.riista.integration.srva.callring;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.nsftele.tempo.api.CallRingApi;
import com.nsftele.tempo.api.CallTrackingApi;
import com.nsftele.tempo.api.LanguageApi;
import com.nsftele.tempo.model.BaseCallRing;
import com.nsftele.tempo.model.BasicRedirection;
import com.nsftele.tempo.model.CallRingSetup;
import com.nsftele.tempo.model.CallTrackingRule;
import com.nsftele.tempo.model.CallTrackingRules;
import com.nsftele.tempo.model.EmailCallTrackingReport;
import com.nsftele.tempo.model.Language;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TempoApiAdapter {
    private static final String COMPANY_ID = "19";

    // Hard-coded parameters for notification template added manually
    private static final int EMAIL_MESSAGE_TEMPLATE_ID = 15;
    private static final int EMAIL_SUBJECT_TEMPLATE_ID = 13;

    // Maximum timeout for unanswered call before proceeding to next member in call ring
    private static final int RINGING_TIMEOUT_SEC = 30;

    @Resource
    private CallRingApi callRingApi;

    @Resource
    private CallTrackingApi callTrackingApi;

    @Resource
    private LanguageApi languageApi;

    private Supplier<Map<String, Integer>> LANGUAGE_CODE_CACHE = Suppliers.memoize(() -> {
        final List<Language> languages = languageApi.getLanguages();

        if (languages == null || languages.isEmpty()) {
            throw new IllegalStateException("Could not retrieve language codes");
        }

        return languages.stream().collect(Collectors.toMap(Language::getLangCode, Language::getId));
    });

    public Map<String, Integer> getLanguageCodeMap() {
        return LANGUAGE_CODE_CACHE.get();
    }

    public void configureCallRing(final SrvaCallRingConfiguration rhyConfiguration,
                                  final int finnishLanguageId) {


        final BasicRedirection basicRedirection = new BasicRedirection()
                .ringingTimeout(RINGING_TIMEOUT_SEC)
                .languageId(finnishLanguageId)
                .answerPreferences(2)
                .call(rhyConfiguration.getFormattedPhoneNumbers());

        final BaseCallRing callRing = new BaseCallRing()
                .number(rhyConfiguration.getRhyOfficialCode())
                .setup(new CallRingSetup()._default(basicRedirection));

        callRingApi.putCallRing(COMPANY_ID, callRing);
    }

    public void configureCallTrackingRules(final SrvaCallRingConfiguration rhyConfiguration) {
        final CallTrackingRules callTrackingRules = rhyConfiguration.getNotificationEmails().stream().map(email -> {
            final EmailCallTrackingReport callTrackingReport = new EmailCallTrackingReport()
                    .subjectTemplateId(EMAIL_SUBJECT_TEMPLATE_ID)
                    .messageTemplateId(EMAIL_MESSAGE_TEMPLATE_ID)
                    .email(email);

            callTrackingReport.setType(EmailCallTrackingReport.TypeEnum.EMAILCALLTRACKINGREPORT);

            return new CallTrackingRule()
                    .event(CallTrackingRule.EventEnum.NO_ANSWER)
                    .report(callTrackingReport);

        }).collect(Collectors.toCollection(CallTrackingRules::new));

        callTrackingApi.updateCallTrackingRules(callTrackingRules, COMPANY_ID, rhyConfiguration.getRhyOfficialCode());
    }
}
