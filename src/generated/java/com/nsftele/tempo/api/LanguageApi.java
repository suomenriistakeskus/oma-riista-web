package com.nsftele.tempo.api;

import com.nsftele.tempo.model.Languages;
import feign.Headers;
import feign.RequestLine;


public interface LanguageApi {


  /**
   * List languages
   * Lists supported languages
   * @return Languages
   */
  @RequestLine("GET /language.json")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
  })
  Languages getLanguages();
}
