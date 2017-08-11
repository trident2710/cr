/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inria.crawlerv2.engine;

import com.google.gson.JsonObject;
import inria.crawlerv2.driver.FacebookPageInformationDriver;
import inria.crawlerv2.driver.FacebookSelfPageInformationDriver;
import inria.crawlerv2.driver.WebDriverOption;
import inria.crawlerv2.engine.account.Account;
import inria.crawlerv2.provider.AttributeName;
import inria.crawlerv2.provider.AttributeProvider;
import inria.crawlerv2.provider.FacebookAttributeVisibilityProvider;
import java.net.URI;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author adychka
 */
public class AttributeVisibilityCrawlerCallable implements Callable<JsonObject>{
    
    private final Account account;
    private final AttributeProvider ap;
    private JsonObject object;
    private CrawlingInstanceSettings settings;
    private FacebookSelfPageInformationDriver fpid;
    
    private static final Logger LOG = Logger.getLogger(AttributeVisibilityCrawlerCallable.class.getName());
    
    public AttributeVisibilityCrawlerCallable(Account account,URI target,CrawlingInstanceSettings settings){
        this.account = account;
        this.settings = settings;
        this.fpid = new FacebookSelfPageInformationDriver(target, settings.getWebDriverOption(), settings.getWaitForElemLoadSec(), settings.getShortWaitMillis(), settings.getMaxFriendsToCollect());
        this.ap = new FacebookAttributeVisibilityProvider(target,fpid);
    }

    @Override
    public JsonObject call(){
        try {
            object = new JsonObject();
            if(account==null)
                throw new Exception("account can't be null");
            if(!ap.loginWithCredentials(account.getLogin(), account.getPassword())){
                LOG.log(Level.WARNING,"unabe to login with provided account");
                return null;
            }

            for(AttributeName name:AttributeName.values()){
                try {
                    object.add(name.getName(), ap.getAttribute(name));
                } catch (Exception e) {
                    LOG.log(Level.WARNING,"unable to get for attribute {0}",name);
                }
                
            }
            return object;
        } catch (Exception e) {
            LOG.log(Level.WARNING,"exception raised while getting attribute visibility",e);
            return object;
        } finally{
            ap.finishSession();
        }
        
    }
    
}
