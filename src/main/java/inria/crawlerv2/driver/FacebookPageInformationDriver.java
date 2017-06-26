/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inria.crawlerv2.driver;

import java.net.URI;
import java.util.NoSuchElementException;
import org.openqa.selenium.By;

/**
 *
 * @author adychka
 */
public class FacebookPageInformationDriver extends BasicFacebookPageDriver{

  public FacebookPageInformationDriver(URI target) {
    super(target);
  }
  /**
   * get user name 
   * consist first name, last name can consist of components >=1
   * @return 
   * @throws NoSuchElementException
   */
  public String getName() throws NoSuchElementException{
    driver.get(target.toString());
    return driver.findElement(By.id("fb-timeline-cover-name")).getText(); 
  }
  
  
}
