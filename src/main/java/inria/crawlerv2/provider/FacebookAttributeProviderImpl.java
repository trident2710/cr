/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inria.crawlerv2.provider;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import inria.crawlerv2.driver.FacebookPageInformationDriver;
import java.net.URI;
import java.util.Arrays;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;



/**
 * implementation of facebook attribute provider
 * @author adychka
 */
public class FacebookAttributeProviderImpl implements FacebookAttributeProvider{
  
  /**
   * url of user facebook page, 
   * for example, https://www.facebook.com/john.cena
   */
  private URI url;
  private final FacebookPageInformationDriver fpid;

  public FacebookAttributeProviderImpl(URI url){
    this.url = url;
    this.fpid = new FacebookPageInformationDriver(url);
  }

  @Override
  public void getAttribute(FacebookAttributeName name, FacebookProfileCrawlerCallback callback) {
    String response;
    switch(name){
      case ID:
        response = collectId();
        try {
          JsonParser parser = new JsonParser();
          response = parser.parse(response).getAsJsonObject().get("id").getAsString();
        } catch (JsonSyntaxException e) {
          callback.onError(name,"wrong response");
          return;
        }
        break;
      case FIRST_NAME:
        response = getFirstName();
        break;
      case LAST_NAME:
        response = getLastName();
        break;
      default:
        callback.onError(name,"unresolved attribute");
        return;
    }
    if(response==null||response.isEmpty()){
      callback.onError(name,"unable to get response");
      return;
    } 
    callback.onAttributeCollected(name, response);
      
  }

  public URI getUrl() {
    return url;
  }

  public void setUrl(URI url) {
    this.url = url;
  }
  
  private String collectId(){
    MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
		map.add("url", url.toString());
    RestTemplate restTemplate = new RestTemplate();
    String response = restTemplate.postForObject("https://findmyfbid.com/", map, String.class);
    return response;
  }
  
  private String getFirstName(){
    return fpid.getName().split(" ")[0];
  }
  
  private String getLastName(){
    String[] items = fpid.getName().split(" ");
    if(items.length==1){
     return null;
    }
    return String.join(" ",Arrays.copyOfRange(items, 1, items.length));
  }

  @Override
  public boolean loginWithCredentials(String username, String password) {
    return this.fpid.start(username, password);
  }
}
