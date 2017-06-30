/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inria.crawlerv2.settings;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * class for loading the settings from file
 * follows singleton pattern
 * @author adychka
 */
public final class Settings {
  
  /**
   * defining the setting properties which should be present in file
   */
  private enum SettingsFields{
    /**
     * path to the file with the facebook acounts to log in facebook
     */
    FB_ACCOUNTS_PATH(String.class),
    /**
     * path to geckodriver which is needed for Mozilla WebDriver in Selenium
     * @see https://github.com/mozilla/geckodriver/releases
     */
    GECKODRIVER_PATH(String.class),
    
    /**
     * the folder in which collected data should be loaded
     */
    OUTPUT_DATA_PATH(String.class),
    
    /**
     * the upper border for the long delay while crawling attribute
     */
    LONG_WAIT_MILLIS(Integer.class),
    /**
     * the upper border for the short delay while crawling attribute
     */
    SHORT_WAIT_MILLIS(Integer.class),
    /**
     * how much seconds should selenium retry if element is not appearing in HTML 
     */
    WAIT_FOR_ELEM_LOAD_SEC(Integer.class),
    /**
     * the upper border for the delay between crawling the attributes
     * needed to simulate human behavior
     */
    REQUEST_DELAY(Integer.class);
    
    private Object value;
    
    private final Class c;
    
    private SettingsFields(Class c){
      this.c = c;
    }
    public Class getPropertyClass(){
      return c;
    }
    public Object getValue(){
      return value;
    }
    public String getPropertyName(){
      return this.name().toLowerCase();
    }
    public void setValue(Object value){
      this.value = value;
    }
  }
  
  private final static String SETTINGS_PATH = "settings.json";
  
  private static final Settings _instance = new Settings();
  
  private boolean isLoaded;
 
  private Settings(){init();}
  
  /**
   * get the single settings instance
   * @return 
   */
  public static Settings getInstance(){
    return _instance;
  }
  
  /**
   * needed to ensure that settings were loaded correctly
   * @return 
   */
  public boolean isLoadedCorrectly(){
    return isLoaded;
  }
  
  public String getFacebookAccountsFilePath(){
    return (String)SettingsFields.FB_ACCOUNTS_PATH.getValue();
  }
  
  public String getGeckodriverFilePath(){
    return (String)SettingsFields.GECKODRIVER_PATH.getValue();
  }
  
  public String getOutputDataPath(){
    return (String)SettingsFields.OUTPUT_DATA_PATH.getValue();
  }
  
  public Integer getLongWaitMillis(){
    return (Integer)SettingsFields.LONG_WAIT_MILLIS.getValue();
  }
  
  public Integer getShortWaitMillis(){
    return (Integer)SettingsFields.SHORT_WAIT_MILLIS.getValue();
  }
  
  public Integer getWaitForElemSec(){
    return (Integer)SettingsFields.WAIT_FOR_ELEM_LOAD_SEC.getValue();
  }
  
  public Integer getRequestDelayMillis(){
    return (Integer)SettingsFields.REQUEST_DELAY.getValue();
  }
  
  private void init(){
    try {
      Gson gson = new Gson();
      JsonReader reader = new JsonReader(new FileReader(SETTINGS_PATH));
      JsonObject object = gson.fromJson(reader, JsonObject.class);
      for(SettingsFields f:SettingsFields.values()){
        if(f.getPropertyClass()==String.class){
          f.setValue(object.get(f.getPropertyName()).getAsString());
        } else f.setValue(object.get(f.getPropertyName()).getAsInt()); 
      }
      for(SettingsFields f:SettingsFields.values()) 
        if(f.getValue()==null){
          Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, "field is missing in settings: {0}", f.getPropertyName());
          return;
        }
      isLoaded = true;
    } catch (FileNotFoundException|IllegalStateException|JsonSyntaxException ex) {
      Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, "unabe to load settings");
    }
  }
 
}
