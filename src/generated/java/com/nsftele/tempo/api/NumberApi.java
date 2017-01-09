package com.nsftele.tempo.api;

import com.nsftele.tempo.model.AddNumber;
import com.nsftele.tempo.model.NewResource;
import com.nsftele.tempo.model.Number;
import com.nsftele.tempo.model.UpdateNumber;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.List;


public interface NumberApi {


  /**
   * Add numbers
   * Numbers...
   * @param addNumbers object that describes list of numbers (required)
   * @param companyId  (required)
   * @return List<NewResource>
   */
  @RequestLine("POST /number.json/{companyId}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
  })
  List<NewResource> addNumbers(List<AddNumber> addNumbers, @Param("companyId") String companyId);

  /**
   * Delete number
   * Removes a number.
   * @param numberId  (required)
   * @return void
   */
  @RequestLine("DELETE /number.json/{numberId}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
  })
  void deleteNumber(@Param("numberId") String numberId);

  /**
   * Get number details
   * Reads number details.
   * @param numberId  (required)
   * @return Number
   */
  @RequestLine("GET /number.json/{numberId}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
  })
  Number getNumber(@Param("numberId") String numberId);

  /**
   * Update number details
   * Updates a number.
   * @param numberId  (required)
   * @param updateNumber object that describes a number (required)
   * @return void
   */
  @RequestLine("PUT /number.json/{numberId}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
  })
  void updateNumber(@Param("numberId") String numberId, UpdateNumber updateNumber);
}
