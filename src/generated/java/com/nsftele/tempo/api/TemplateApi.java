package com.nsftele.tempo.api;

import com.nsftele.tempo.model.BaseTemplate;
import com.nsftele.tempo.model.NewResource;
import com.nsftele.tempo.model.Template;
import feign.Headers;
import feign.Param;
import feign.RequestLine;


public interface TemplateApi {


  /**
   * Add template
   * Templates are used within Tempo Platform to construct variable-dependant strings. Template format is [mustache](https://mustache.github.io/)
   * @param templateDetails object that describes a template (required)
   * @param projectId the id of the project that this operation affects (required)
   * @return NewResource
   */
  @RequestLine("POST /template.json/{projectId}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
  })
  NewResource addTemplate(BaseTemplate templateDetails, @Param("projectId") String projectId);

  /**
   * Delete template
   * Removes a template.
   * @param templateId the id of the template that this operation affects (required)
   * @return void
   */
  @RequestLine("DELETE /template.json/{templateId}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
  })
  void deleteTemplate(@Param("templateId") String templateId);

  /**
   * Get template details
   * Reads template details.
   * @param templateId the id of the template that this operation affects (required)
   * @return Template
   */
  @RequestLine("GET /template.json/{templateId}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
  })
  Template getTemplate(@Param("templateId") String templateId);

  /**
   * Update template details
   * Updates a template.
   * @param templateId the id of the template that this operation affects (required)
   * @param templateDetails object that describes a template (required)
   * @return void
   */
  @RequestLine("PUT /template.json/{templateId}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
  })
  void updateTemplate(@Param("templateId") String templateId, BaseTemplate templateDetails);
}
