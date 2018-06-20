package com.nsftele.tempo.api;

import com.nsftele.tempo.model.CallTrackingRules;
import feign.Headers;
import feign.Param;
import feign.RequestLine;


public interface CallTrackingApi  {


  /**
   * List call tracking rules
   * Lists short numbers&#39;s call tracking rules.
    * @param companyId the id of the company that this operation affects (required)
    * @param shortNumber the short number that this operation affects (required)
   * @return CallTrackingRules
   */
  @RequestLine("GET /call_tracking.json/{companyId}/{shortNumber}")
  @Headers({
    "Accept: application/json",
  })
  CallTrackingRules getCallTrackingRules(@Param("companyId") String companyId, @Param("shortNumber") String shortNumber);

  /**
   * Update call tracking rules
   * Registers call tracking rules for specified short number. Current implementation provides email reporting of calls that were answered/unanswered.
    * @param callTrackingRules  (required)
    * @param companyId the id of the company that this operation affects (required)
    * @param shortNumber the short number that this operation affects (required)
   */
  @RequestLine("PUT /call_tracking.json/{companyId}/{shortNumber}")
  @Headers({
    "Content-Type: application/json",
    "Accept: application/json",
  })
  void updateCallTrackingRules(CallTrackingRules callTrackingRules, @Param("companyId") String companyId, @Param("shortNumber") String shortNumber);
}
