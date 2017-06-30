/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inria.crawlerv2.provider;

import com.google.gson.JsonElement;

/**
 * declares the requirement for the profile attribute provider 
 * @author adychka
 */
public interface AttributeProvider {
  
    /**
     * login to facebook with specified credentials
     * @param username
     * @param password 
     * @return true if successfully logged in
     */
    boolean loginWithCredentials(String username,String password);
    /**
     * get the attribute from the facebook account
     * @param name - which attribute should be collected
     * @param callback
     */
    void getAttribute(AttributeName name,AttributeCallback callback);
    
    /**
     * change the target url to the form: facebook.com/profile.php?id=123124
     * @param id 
     */
    void transformTargetWithId(String id);
    
    
    /**
    * callback for collecting the attribute from facebook profile
    * @author adychka
    */
  public static interface AttributeCallback {
    /**
     * invoked if such attribute was successfully collected
     * @param name
     * @param value 
     */
    void onAttributeCollected(AttributeName name,JsonElement value);
    
    /**
     * invoked if the error occured while collecting the attribute
     * @param description - error description
     * @param name
     */
    void onError(AttributeName name,String description);
  }
    
}