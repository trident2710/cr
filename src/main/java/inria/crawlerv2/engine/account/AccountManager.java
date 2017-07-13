/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inria.crawlerv2.engine.account;

import java.util.List;

/**
 * defines the methods for interaction with the list of facebook accounts
 *
 * @author adychka
 */
public interface AccountManager {

    List<Account> getAllAccounts();

    List<Account> getBannedAccounts();

    List<Account> getWorkingAccounts();

    Account getRandomWorkingAccount();

    Account addAccount(String login, String password);

    void removeAccount(Account account);

    void save();
}
