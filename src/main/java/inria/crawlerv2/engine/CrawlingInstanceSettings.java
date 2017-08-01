/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inria.crawlerv2.engine;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import inria.crawlerv2.driver.WebDriverOption;
import inria.crawlerv2.provider.AttributeName;

/**
 *
 * @author adychka
 */
public class CrawlingInstanceSettings {

    /**
     * which attributes crawler should collect
     */
    protected AttributeName[] attributes = AttributeName.values();

    /**
     * the upper border for the long delay while crawling attribute
     */
    protected int longWaitMillis = 10000;

    /**
     * the upper border for the short delay while crawling attribute
     */
    protected int shortWaitMillis = 5000;

    /**
     * how much seconds should selenium retry if element is not appearing in
     * HTML
     */
    protected int waitForElemLoadSec = 5;

    /**
     * the upper border for the delay between crawling the attributes needed to
     * simulate human behavior
     */
    protected int requestDelay = 20000;

    /**
     * change login account after this number of crawlings
     */
    protected int changeAccountAfter = 10;

    /**
     * explicit wait before start crawling
     */
    protected int delayBeforeRunInMillis = 0;

    /**
     * maximum amount of friends to collect for current profile
     */
    protected int maxFriendsToCollect = 10000;

    /**
     * stop scrolling down the list of friends after this amount of friends
     */
    protected int maxFriendsToDiscover = 10000;

    /**
     * PHANTOM - to run silently, GECKO to run in visual mode
     */
    protected WebDriverOption webDriverOption = WebDriverOption.PHANTOM;

    public AttributeName[] getAttributes() {
        return attributes;
    }

    public int getLongWaitMillis() {
        return longWaitMillis;
    }

    public int getShortWaitMillis() {
        return shortWaitMillis;
    }

    public int getWaitForElemLoadSec() {
        return waitForElemLoadSec;
    }

    public int getRequestDelay() {
        return requestDelay;
    }

    public int getChangeAccountAfter() {
        return changeAccountAfter;
    }

    public int getDelayBeforeRunInMillis() {
        return delayBeforeRunInMillis;
    }

    public WebDriverOption getWebDriverOption() {
        return webDriverOption;
    }

    public int getMaxFriendsToCollect() {
        return maxFriendsToCollect;
    }

    public int getMaxFriendsToDiscover() {
        return maxFriendsToDiscover;
    }

    public void setAttributes(AttributeName[] attributes) {
        this.attributes = attributes;
    }
    
    public static Builder getStaticBuilder() {
        return new CrawlingInstanceSettings().new Builder();
    }

    public Builder getBuilder() {
        return this.new Builder();
    }

    public void check() {
        if (attributes == null || attributes.length == 0) {
            throw new RuntimeException("wrong attributes value");
        }
        if (longWaitMillis < 0) {
            throw new RuntimeException("wrong longVaitMillis value");
        }
        if (shortWaitMillis < 0) {
            throw new RuntimeException("wrong shortVaitMillis value");
        }
        if (waitForElemLoadSec < 0) {
            throw new RuntimeException("wrong waitForElemLoadSec value");
        }
        if (requestDelay < 0) {
            throw new RuntimeException("wrong requestDelay value");
        }
        if (changeAccountAfter < 0) {
            throw new RuntimeException("wrong changeAccountAfter value");
        }
        if (delayBeforeRunInMillis < 0) {
            throw new RuntimeException("wrong delayBeforeRunInMillis value");
        }
        if (maxFriendsToCollect < 1) {
            throw new RuntimeException("wrong maxFriendsToCollect value");
        }
        if (maxFriendsToDiscover< 1) {
            throw new RuntimeException("wrong maxFriendsToScroll value");
        }
        if (maxFriendsToDiscover< maxFriendsToCollect) {
            throw new RuntimeException("maxFriendsToCollect could not be greater than maxFriendsToDiscover");
        }
        if (webDriverOption == null) {
            throw new RuntimeException("wrong webDriverOption value");
        }

    }

    @JsonIgnoreType
    public class Builder {

        public Builder setCollectAttributes(AttributeName[] attributes) {
            CrawlingInstanceSettings.this.attributes = attributes;
            return this;
        }

        public Builder setLongWaitMillis(int longWaitMillis) {
            CrawlingInstanceSettings.this.longWaitMillis = longWaitMillis;
            return this;
        }

        public Builder setShortWaitMillis(int shortWaitMillis) {
            CrawlingInstanceSettings.this.shortWaitMillis = shortWaitMillis;
            return this;
        }

        public Builder setWaitForElemLoadSec(int waitForElemLoadSec) {
            CrawlingInstanceSettings.this.waitForElemLoadSec = waitForElemLoadSec;
            return this;
        }

        public Builder setRequestDelay(int requestDelay) {
            CrawlingInstanceSettings.this.requestDelay = requestDelay;
            return this;
        }

        public Builder setChangeAccountAfter(int changeAccountAfter) {
            CrawlingInstanceSettings.this.changeAccountAfter = changeAccountAfter;
            return this;
        }

        public Builder setDelayBeforeRunInMillis(int delayBeforeRunInMillis) {
            CrawlingInstanceSettings.this.delayBeforeRunInMillis = delayBeforeRunInMillis;
            return this;
        }

        public Builder setMaxFriendsToCollect(int maxFriendsToCollect) {
            CrawlingInstanceSettings.this.maxFriendsToCollect = maxFriendsToCollect;
            return this;
        }
        
        public Builder setMaxFriendsToDiscover(int maxFriendsToDiscover) {
            CrawlingInstanceSettings.this.maxFriendsToDiscover = maxFriendsToDiscover;
            return this;
        }

        public Builder setWebDriverOption(WebDriverOption option) {
            CrawlingInstanceSettings.this.webDriverOption = option;
            return this;
        }

        public CrawlingInstanceSettings build() {
            CrawlingInstanceSettings.this.check();
            return CrawlingInstanceSettings.this;
        }
    }

}
