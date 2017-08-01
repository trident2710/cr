/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inria.crawlerv2.engine;

import com.google.gson.JsonObject;
import inria.crawlerv2.driver.FacebookPageInformationDriver;
import inria.crawlerv2.engine.account.Account;
import inria.crawlerv2.engine.account.AccountManager;
import inria.crawlerv2.provider.AttributeName;
import inria.crawlerv2.provider.FacebookAttributeProvider;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author adychka
 */
public abstract class CrawlingInstance {
     /**
     * for generating random time intervals for the scrapping
     */
    protected Random random;

    protected FacebookAttributeProvider fapi;

    private static final Logger LOG = Logger.getLogger(CrawlingRunable.class.getName());

    protected JsonObject object;

    protected AccountManager accountManager;

    protected CrawlingInstanceSettings settings;

    protected Account singleUsingAccount;
    
    protected URI target;
    
    public CrawlingInstance(AccountManager accountManager, CrawlingInstanceSettings settings, URI target) {
        this.target = target;
        random = new Random();
        FacebookPageInformationDriver fpid = new FacebookPageInformationDriver(
                target,
                settings.getWebDriverOption(),
                settings.getWaitForElemLoadSec(),
                settings.getShortWaitMillis(),
                settings.getMaxFriendsToDiscover());

        fapi = new FacebookAttributeProvider(target, fpid, settings.getMaxFriendsToCollect());
        this.object = new JsonObject();
        this.accountManager = accountManager;
        this.settings = settings;
    }

    public CrawlingInstance(AccountManager accountManager, CrawlingInstanceSettings settings, URI target, Account singleUsingAccount) {
        this(accountManager, settings, target);
        this.singleUsingAccount = singleUsingAccount;
    }

    public CrawlingInstance(CrawlingInstanceSettings settings, URI target, Account singleUsingAccount) {
        this(null, settings, target, singleUsingAccount);
    }
    
    protected class NoWorkingAccountsException extends Exception {
    };

    /**
     * certain attributes are located on the same pages
     *
     * @return the attribute groups (which are on the same page) to overcome
     * unnecessary waiting
     */
    protected List<AttributeName[]> getAttributesByPages() {
        List<AttributeName[]> list = new ArrayList<>();
        list.add(new AttributeName[]{AttributeName.ID});
        list.add(new AttributeName[]{AttributeName.FIRST_NAME, AttributeName.LAST_NAME});
        list.add(new AttributeName[]{AttributeName.FRIEND_IDS});
        list.add(new AttributeName[]{AttributeName.BIRTHDAY,AttributeName.GENDER,
            AttributeName.GENDER_INTERESTS, AttributeName.POLITICAL_VIEW, AttributeName.RELIGIOUS_VIEW, AttributeName.PHONES,
            AttributeName.EMAIL_ADDRESS, AttributeName.LANGUAGES, AttributeName.ADDRESS});
        list.add(new AttributeName[]{AttributeName.WORK_IDS, AttributeName.EDUCATION_IDS});
        return list;
    }
    
    protected void login() throws NoWorkingAccountsException {
        if (accountManager.getWorkingAccounts().isEmpty()) {
            throw new NoWorkingAccountsException();
        }

        Account acc = accountManager.getRandomWorkingAccount();
        if (!login(acc)) {
            login();
        }
    }

    protected boolean login(Account acc) {
        if (!fapi.loginWithCredentials(acc.getLogin(), acc.getPassword())) {
            LOG.log(Level.WARNING, "unable to login");
            acc.setIsBanned(true);
            if (accountManager != null) {
                accountManager.save();
            }
            return false;
        }
        return true;
    }
    
    protected abstract void finish();

    public URI getTarget() {
        return target;
    }
    
    
}
