/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inria.crawlerv2.provider;

/**
 * declares the requirement for the facebook profile attribute provider 
 * @author adychka
 */
public interface FacebookAttributeProvider {
  
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
    void getAttribute(FacebookAttributeName name,FacebookProfileCrawlerCallback callback);
}
