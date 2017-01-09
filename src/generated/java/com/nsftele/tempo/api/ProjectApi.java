package com.nsftele.tempo.api;

import com.nsftele.tempo.model.BaseProject;
import com.nsftele.tempo.model.Companies;
import com.nsftele.tempo.model.NewResource;
import com.nsftele.tempo.model.Project;
import com.nsftele.tempo.model.Templates;
import feign.Headers;
import feign.Param;
import feign.RequestLine;


public interface ProjectApi {


  /**
   * Add project
   * Add project
   * @param projectDetails configuration details for a project (required)
   * @return NewResource
   */
  @RequestLine("POST /project.json")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
  })
  NewResource addProject(BaseProject projectDetails);

  /**
   * Delete project
   * Removes a project.
   * @param projectId the id of the project that this operation affects (required)
   * @return void
   */
  @RequestLine("DELETE /project.json/{projectId}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
  })
  void deleteProject(@Param("projectId") String projectId);

  /**
   * Get project details
   * Project can be considered as a top-level &#39;account&#39; on Tempo Platform. Username and password of the API access are the credentials of the project. All interfaces in this API create/read/update/delete resources that belong to the project. Removing the project removes all resources allocated by the project.
   * @param projectId the id of the project that this operation affects (required)
   * @return Project
   */
  @RequestLine("GET /project.json/{projectId}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
  })
  Project getProject(@Param("projectId") String projectId);

  /**
   * List companies
   * Lists companies in this project.
   * @param projectId the id of the project that this operation affects (required)
   * @return Companies
   */
  @RequestLine("GET /project.json/{projectId}/companies")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
  })
  Companies listCompanies(@Param("projectId") String projectId);

  /**
   * List templates
   * Lists all templates that are available in this project.
   * @return Templates
   */
  @RequestLine("GET /project.json/{projectId}/templates")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
  })
  Templates listTemplates();

  /**
   * Update project
   * Update project details
   * @param projectId the id of the project that this operation affects (required)
   * @param projectDetails configuration details for a project (required)
   * @return void
   */
  @RequestLine("PUT /project.json/{projectId}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
  })
  void updateProject(@Param("projectId") String projectId, BaseProject projectDetails);
}
