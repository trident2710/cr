/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inria.crawlerv2.driver;

/**
 *
 * @author adychka
 */
public enum AttributeVisibility {
    SELF,
    FRIEND,
    FRIEND_OF_FRIEND,
    PUBLIC;
    
    public boolean isStrongerThan(AttributeVisibility other){
        return other.ordinal()>this.ordinal();
    }
}
