/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inria.crawlerv2.engine;

import com.google.gson.JsonObject;
import inria.crawlerv2.provider.FacebookAttributeName;
import inria.crawlerv2.provider.FacebookAttributeProviderImpl;
import inria.crawlerv2.provider.FacebookProfileCrawlerCallback;
import inria.crawlerv2.utils.ArrayHelper;
import java.net.URI;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author adychka
 */
public class CrawlingEngine implements Runnable{
  
  /**
   * max delay between the actions
   */
  private static final Integer MAX_DELAY = 1000*60*1; //1 minutes
  
  /**
   * for generating random time intervals for the scrapping
   */
  private final Random random;
  
  private final FacebookAttributeProviderImpl fapi;
  
  private final FinishCallback fc;
  
  private static final Logger LOG = Logger.getLogger(CrawlingEngine.class.getName()); 
  
  private JsonObject object;
  
  public CrawlingEngine(URI url,FinishCallback fc){
    random = new Random();
    fapi = new FacebookAttributeProviderImpl(url);
    this.fc = fc;
    this.object = new JsonObject();
  }

  @Override
  public void run() {
    fapi.loginWithCredentials("btinr2017@yandex.ru", "gtheyjdfhfnm");
    for(FacebookAttributeName name:ArrayHelper.shuffle(FacebookAttributeName.values())){
      int delay = random.nextInt(MAX_DELAY);
      try {
        LOG.log(Level.INFO,"sleeping for {0} milliseconds",delay);
        fapi.getAttribute(name, callback);
        Thread.sleep(delay);
      } catch (InterruptedException ex) {}
    }
    fc.onFinished(object);
      
  }
  
  private final FacebookProfileCrawlerCallback callback = new FacebookProfileCrawlerCallback() {
      @Override
      public void onAttributeCollected(FacebookAttributeName name, String value) {
        LOG.log(Level.INFO, "collected attribute of type {0}: {1}", new String[]{name.toString(),value});
        object.addProperty(name.toString(), value);
      }

      @Override
      public void onError(FacebookAttributeName name,String description) {
        LOG.log(Level.WARNING, "unable to get the attribute of type {0}: {1}", new String[]{name.toString(),description});
      }
  };
  
  /**
   * called when CrawlingEngine collected all information
   */
  public interface FinishCallback{
    /**
     * called when CrawlingEngine collected all information
     * @param object  - object containing all collected attributes
     */
    void onFinished(JsonObject object);
  }
  
  
  
}
