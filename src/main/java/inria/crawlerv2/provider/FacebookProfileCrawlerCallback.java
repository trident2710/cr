/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inria.crawlerv2.provider;

/**
 * callback for collecting the attribute from facebook profile
 * @author adychka
 */
public interface FacebookProfileCrawlerCallback {
    /**
     * invoked if such attribute was successfully collected
     * @param name
     * @param value 
     */
    void onAttributeCollected(FacebookAttributeName name,String value);
    
    /**
     * invoked if the error occured while collecting the attribute
     * @param description - error description
     * @param name
     */
    void onError(FacebookAttributeName name,String description);
}
