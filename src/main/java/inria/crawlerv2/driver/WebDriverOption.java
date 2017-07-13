/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inria.crawlerv2.driver;

/**
 * @author adychka defines the variants of webdriver which can be used to crawl
 * the page
 */
public enum WebDriverOption {
    /**
     * will crawl in visual mode. i.e. the prosess will be visible
     */
    GECKO,
    /**
     * will crawl in silent mode. i.e. the process will be not visible
     */
    PHANTOM
}
