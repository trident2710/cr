/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inria.crawlerv2.engine;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import inria.crawlerv2.engine.account.Account;
import inria.crawlerv2.engine.account.AccountManager;
import inria.crawlerv2.provider.AttributeName;
import inria.crawlerv2.provider.AttributeProvider;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author adychka
 */
public class CrawlingCallable extends CrawlingInstance implements Callable<JsonObject>{



    private static final Logger LOG = Logger.getLogger(CrawlingRunable.class.getName());

    public CrawlingCallable(AccountManager accountManager, CrawlingInstanceSettings settings, URI target) {
        super(accountManager, settings, target);
    }

    public CrawlingCallable(AccountManager accountManager, CrawlingInstanceSettings settings, URI target, Account singleUsingAccount) {
        super(accountManager, settings, target,singleUsingAccount);
    }

    public CrawlingCallable(CrawlingInstanceSettings settings, URI target, Account singleUsingAccount) {
        super(null, settings, target, singleUsingAccount);
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

    @Override
    protected void finish() {
        if (object == null) {
            LOG.log(Level.SEVERE, "impossible to collect data");
        }
        fapi.finishSession();
    }

}
