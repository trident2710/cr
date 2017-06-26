/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inria.crawlerv2;

import inria.crawlerv2.engine.CrawlingEngine;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 *
 * @author adychka
 */
public class Main {
  /**
   * defines the argument name for providing url of the profile which should be crawled 
   */
  private static final String USER_URL = "-url";
  
  private static final Logger LOG = Logger.getLogger(Main.class.getName()); 

  public static void main(String[] args) throws Exception{
    if(args.length!=2||
    !args[0].equals(USER_URL)||
    args[1]==null||
    args[1].isEmpty()){
      LOG.log(Level.SEVERE,"arguments were not set properly");
      return;
    }
       
    URI uri;
    try {
      uri  = new URI(args[1]);
    } catch (URISyntaxException e) {
      LOG.log(Level.SEVERE,"unable to parse URI from input parameter");
      return;
    }
    LOG.log(Level.INFO, "starting scrapping for the profile: {0}", uri.toString());
    
    
    CrawlingEngine engine = new CrawlingEngine(uri,(object)->{
      LOG.log(Level.INFO, "collected data: {0}", object.toString());
    });
    
    Thread thread = new Thread(engine);
    thread.start();
    
  }
}
