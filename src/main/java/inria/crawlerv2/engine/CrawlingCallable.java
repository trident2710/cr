/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inria.crawlerv2.engine;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import inria.crawlerv2.driver.FacebookPageInformationDriver;
import inria.crawlerv2.engine.account.Account;
import inria.crawlerv2.engine.account.AccountManager;
import inria.crawlerv2.provider.AttributeName;
import inria.crawlerv2.provider.AttributeProvider;
import inria.crawlerv2.provider.FacebookAttributeProvider;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author adychka
 */
public class CrawlingCallable implements Callable<JsonObject>{

    /**
     * for generating random time intervals for the scrapping
     */
    private Random random;

    private FacebookAttributeProvider fapi;

    private static final Logger LOG = Logger.getLogger(CrawlingEngine.class.getName());

    private JsonObject object;

    private AccountManager accountManager;

    private CrawlingEngineSettings settings;

    private Account singleUsingAccount;

    public CrawlingCallable(AccountManager accountManager, CrawlingEngineSettings settings, URI target) {
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

    public CrawlingCallable(AccountManager accountManager, CrawlingEngineSettings settings, URI target, Account singleUsingAccount) {
        this(accountManager, settings, target);
        this.singleUsingAccount = singleUsingAccount;
    }

    public CrawlingCallable(CrawlingEngineSettings settings, URI target, Account singleUsingAccount) {
        this(null, settings, target, singleUsingAccount);
    }

    @Override
    public JsonObject call() {
        try {
            boolean useDefault = false;

            if (singleUsingAccount != null) {
                if (login(singleUsingAccount)) {
                    useDefault = true;
                } else {
                    useDefault = false;
                    LOG.log(Level.SEVERE, "unable to login with provided account");
                    return null;
                }
            }

            if (!useDefault) {
                try {
                    login();
                } catch (NoWorkingAccountsException e) {
                    LOG.log(Level.SEVERE, "no working accounts left");
                    return null;
                }
            }

            if (settings.getDelayBeforeRunInMillis() != 0) {
                LOG.log(Level.INFO, "explicitly waiting {0} millis");
                try {
                    Thread.sleep(settings.getDelayBeforeRunInMillis());
                } catch (InterruptedException ex) {
                }
            }

            if (!Arrays.equals(settings.getAttributes(), AttributeName.values())) {
                crawlBlock(settings.getAttributes());
            } else {
                List<AttributeName[]> names = getAttributesByPages();
                Collections.shuffle(names);
                for (AttributeName[] block : names) {
                    crawlBlock(block);
                }
            }
            
            return object;
        } finally {
            finish();
        }
        
    }

    private void crawlBlock(AttributeName[] block) {
        int delay = random.nextInt(settings.getRequestDelay());
        LOG.log(Level.INFO, "sleeping for {0} milliseconds", delay);

        try {
            List<AttributeName> page_names = Arrays.asList(block);
            Collections.shuffle(page_names);
            for (AttributeName p : page_names) {
                LOG.log(Level.INFO, "crawling {0}:", p.getName());
                
                try {
                    JsonElement value;
                    value = fapi.getAttribute(p);
                    object.add(p.getName(), value);
                    LOG.log(Level.INFO, "collected attribute of type {0}: {1}", new String[]{p.toString(), value.toString()});
                } catch (AttributeProvider.CollectException ex) {
                    LOG.log(Level.WARNING, "unable to get the attribute of type {0}: {1}", new String[]{p.toString(), ex.getMessage()});
                }     
            }
            Thread.sleep(delay);
        } catch (InterruptedException ex) {
        }
    }


    private void login() throws NoWorkingAccountsException {
        if (accountManager.getWorkingAccounts().isEmpty()) {
            throw new NoWorkingAccountsException();
        }

        Account acc = accountManager.getRandomWorkingAccount();
        if (!login(acc)) {
            login();
        }
    }

    private boolean login(Account acc) {
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

    private void finish() {
        if (object == null) {
            LOG.log(Level.SEVERE, "impossible to collect data");
        }
        fapi.finishSession();
    }

    private class NoWorkingAccountsException extends Exception {
    };

    /**
     * certain attributes are located on the same pages
     *
     * @return the attribute groups (which are on the same page) to overcome
     * unnecessary waiting
     */
    private List<AttributeName[]> getAttributesByPages() {
        List<AttributeName[]> list = new ArrayList<>();
        list.add(new AttributeName[]{AttributeName.ID});
        list.add(new AttributeName[]{AttributeName.FIRST_NAME, AttributeName.LAST_NAME});
        list.add(new AttributeName[]{AttributeName.FRIEND_IDS});
        list.add(new AttributeName[]{AttributeName.BIRTH_DATE, AttributeName.BIRTH_YEAR, AttributeName.GENDER,
            AttributeName.GENDER_INTERESTS, AttributeName.POLITICAL_VIEW, AttributeName.RELIGIOUS_VIEW, AttributeName.PHONES,
            AttributeName.EMAIL_ADDRESS, AttributeName.LANGUAGES, AttributeName.ADDRESS});
        list.add(new AttributeName[]{AttributeName.WORK_IDS, AttributeName.EDUCATION_IDS});
        return list;
    }

}
