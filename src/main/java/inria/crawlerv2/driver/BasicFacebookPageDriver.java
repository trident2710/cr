/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inria.crawlerv2.driver;

import java.net.URI;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 *
 * @author adychka
 */
public class BasicFacebookPageDriver {
  
  protected final WebDriver driver;
  /**
   * URI of the page  which should be observed
   */
  protected final URI target;
  
  public BasicFacebookPageDriver(URI target){
    this.driver = new FirefoxDriver();
    this.target = target;
  }
  /**
   * start crawling by getting FB page and logging in 
   * @param username
   * @param password
   * @return 
   */
  public boolean start(String username, String password){
    login(username, password);
    return isLoggedIn();
  }
  
  /**
   * check whether user is logged in or not
   * @return 
   */
  public boolean isLoggedIn() {
    try {
      driver.findElement(By.xpath("//div[@data-click='profile_icon']/a"));
      return true;
    } catch (NoSuchElementException e) {}
    return false;
  }
  
  /**
   * check whether this account is banned or not
   * @param driver
   * @return 
   */
  public boolean isBanned(WebDriver driver) {
    try {
      driver.findElement(By.xpath("//div [@id=\"globalContainer\"]"
              + "/div[@id=\"content\"]"
              + "/div[@class=\"UIFullPage_Container\"]"
              + "/div[@id=\"confirm_center\"]"
              + "/div[@class=\"mvl ptm uiInterstitial uiInterstitialLarge uiBoxWhite\"]"
              + "/div[@class=\"uiHeader uiHeaderBottomBorder mhl mts uiHeaderPage interstitialHeader\"]"
              + "/div[@class=\"clearfix uiHeaderTop\"]"
      ));
      return true;
    } catch (NoSuchElementException e) {}
    try {
        driver.findElement(By.xpath("//div [@id=\"globalContainer\"]"
                + "/div[@id=\"content\"]"
                + "/div[@class=\"mvl ptm uiInterstitial uiInterstitialLarge uiBoxWhite\"]"
                + "/div[@class=\"uiHeader uiHeaderWithImage uiHeaderBottomBorder mhl mts uiHeaderPage interstitialHeader\"]"
                + "/div[@class=\"clearfix uiHeaderTop\"]"
        ));
        return true;
    } catch (NoSuchElementException e) {}
    return false;
  }
  
  
  /**
   * insert username and password and login in facebook
   * @param username
   * @param password
   */
  protected void login(String username, String password){
    String url = "https://www.facebook.com";
    driver.get(url);

    driver.findElement(By.xpath("//input[@id='email']")).sendKeys(username);
    driver.findElement(By.xpath("//input[@id='pass']")).sendKeys(password);
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
   * zoom the page in
   * @param n 
   */
  protected void zoomIn(int n) {
    for (int i = 0; i < n; i++) {
      new Actions(driver).sendKeys(Keys.chord(Keys.CONTROL, Keys.ADD)).perform();
    }
  }
  
  /**
   * zoom the page out
   * @param n 
   */
  protected void zoomOut(int n) {
    for (int i = 0; i < n; i++) {
      new Actions(driver).sendKeys(Keys.chord(Keys.CONTROL, Keys.SUBTRACT)).perform();
    }
  }
}
