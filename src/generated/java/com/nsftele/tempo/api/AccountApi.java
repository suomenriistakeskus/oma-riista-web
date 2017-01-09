package com.nsftele.tempo.api;

import com.nsftele.tempo.model.Account;
import com.nsftele.tempo.model.Accounts;
import com.nsftele.tempo.model.AddAccount;
import com.nsftele.tempo.model.NewResource;
import com.nsftele.tempo.model.UpdateAccount;
import feign.Headers;
import feign.Param;
import feign.RequestLine;


public interface AccountApi {


  /**
   * Add account
   * Add account
   * @param projectId the id of the project that this operation affects (required)
   * @param addAccount configuration details for an account (required)
   * @return NewResource
   */
  @RequestLine("POST /account.json/{projectId}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
  })
  NewResource addAccount(@Param("projectId") String projectId, AddAccount addAccount);

  /**
   * Delete account
   * Removes an account.
   * @param accountId the id of the account that this operation affects (required)
   * @return void
   */
  @RequestLine("DELETE /account.json/{accountId}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
  })
  void deleteAccount(@Param("accountId") String accountId);

  /**
   * List accounts
   * List accounts
   * @return Accounts
   */
  @RequestLine("GET /account.json")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
  })
  Accounts listAccounts();

  /**
   * Show account
   * Show account
   * @param accountId the id of the account that this operation affects (required)
   * @return Account
   */
  @RequestLine("GET /account.json/{accountId}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
  })
  Account showAccount(@Param("accountId") String accountId);

  /**
   * Update account
   * Update account details
   * @param accountId the id of the account that this operation affects (required)
   * @param updateAccount configuration details for an account (required)
   * @return void
   */
  @RequestLine("PUT /account.json/{accountId}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
  })
  void updateAccount(@Param("accountId") String accountId, UpdateAccount updateAccount);
}
