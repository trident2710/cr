/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inria.crawlerv2.driver;

import inria.crawlerv2.settings.Settings;
import java.net.URI;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 *
 * @author adychka
 */
public class BasicFacebookPageDriver {
  
  protected static final String FACEBOOK_URL = "https://www.facebook.com";
  
  private static final String URL_WITH_ID_TEMPLATE = "profile.php?id=";
  
  protected final WebDriver driver;
  
  protected static final Logger LOG = Logger.getLogger(BasicFacebookPageDriver.class.getName()); 
  /**
   * URI of the page  which should be observed
   */
  protected URI target;
  
  protected final Random random;
  
  public BasicFacebookPageDriver(URI target){
    /**
     * !Important
     * set path to geckodriver. Needed for firefox driver to run
     */
    System.setProperty("webdriver.gecko.driver", Settings.getInstance().getGeckodriverFilePath());
    this.driver = new FirefoxDriver();
    this.target = target;
    this.random = new Random();
    setWaitForElementLoadEnabled(true);  
  }
  
  /**
   * start crawling by getting FB page and logging in 
   * @param username
   * @param password
   * @return 
   */
  public boolean start(String username, String password){
    login(username, password);
    return (!isBanned())&&isLoggedIn();
  }
  
  /**
   * check whether user is logged in or not
   * @return 
   */
  public boolean isLoggedIn() {
    try {
      driver.findElement(By.xpath("//span[@class=\"_2md\"]"));
      return true;
    } catch (NoSuchElementException e) {}
    return false;
  }
  
  public boolean isBanned() {
    randomShortWait();
    return driver.getCurrentUrl().contains("checkpoint");
  }
  
  /**
   * get the user id
   * @param fbUrl - profile url
   * @return 
   */
  public String getUserID(String fbUrl) {
    driver.get(fbUrl);
    randomShortWait();
    driver.findElement(By.xpath(".//div[@data-click='profile_icon']")).click();
    return driver.getCurrentUrl().split("facebook.com/")[1];
  }
  
  /**
   * insert username and password and login in facebook
   * @param username
   * @param password
   */
  protected void login(String username, String password){
    String url = FACEBOOK_URL;
    driver.get(url);
    randomShortWait();
    driver.findElement(By.xpath("//input[@id='email']")).sendKeys(username);
    randomShortWait();
    driver.findElement(By.xpath("//input[@id='pass']")).sendKeys(password);
    randomShortWait();
    driver.findElement(By.xpath("//input[@id='pass']")).sendKeys(Keys.ENTER);

    waitForLoad();
  }
  
  /**
   * wait until javascript will be loaded on the page 
   */
  protected void waitForLoad() {
    new WebDriverWait(driver, 30).until((ExpectedCondition<Boolean>) wd ->
            ((JavascriptExecutor) wd).executeScript("return document.readyState").equals("complete"));
  }
  
  /**
   * scroll the page to the height of page n times
   * @param n - heights of page to scroll
   */
  protected void scrollPage(int n){
    JavascriptExecutor executor = (JavascriptExecutor) driver;
    for (int i = 0; i < n; i++) {
        executor.executeScript("window.scrollTo(0,document.body.scrollHeight);");
    }
  }
  
  /**
   * scroll the page to the bottom
   */
  protected void scrollPage(){
    Long bodyheight_beforeScroll = 0L;
    JavascriptExecutor executor = (JavascriptExecutor) driver;
    Long bodyheight_afterScroll = (Long) executor.executeScript("return document.body.scrollHeight;");
    while (!Objects.equals(bodyheight_afterScroll, bodyheight_beforeScroll)) {
      bodyheight_beforeScroll = bodyheight_afterScroll;
      executor.executeScript("return window.scrollTo(0,document.body.scrollHeight);");
      bodyheight_afterScroll = (Long) executor.executeScript("return document.body.scrollHeight;");
    }
  }
  
  /**
   * wait about MAX_SHORT_WAIT_MILLIS but not less than 2sec
   */
  protected void randomShortWait(){
    waitBetween(Settings.getInstance().getShortWaitMillis()>2000?2000:0, Settings.getInstance().getShortWaitMillis());
  }
  
  protected void waitBetween(int from,int to){
    if(to-from<=0) throw new IllegalArgumentException("to < from");
    try {
      Thread.sleep(from+random.nextInt(to-from));
    } catch (InterruptedException ex) {}
  }
 
  public URI getTarget() {
    return target;
  }

  public void setTarget(URI target) {
    this.target = target;
  }
  
  /**
   * switch on or off the implicit waiting for the element,
   * i.e. if enabled, driver will try to search the specific element in DOM during the time period
   * @param enabled 
   */
  protected final void setWaitForElementLoadEnabled(boolean enabled){
    driver.manage().timeouts().implicitlyWait(enabled?Settings.getInstance().getWaitForElemSec():0, TimeUnit.SECONDS);
  }
  
  /**
   * load the url with the selected attributes
   * @param attrs - url attributes i.e. {sk=about,section=contact-info}
   * @param requiredArgs  - parts which should be present in url to be sure that it leads to the same resource as needed
   * for example, in facebook after loading page the url can change, but it still contains the parts which inform that 
   * the opened page is the same
   */
  protected void loadUrl(String[] attrs,String[] requiredArgs){
    //if(!checkUrlInIdForm()) throw new InvalidArgumentException("target URI is malformed. Does not contain ID");
    if(requiredArgs!=null&&requiredArgs.length>0)
      for(String s:requiredArgs){
        if(!urlContains(s))
          driver.get(formUrl(attrs));   
          return;
      }
    else {
      String url = formUrl(attrs);
      if(!driver.getCurrentUrl().equals(url)){
        driver.get(url);
      }
    }
  }
  
  protected boolean checkUrlInIdForm(){
    if(target==null)
      return false;
    return target.toString().contains(URL_WITH_ID_TEMPLATE);
  }
  
  private boolean urlContains(String token){
    return driver.getCurrentUrl().contains(token);
  }
  
  private String formUrl(String[] attrs){
    String url  = target.toString();
    if(attrs!=null){
      for (int i=0;i<attrs.length;i++) {
        url += ((i==0&&!checkUrlInIdForm())?"?":"&") + attrs[i];
      }
    }
    return url;
  }
  
  
  
  
}
