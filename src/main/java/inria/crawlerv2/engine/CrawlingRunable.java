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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The class which connects the facebook crawler realisation with the main
 * program acts like adapter witch requests the attributes from the crawler
 * driver, collects the results and returns to the main flow.
 *
 * @author adychka
 */
public class CrawlingRunable extends CrawlingInstance implements Runnable {

    private FinishCallback fc;

    private static final Logger LOG = Logger.getLogger(CrawlingRunable.class.getName());

    public CrawlingRunable(AccountManager accountManager, CrawlingInstanceSettings settings, URI target, FinishCallback fc) {
        super(accountManager,settings,target);
        this.fc = fc;
    }

    public CrawlingRunable(AccountManager accountManager, CrawlingInstanceSettings settings, URI target, Account singleUsingAccount, FinishCallback fc) {
        this(accountManager, settings, target, fc);
        this.singleUsingAccount = singleUsingAccount;
    }

    public CrawlingRunable(CrawlingInstanceSettings settings, URI target, Account singleUsingAccount, FinishCallback fc) {
        this(null, settings, target, singleUsingAccount, fc);
    }

    @Override
    public void run() {
        boolean useDefault = false;

        if (singleUsingAccount != null) {
            if (login(singleUsingAccount)) {
                useDefault = true;
            } else {
                useDefault = false;
                LOG.log(Level.SEVERE, "unable to login with provided account");
                finish();
                return;
            }
        }

        if (!useDefault) {
            try {
                login();
            } catch (NoWorkingAccountsException e) {
                LOG.log(Level.SEVERE, "no working accounts left");
                finish();
                return;
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
        finish();
    }

    private void crawlBlock(AttributeName[] block) {
        int delay = random.nextInt(settings.getRequestDelay());
        LOG.log(Level.INFO, "sleeping for {0} milliseconds", delay);

        try {
            List<AttributeName> page_names = Arrays.asList(block);
            Collections.shuffle(page_names);
            for (AttributeName p : page_names) {
                LOG.log(Level.INFO, "crawling {0}:", p.getName());
                fapi.getAttributeAsync(p, callback);
            }

            Thread.sleep(delay);
        } catch (InterruptedException ex) {
        }
    }

    private final AttributeProvider.AttributeCallback callback = new AttributeProvider.AttributeCallback() {
        @Override
        public void onAttributeCollected(AttributeName name, JsonElement value) {
            LOG.log(Level.INFO, "collected attribute of type {0}: {1}", new String[]{name.toString(), value.toString()});
            object.add(name.getName(), value);
        }

        @Override
        public void onError(AttributeName name, String description) {
            LOG.log(Level.WARNING, "unable to get the attribute of type {0}: {1}", new String[]{name.toString(), description});
        }
    };

    @Override
    protected void finish() {
        if (object == null) {
            LOG.log(Level.SEVERE, "impossible to collect data");
        }
        fapi.finishSession();
        fc.onFinished(object);
    }
  
    /**
     * called when CrawlingEngine collected all information
     */
    public static interface FinishCallback {

        /**
         * called when CrawlingEngine collected all information
         *
         * @param object - object containing all collected attributes
         */
        void onFinished(JsonObject object);
    }
}
