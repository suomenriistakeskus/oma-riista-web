package com.nsftele.tempo.api;

import com.nsftele.tempo.model.BaseCompany;
import com.nsftele.tempo.model.Company;
import com.nsftele.tempo.model.NewResource;
import com.nsftele.tempo.model.Numbers;
import com.nsftele.tempo.model.ShortNumbers;
import feign.Headers;
import feign.Param;
import feign.RequestLine;


public interface CompanyApi {


  /**
   * Add company
   * Company is a logical unit within project. Short numbers, IVRs, etc. belong to one and only one company.
   * @param projectId the id of the project that this operation affects (required)
   * @param companyDetails configuratin details for a company (required)
   * @return NewResource
   */
  @RequestLine("POST /company.json/{projectId}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
  })
  NewResource addCompany(@Param("projectId") String projectId, BaseCompany companyDetails);

  /**
   * Delete company
   * Removes a company. Short numbers belonging to the removed company are also removed.
   * @param companyId  (required)
   * @return void
   */
  @RequestLine("DELETE /company.json/{companyId}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
  })
  void deleteCompany(@Param("companyId") String companyId);

  /**
   * Get company details
   * Reads configuration details of a company.
   * @param companyId  (required)
   * @return Company
   */
  @RequestLine("GET /company.json/{companyId}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
  })
  Company getCompany(@Param("companyId") String companyId);

  /**
   * List company numbers
   * Lists all numbers belonging to particular company.
   * @param companyId  (required)
   * @return Numbers
   */
  @RequestLine("GET /company.json/{companyId}/numbers")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
  })
  Numbers getCompanyNumbers(@Param("companyId") String companyId);

  /**
   * List company short numbers
   * Lists all short numbers belonging to particular company.
   * @param companyId  (required)
   * @return ShortNumbers
   */
  @RequestLine("GET /company.json/{companyId}/shortNumbers")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
  })
  ShortNumbers getCompanyShortNumbers(@Param("companyId") String companyId);

  /**
   * Update company details
   * Updates the configuration details of a company.
   * @param companyId  (required)
   * @param companyDetails configuratin details for a company (required)
   * @return void
   */
  @RequestLine("PUT /company.json/{companyId}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
  })
  void updateCompany(@Param("companyId") String companyId, BaseCompany companyDetails);
}
