/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inria.crawlerv2;

import com.google.gson.JsonObject;
import inria.crawlerv2.engine.CrawlingEngine;
import inria.crawlerv2.provider.AttributeName;
import inria.crawlerv2.settings.Settings;
import inria.crawlerv2.utils.FileUtils;
import java.io.IOException;
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
    
    /**
     * for test only
     * remove before deploy
     */
    args = new String[]{"-url","https://www.facebook.com/profile.php?id=100001501699606"};
    
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
    
    if(!Settings.getInstance().isLoadedCorrectly()){
      LOG.log(Level.SEVERE, "unable to correctly load the settings. Program will now terminate");
      return;
    }
    
    LOG.log(Level.INFO, "starting scrapping for the profile: {0}", uri.toString());
    
    CrawlingEngine engine = new CrawlingEngine(uri,(object)->{
      if(object==null){
        LOG.log(Level.SEVERE, "no data collected");
        return;
      }
      LOG.log(Level.INFO, "collected data: {0}", object.toString());
      try {
        writeFacebookProfileToFile(object);
      } catch (IOException e) {
        LOG.log(Level.SEVERE, "unable to write output to file", object.toString());
      }    
    });
    
    Thread thread = new Thread(engine);
    thread.start();  
  } 
  
  private static void writeFacebookProfileToFile(JsonObject attributes) throws IOException{
    String fileName = "";
    if(attributes.has(AttributeName.ID.getName())&&!attributes.get(AttributeName.ID.getName()).isJsonNull())
      fileName = attributes.get(AttributeName.ID.getName()).getAsString();
    else{
      if(attributes.has(AttributeName.FIRST_NAME.getName())&&attributes.has(AttributeName.LAST_NAME.getName()))
        fileName = attributes.get(AttributeName.FIRST_NAME.getName()).getAsString()+attributes.get(AttributeName.LAST_NAME.getName()).getAsString();
      else fileName = "FBAttributes"+System.currentTimeMillis();    
    }
    fileName = Settings.getInstance().getOutputDataPath()+fileName+".json";
    LOG.log(Level.INFO, "writing data to file :{0}",fileName);
    FileUtils.writeObjectToFile(Settings.getInstance().getOutputDataPath(), fileName, attributes);
  }
  
}
