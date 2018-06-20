package com.nsftele.tempo.api;

import com.nsftele.tempo.model.BaseCallRing;
import com.nsftele.tempo.model.CallRing;
import feign.Headers;
import feign.Param;
import feign.RequestLine;


public interface CallRingApi {


  /**
   * Delete call ring
   * 
    * @param companyId the id of the company that this operation affects (required)
    * @param ringNumber the number of call ring that this operation affects (required)
   */
  @RequestLine("DELETE /call_ring.json/{companyId}/{ringNumber}")
  @Headers({
    "Accept: application/json",
  })
  void deleteCallRing(@Param("companyId") String companyId, @Param("ringNumber") String ringNumber);

  /**
   * Get call ring details
   * 
    * @param companyId the id of the company that this operation affects (required)
    * @param ringNumber the number of call ring that this operation affects (required)
   * @return CallRing
   */
  @RequestLine("GET /call_ring.json/{companyId}/{ringNumber}")
  @Headers({
    "Accept: application/json",
  })
  CallRing getCallRing(@Param("companyId") String companyId, @Param("ringNumber") String ringNumber);

  /**
   * Add or update call ring
   * 
    * @param companyId the id of the company that this operation affects (required)
    * @param callRingDetails call ring configuration details (required)
   */
  @RequestLine("PUT /call_ring.json/{companyId}")
  @Headers({
    "Content-Type: application/json",
    "Accept: application/json",
  })
  void putCallRing(@Param("companyId") String companyId, BaseCallRing callRingDetails);
}
