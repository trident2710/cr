/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inria.crawlerv2.engine;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import inria.crawlerv2.constants.Constants;
import inria.crawlerv2.engine.account.Account;
import inria.crawlerv2.engine.account.AccountManager;
import inria.crawlerv2.engine.account.AccountManagerImpl;
import inria.crawlerv2.provider.AttributeName;
import inria.crawlerv2.provider.AttributeProvider;
import inria.crawlerv2.provider.FacebookAttributeProvider;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The class which connects the real facebook crawler realisation with the main program
 * acts like adapter witch requests the attributes from the crawler driven, collects the results and returns to the 
 * main flow.
 * @author adychka
 */
public class CrawlingEngine implements Runnable{
  
  /**
   * max delay between the actions
   */
  private static final Integer MAX_DELAY = 1000*10; //10 seconds
  /**
   * for generating random time intervals for the scrapping
   */
  private final Random random;
  
  private final FacebookAttributeProvider fapi;
  
  private final FinishCallback fc;
  
  private static final Logger LOG = Logger.getLogger(CrawlingEngine.class.getName()); 
  
  private JsonObject object;
  
  private final AccountManager accountManager;
  
  public CrawlingEngine(URI url,FinishCallback fc){
    random = new Random();
    fapi = new FacebookAttributeProvider(url);
    this.fc = fc;
    this.object = new JsonObject();
    this.accountManager = new AccountManagerImpl(Constants.ACCOUNTS_FILE_PATH);
  }

  @Override
  public void run(){
    
    Account account = accountManager.getRandomWorkingAccount();
    fapi.loginWithCredentials(account.getLogin(), account.getPassword());
    
    List<AttributeName[]> names = getAttributesByPages();
    Collections.shuffle(names);
    for(AttributeName[] pages:names){
      int delay = random.nextInt(MAX_DELAY);
      try {
        LOG.log(Level.INFO,"sleeping for {0} milliseconds",delay);

        List<AttributeName> page_names = Arrays.asList(pages);
        Collections.shuffle(page_names);
        for(AttributeName p:page_names){
          fapi.getAttribute(p, callback);
        }

        Thread.sleep(delay);
      } catch (InterruptedException ex) {}
    }
    fc.onFinished(object);
  }
  
  private final AttributeProvider.AttributeCallback callback = new AttributeProvider.AttributeCallback() {
    @Override
    public void onAttributeCollected(AttributeName name, JsonElement value) {
      LOG.log(Level.INFO, "collected attribute of type {0}: {1}", new String[]{name.toString(),value.toString()});
      object.add(name.getName(), value);
    }

    @Override
    public void onError(AttributeName name,String description) {
      LOG.log(Level.WARNING, "unable to get the attribute of type {0}: {1}", new String[]{name.toString(),description});
    }
  };
  
  /**
   * certain attributes are located on the same pages
   * 
   * @return the attribute groups (which are on the same page) to overcome unnecessary waiting
   */
  private List<AttributeName[]> getAttributesByPages(){
    List<AttributeName[]> list = new ArrayList<>();
    list.add(new AttributeName[]{AttributeName.ID});
    list.add(new AttributeName[]{AttributeName.FIRST_NAME,AttributeName.LAST_NAME});
    list.add(new AttributeName[]{AttributeName.FRIEND_IDS});
    list.add(new AttributeName[]{AttributeName.BIRTH_DATE,AttributeName.BIRTH_YEAR,AttributeName.GENDER,
      AttributeName.GENDER_INTERESTS,AttributeName.POLITICAL_VIEW,AttributeName.RELIGIOUS_VIEW,AttributeName.PHONES,
      AttributeName.EMAIL_ADDRESS,AttributeName.LANGUAGES,AttributeName.ADDRESS});
    list.add(new AttributeName[]{AttributeName.WORK_IDS,AttributeName.EDUCATION_IDS});
    return list;
  }
  
  /**
   * called when CrawlingEngine collected all information
   */
  public static interface FinishCallback{
    /**
     * called when CrawlingEngine collected all information
     * @param object  - object containing all collected attributes
     */
    void onFinished(JsonObject object);
  }
}
