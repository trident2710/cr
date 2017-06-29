/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inria.crawlerv2.provider;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import inria.crawlerv2.driver.FacebookPageInformationDriver;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * implementation of facebook attribute provider
 * @author adychka
 */
public class FacebookAttributeProvider implements AttributeProvider{
  
  private static final String FIND_ID_URL = "https://findmyfbid.com/";
  private static final String TARGET_WITH_ID_TEMPLATE = "https://www.facebook.com/profile.php?id=";
  private static final Logger LOG = Logger.getLogger( FacebookAttributeProvider.class.getName()); 
  
  /**
   * url of user facebook page, 
   * for example, https://www.facebook.com/profile.php?id=6456
   */
  private URI url;
  private final FacebookPageInformationDriver fpid;

  public FacebookAttributeProvider(URI url){
    this.url = url;
    this.fpid = new FacebookPageInformationDriver(url);
  }

  @Override
  public void getAttribute(AttributeName name, AttributeCallback callback) {
    JsonElement response = null;
    Object val = getAttributeByName(name);
    
    if(val==null){
      callback.onError(name,"unable to get response");
      return;
    }
    if(val instanceof String){
      String v= (String)val;
      if(!v.isEmpty())
      response = new JsonPrimitive(v);
    }
    if(val instanceof List){
      List<String> v = (List<String>)val;
      if(!v.isEmpty()){
        JsonArray array = new JsonArray();
        v.forEach((s) -> {
          array.add(s);
        });
        response = array;
      } 
    }
    if(response==null){
      callback.onError(name,"unable to get response");
      return;
    } 
    callback.onAttributeCollected(name, response);    
  }
  
  private Object getAttributeByName(AttributeName name){
    switch(name){
      case ID: return collectId();
      case FIRST_NAME: return getFirstName();
      case LAST_NAME: return getLastName();   
      case FRIEND_IDS: return getPageIdsFromUrls(fpid.getFriends());
      case BIRTH_DATE: return fpid.getBirthDate();
      case BIRTH_YEAR: return fpid.getBirthYear();
      case RELIGIOUS_VIEW: return fpid.getReligion();
      case POLITICAL_VIEW: return fpid.getPolitic();
      case GENDER: return fpid.getGender();
      case GENDER_INTERESTS: return fpid.getGenderInterest();
      case LANGUAGES: return fpid.getLanguages();
      case PHONES: return fpid.getMobilePhones();
      case ADDRESS: return fpid.getAddress();
      case EMAIL_ADDRESS: return fpid.getEmailAddress();
      case WORK_IDS: return getPageIdsFromUrls(fpid.getWorks());
      case EDUCATION_IDS: return getPageIdsFromUrls(fpid.getEducations());
      default: throw new IllegalArgumentException("unsupported attribute");
    }
  }

  public URI getUrl() {
    return url;
  }

  public void setUrl(URI url) {
    this.url = url;
  }
  
  private String collectId(){
    return collectId(url.toString());
  }
  
  private String collectId(String url){
    LOG.log(Level.INFO,"collecting the id for the page: {0}",url);
    //if there is already id in url
    if(url.contains("id=")){
      return url.split("&")[0].split("=")[1];
    }
    url = url.split(Pattern.quote("?"))[0];
    
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("url", url);
    RestTemplate restTemplate = new RestTemplate();
    String response = restTemplate.postForObject(FIND_ID_URL, map, String.class);
    try {
      return new JsonParser().parse(response).getAsJsonObject().get("id").getAsString();
    } catch (JsonSyntaxException|IllegalStateException e) {
      LOG.log(Level.WARNING,"unable to collect id for the profile: {0}",url);
    }
    return null;
  }
  
  private String getFirstName(){
    String name = fpid.getName();
    return name!=null?name.split(" ")[0]:name;
  }
  
  private String getLastName(){
    String name = fpid.getName();
    if(name==null) return null;
    String[] items = name.split(" ");
    if(items.length==1){
     return null;
    }
    return String.join(" ",Arrays.copyOfRange(items, 1, items.length));
  }
  private List<String> getPageIdsFromUrls(List<String> pageUrls){
    if(pageUrls==null||pageUrls.isEmpty()) return null;
    List<String> ids = new ArrayList<>();
    pageUrls.forEach((u) -> {
      try {
          ids.add(collectId(u));
        } catch (Exception e) {
          ids.add(u);
          LOG.log(Level.WARNING,"unable to collect id for the page: {0}",u);
        } 
    });
    return ids;
  }
  
  @Override
  public boolean loginWithCredentials(String username, String password) {
    return this.fpid.start(username, password);
  }

  @Override
  public void transformTargetWithId(String id) {
    try {
      this.url = new URI(TARGET_WITH_ID_TEMPLATE+id);
      this.fpid.setTarget(url);
    } catch (URISyntaxException ex) {}
  }
}
