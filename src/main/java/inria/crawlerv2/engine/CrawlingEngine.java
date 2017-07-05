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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The class which connects the facebook crawler realisation with the main program
 * acts like adapter witch requests the attributes from the crawler driver, collects the results and returns to the 
 * main flow.
 * @author adychka
 */
public class CrawlingEngine implements Runnable{
  
  /**
   * for generating random time intervals for the scrapping
   */
  private Random random;
  
  private FacebookAttributeProvider fapi;
  
  private FinishCallback fc;
  
  private static final Logger LOG = Logger.getLogger(CrawlingEngine.class.getName()); 
  
  private JsonObject object;
  
  private AccountManager accountManager;
  
  private CrawlingEngineSettings settings;
  
  public CrawlingEngine(AccountManager accountManager,CrawlingEngineSettings settings,FinishCallback fc){
    random = new Random();
    FacebookPageInformationDriver fpid = new FacebookPageInformationDriver(
            settings.getTarget(), 
            settings.getWebDriverOption(), 
            settings.getWaitForElemLoadSec(), 
            settings.getShortWaitMillis());
    
    fapi = new FacebookAttributeProvider(settings.getTarget(),fpid);
    this.fc = fc;
    this.object = new JsonObject();
    this.accountManager = accountManager;
    this.settings = settings;
  }
  
 

  @Override
  public void run(){
    boolean useDefault = false;
    
    if(settings.getSingleUsingAccount()!=null){
      if(login(settings.getSingleUsingAccount()))
        useDefault = true;
      else{
        useDefault = false;
        LOG.log(Level.SEVERE,"unable to login with provided account");
        finish(null);
        return;  
      }
    } 
    
    if(!useDefault)
      try {
        login();
      } catch (NoWorkingAccountsException e) {
        LOG.log(Level.SEVERE,"no working accounts left");
        finish(null);
        return;
      }
    
    if(settings.getDelayBeforeRunInMillis()!=0){
      LOG.log(Level.INFO,"explicitly waiting {0} millis");
      try {
        wait(settings.getDelayBeforeRunInMillis());
      } catch (InterruptedException ex) {}
    }
    
    if(!Arrays.equals(settings.getAttributes(),AttributeName.values())){
      crawlBlock(settings.getAttributes());
    } else{
      List<AttributeName[]> names = getAttributesByPages();
      Collections.shuffle(names);
      for(AttributeName[] block:names){
        crawlBlock(block);
      }
    }
    finish(object);
  }
  
  private void crawlBlock(AttributeName[] block){
    int delay = random.nextInt(settings.getRequestDelay());
    LOG.log(Level.INFO,"sleeping for {0} milliseconds",delay);

    try {
      List<AttributeName> page_names = Arrays.asList(block);
      Collections.shuffle(page_names);
      for(AttributeName p:page_names){
        LOG.log(Level.INFO,"crawling {0}:",p.getName());
        fapi.getAttribute(p, callback);
      }

      Thread.sleep(delay);
    } catch (InterruptedException ex) {}
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
  
  private void login() throws NoWorkingAccountsException{
    if(accountManager.getWorkingAccounts().isEmpty())
      throw new NoWorkingAccountsException();
    
    Account acc = accountManager.getRandomWorkingAccount();
    if(!login(acc)){
      login();
    }
  }
  
  private boolean login(Account acc){
    if(!fapi.loginWithCredentials(acc.getLogin(), acc.getPassword())){
      LOG.log(Level.WARNING, "unable to login");
      acc.setIsBanned(true);
      accountManager.save();
      return false;
    }
    return true;
  }
  
  private void finish(JsonObject object){
    if(object==null){
      LOG.log(Level.SEVERE,"impossible to collect data");
    }
    fapi.finishSession();
    fc.onFinished(object);
  }
  
  private class NoWorkingAccountsException extends Exception{};
  
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
