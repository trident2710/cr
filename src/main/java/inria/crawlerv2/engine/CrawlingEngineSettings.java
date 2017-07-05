/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inria.crawlerv2.engine;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import inria.crawlerv2.driver.WebDriverOption;
import inria.crawlerv2.engine.account.Account;
import inria.crawlerv2.provider.AttributeName;
import java.net.URI;

/**
 *
 * @author adychka
 */
public class CrawlingEngineSettings {
  
  /**
   * the target profile which will be crawled 
   * for example, https://www.facebook.com/profile.php?id=100001501699606
   */
  @JsonIgnore
  private URI target;
  
  /**
   * which attributes crawler should collect
   */
  private AttributeName[] attributes = AttributeName.values();
  
  /**
   * the upper border for the long delay while crawling attribute
   */
  private int longWaitMillis = 10000;
  
  /**
   * the upper border for the short delay while crawling attribute
   */
  private int shortWaitMillis = 5000;
  
  /**
   * how much seconds should selenium retry if element is not appearing in HTML 
   */
  private int waitForElemLoadSec = 5;
  
  /**
   * the upper border for the delay between crawling the attributes
   * needed to simulate human behavior
   */
  private int requestDelay = 20000;
  
  /**
   * change account after this quantity of crawlings
   */
  private int changeAccountAfter = 10;
  
  /**
   * explicit wait before start crawling
   */
  private int delayBeforeRunInMillis = 0;
  
  /**
   * if not null, will be used as single account to login to facebook
   */
  @JsonIgnore
  private Account singleUsingAccount;
  
  /**
   * PHANTOM - to run silently, 
   * GECKO to run in visual mode
   */
  private WebDriverOption webDriverOption = WebDriverOption.PHANTOM;

  public URI getTarget() {
    return target;
  }

  public AttributeName[] getAttributes() {
    return attributes;
  }

  public int getLongWaitMillis() {
    return longWaitMillis;
  }

  public int getShortWaitMillis() {
    return shortWaitMillis;
  }

  public int getWaitForElemLoadSec() {
    return waitForElemLoadSec;
  }

  public int getRequestDelay() {
    return requestDelay;
  }

  public int getChangeAccountAfter() {
    return changeAccountAfter;
  }

  public Account getSingleUsingAccount() {
    return singleUsingAccount;
  }

  public int getDelayBeforeRunInMillis() {
    return delayBeforeRunInMillis;
  }

  public WebDriverOption getWebDriverOption() {
    return webDriverOption;
  }
  
  public static Builder getStaticBuilder(){
    return new CrawlingEngineSettings().new Builder();
  }
  
  public Builder getBuilder(){
    return this.new Builder();
  }
  
  
  private void check(){
    if(target == null)
      throw new RuntimeException("you must set target attribute");
    if(attributes == null || attributes.length==0)
      throw new RuntimeException("wrong attributes value");
    if(longWaitMillis <=0)
      throw new RuntimeException("wrong longVaitMillis value");
    if(shortWaitMillis <=0)
      throw new RuntimeException("wrong shortVaitMillis value");
    if(waitForElemLoadSec <=0)
      throw new RuntimeException("wrong waitForElemLoadSec value");
    if(requestDelay <=0)
      throw new RuntimeException("wrong requestDelay value");
    if(changeAccountAfter <=0)
      throw new RuntimeException("wrong changeAccountAfter value");
    if(delayBeforeRunInMillis <0)
      throw new RuntimeException("wrong delayBeforeRunInMillis value");
    if(webDriverOption == null)
      throw new RuntimeException("wrong webDriverOption value");
  }
  
  @JsonIgnoreType
  public class Builder{
    
    public Builder setSingleUsingAccount(Account account){
      CrawlingEngineSettings.this.singleUsingAccount = account;
      return this;
    }
    
    public Builder setTarget(URI target){
      CrawlingEngineSettings.this.target = target;
      return this;
    }
    
    public Builder setCollectAttributes(AttributeName[] attributes){
      CrawlingEngineSettings.this.attributes = attributes;
      return this;
    }
    
    public Builder setLongWaitMillis(int longWaitMillis){
      CrawlingEngineSettings.this.longWaitMillis = longWaitMillis;
      return this;
    }
    
    public Builder setShortWaitMillis(int shortWaitMillis){
      CrawlingEngineSettings.this.shortWaitMillis = shortWaitMillis;
      return this;
    }
    
    public Builder setWaitForElemLoadSec(int waitForElemLoadSec){
      CrawlingEngineSettings.this.waitForElemLoadSec = waitForElemLoadSec;
      return this;
    }
    
    public Builder setRequestDelay(int requestDelay){
      CrawlingEngineSettings.this.requestDelay = requestDelay;
      return this;
    }
    
    public Builder setChangeAccountAfter(int changeAccountAfter){
      CrawlingEngineSettings.this.changeAccountAfter = changeAccountAfter;
      return this;
    }
    
    public Builder setDelayBeforeRunInMillis(int delayBeforeRunInMillis){
      CrawlingEngineSettings.this.delayBeforeRunInMillis = delayBeforeRunInMillis;
      return this;
    }
    
    public Builder setWebDriverOption(WebDriverOption option){
      CrawlingEngineSettings.this.webDriverOption = option;
      return this;
    }
    
    public CrawlingEngineSettings build(){
      CrawlingEngineSettings.this.check();
      return CrawlingEngineSettings.this;
    }
  }
  
}
