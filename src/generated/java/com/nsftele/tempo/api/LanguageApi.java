package com.nsftele.tempo.api;

import com.nsftele.tempo.model.Language;
import feign.Headers;
import feign.RequestLine;

import java.util.List;


public interface LanguageApi  {


  /**
   * List languages
   * Lists supported languages
   * @return List&lt;Language&gt;
   */
  @RequestLine("GET /language.json")
  @Headers({
    "Accept: application/json",
  })
  List<Language> getLanguages();
}
