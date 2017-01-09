package com.nsftele.tempo.api;

import com.nsftele.tempo.model.BaseShortNumber;
import com.nsftele.tempo.model.ShortNumber;
import feign.Headers;
import feign.Param;
import feign.RequestLine;


public interface ShortNumberApi {


  /**
   * Add or update short number
   * Updates the configuration of an existing short number or configures a new short number if it doesn&#39;t exist.  Short numbers can be dialed like regular numbers but unlike regular numbers short numbers cannot be registered to a phone. As a consequence short numbers are always redirected to some regular number(s). A short number can be redirected to different  list of numbers depending on the date and time of the call. start_date, start_time, end_date, end_time, weekday_mask attributes  define when numbers in call list are applicable. If date and time rules of multiple RedirectTo objects match the first one in the list is used.\&quot; 
   * @param companyId  (required)
   * @param shortNumberDetails short number configuration details (required)
   * @return void
   */
  @RequestLine("PUT /shortNumber.json/{companyId}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
  })
  void addOrUpdateShortNumber(@Param("companyId") String companyId, BaseShortNumber shortNumberDetails);

  /**
   * Delete short number
   * Removes a short number and all associated call tracking rules.
   * @param companyId  (required)
   * @param shortNumber the short number that this operation affects (required)
   * @return void
   */
  @RequestLine("DELETE /shortNumber.json/{companyId}/{shortNumber}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
  })
  void deleteShortNumber(@Param("companyId") String companyId, @Param("shortNumber") String shortNumber);

  /**
   * Get short number details
   * Reads short number configuration details.
   * @param companyId  (required)
   * @param shortNumber the short number that this operation affects (required)
   * @return ShortNumber
   */
  @RequestLine("GET /shortNumber.json/{companyId}/{shortNumber}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
  })
  ShortNumber getShortNumber(@Param("companyId") String companyId, @Param("shortNumber") String shortNumber);
}
