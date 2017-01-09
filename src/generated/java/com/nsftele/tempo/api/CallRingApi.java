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
   * @param companyId  (required)
   * @param ringNumber the number of call ring that this operation affects (required)
   * @return void
   */
  @RequestLine("DELETE /call_ring.json/{companyId}/{ringNumber}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
  })
  void deleteCallRing(@Param("companyId") String companyId, @Param("ringNumber") String ringNumber);

  /**
   * Get call ring details
   * 
   * @param companyId  (required)
   * @param ringNumber the number of call ring that this operation affects (required)
   * @return CallRing
   */
  @RequestLine("GET /call_ring.json/{companyId}/{ringNumber}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
  })
  CallRing getCallRing(@Param("companyId") String companyId, @Param("ringNumber") String ringNumber);

  /**
   * Add or update call ring
   * 
   * @param companyId  (required)
   * @param callRingDetails call ring configuration details (required)
   * @return void
   */
  @RequestLine("PUT /call_ring.json/{companyId}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
  })
  void putCallRing(@Param("companyId") String companyId, BaseCallRing callRingDetails);
}
