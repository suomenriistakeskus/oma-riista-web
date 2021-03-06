/*
 * Tempo Net API
 * No description provided (generated by Swagger Codegen https://github.com/swagger-api/swagger-codegen)
 *
 * OpenAPI spec version: 1
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package com.nsftele.tempo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * BasicRedirection
 */

public class BasicRedirection {
  @JsonProperty("call")
  private List<String> call = new ArrayList<>();

  @JsonProperty("ringing_timeout")
  private Integer ringingTimeout = null;

  @JsonProperty("answer_preferences")
  private Integer answerPreferences = null;

  @JsonProperty("language_id")
  private Integer languageId = null;

  @JsonProperty("dtmf_instructions_enabled")
  private Boolean dtmfInstructionsEnabled = null;

  @JsonProperty("dtmf_incorrect_enabled")
  private Boolean dtmfIncorrectEnabled = null;

  @JsonProperty("dtmf_accepted_enabled")
  private Boolean dtmfAcceptedEnabled = null;

  public BasicRedirection call(List<String> call) {
    this.call = call;
    return this;
  }

  public BasicRedirection addCallItem(String callItem) {
    this.call.add(callItem);
    return this;
  }

   /**
   * phone numbers that will be called. numbers are tried for answer in order from first to last. hunting stops once some number answers or the end of the list is reached.
   * @return call
  **/
  @ApiModelProperty(required = true, value = "phone numbers that will be called. numbers are tried for answer in order from first to last. hunting stops once some number answers or the end of the list is reached.")
  public List<String> getCall() {
    return call;
  }

  public void setCall(List<String> call) {
    this.call = call;
  }

  public BasicRedirection ringingTimeout(Integer ringingTimeout) {
    this.ringingTimeout = ringingTimeout;
    return this;
  }

   /**
   * number of seconds to let it ring without answer before trying next number in call list
   * @return ringingTimeout
  **/
  @ApiModelProperty(required = true, value = "number of seconds to let it ring without answer before trying next number in call list")
  public Integer getRingingTimeout() {
    return ringingTimeout;
  }

  public void setRingingTimeout(Integer ringingTimeout) {
    this.ringingTimeout = ringingTimeout;
  }

  public BasicRedirection answerPreferences(Integer answerPreferences) {
    this.answerPreferences = answerPreferences;
    return this;
  }

   /**
   * &#39;1&#39; means &#39;do not confirm call answer with DTMF&#39;, &#39;0&#39; means &#39;use recipients answer preferences&#39;, &#39;2&#39; means &#39;confirm answer with DTMF&#39;
   * @return answerPreferences
  **/
  @ApiModelProperty(required = true, value = "'1' means 'do not confirm call answer with DTMF', '0' means 'use recipients answer preferences', '2' means 'confirm answer with DTMF'")
  public Integer getAnswerPreferences() {
    return answerPreferences;
  }

  public void setAnswerPreferences(Integer answerPreferences) {
    this.answerPreferences = answerPreferences;
  }

  public BasicRedirection languageId(Integer languageId) {
    this.languageId = languageId;
    return this;
  }

   /**
   * call ring language
   * @return languageId
  **/
  @ApiModelProperty(required = true, value = "call ring language")
  public Integer getLanguageId() {
    return languageId;
  }

  public void setLanguageId(Integer languageId) {
    this.languageId = languageId;
  }

  public BasicRedirection dtmfInstructionsEnabled(Boolean dtmfInstructionsEnabled) {
    this.dtmfInstructionsEnabled = dtmfInstructionsEnabled;
    return this;
  }

   /**
   * is &#39;instructions&#39; announcement enabled
   * @return dtmfInstructionsEnabled
  **/
  @ApiModelProperty(value = "is 'instructions' announcement enabled")
  public Boolean isDtmfInstructionsEnabled() {
    return dtmfInstructionsEnabled;
  }

  public void setDtmfInstructionsEnabled(Boolean dtmfInstructionsEnabled) {
    this.dtmfInstructionsEnabled = dtmfInstructionsEnabled;
  }

  public BasicRedirection dtmfIncorrectEnabled(Boolean dtmfIncorrectEnabled) {
    this.dtmfIncorrectEnabled = dtmfIncorrectEnabled;
    return this;
  }

   /**
   * is &#39;incorrect selection&#39; announcement enabled
   * @return dtmfIncorrectEnabled
  **/
  @ApiModelProperty(value = "is 'incorrect selection' announcement enabled")
  public Boolean isDtmfIncorrectEnabled() {
    return dtmfIncorrectEnabled;
  }

  public void setDtmfIncorrectEnabled(Boolean dtmfIncorrectEnabled) {
    this.dtmfIncorrectEnabled = dtmfIncorrectEnabled;
  }

  public BasicRedirection dtmfAcceptedEnabled(Boolean dtmfAcceptedEnabled) {
    this.dtmfAcceptedEnabled = dtmfAcceptedEnabled;
    return this;
  }

   /**
   * is &#39;selection accepted&#39; announcement enabled
   * @return dtmfAcceptedEnabled
  **/
  @ApiModelProperty(value = "is 'selection accepted' announcement enabled")
  public Boolean isDtmfAcceptedEnabled() {
    return dtmfAcceptedEnabled;
  }

  public void setDtmfAcceptedEnabled(Boolean dtmfAcceptedEnabled) {
    this.dtmfAcceptedEnabled = dtmfAcceptedEnabled;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BasicRedirection basicRedirection = (BasicRedirection) o;
    return Objects.equals(this.call, basicRedirection.call) &&
        Objects.equals(this.ringingTimeout, basicRedirection.ringingTimeout) &&
        Objects.equals(this.answerPreferences, basicRedirection.answerPreferences) &&
        Objects.equals(this.languageId, basicRedirection.languageId) &&
        Objects.equals(this.dtmfInstructionsEnabled, basicRedirection.dtmfInstructionsEnabled) &&
        Objects.equals(this.dtmfIncorrectEnabled, basicRedirection.dtmfIncorrectEnabled) &&
        Objects.equals(this.dtmfAcceptedEnabled, basicRedirection.dtmfAcceptedEnabled);
  }

  @Override
  public int hashCode() {
    return Objects.hash(call, ringingTimeout, answerPreferences, languageId, dtmfInstructionsEnabled, dtmfIncorrectEnabled, dtmfAcceptedEnabled);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BasicRedirection {\n");
    
    sb.append("    call: ").append(toIndentedString(call)).append("\n");
    sb.append("    ringingTimeout: ").append(toIndentedString(ringingTimeout)).append("\n");
    sb.append("    answerPreferences: ").append(toIndentedString(answerPreferences)).append("\n");
    sb.append("    languageId: ").append(toIndentedString(languageId)).append("\n");
    sb.append("    dtmfInstructionsEnabled: ").append(toIndentedString(dtmfInstructionsEnabled)).append("\n");
    sb.append("    dtmfIncorrectEnabled: ").append(toIndentedString(dtmfIncorrectEnabled)).append("\n");
    sb.append("    dtmfAcceptedEnabled: ").append(toIndentedString(dtmfAcceptedEnabled)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}

