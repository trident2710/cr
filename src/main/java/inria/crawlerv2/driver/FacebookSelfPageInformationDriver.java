/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inria.crawlerv2.driver;

import static inria.crawlerv2.driver.BasicFacebookPageDriver.LOG;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 *
 * @author adychka
 */
public class FacebookSelfPageInformationDriver extends FacebookPageInformationDriver{
    
    protected Map<String, String> basicInfoVisCash;
    protected Map<String, String> contactInfoVisCash;
    protected Map<String, String> workEducationVisCash;
    
    public FacebookSelfPageInformationDriver(URI target, WebDriverOption option, int waitElemSec, int shortWaitMillis, int maxFriends) {
        super(target, option, waitElemSec, shortWaitMillis, maxFriends);
    }
    
    @Override
    protected void initCash() {
        super.initCash();
        basicInfoVisCash = new HashMap<>();
        contactInfoVisCash = new HashMap<>();
        workEducationVisCash = new HashMap<>();
    }
    
    
    /**
     * get the profile birth date
     *
     * @return
     */
    public String getBirthdayVisibility() {
        return getBasicInfoVisByKey("year of birth"); 
    }
    
    /**
     * get the profile gender interest
     *
     * @return
     */
    public String getGenderInterestVisibility() {
        return getBasicInfoVisByKey("interested in");
    }

    /**
     * get the profile religion
     *
     * @return
     */
    public String getReligionVisibility() {
        return getBasicInfoVisByKey("religious views");
    }

    /**
     * get the profile politic
     *
     * @return
     */
    public String getPoliticVisibility() {
        return getBasicInfoVisByKey("political views");
    }

    /**
     * get the profile languages
     *
     * @return
     */
    public String getLanguagesVisibility() {
        return getBasicInfoVisByKey("languages");
    }

    /**
     * get the mobile phones from contact information
     *
     * @return
     */
    public String getMobilePhonesVisibility() {
        return getContactInfoVisByKey("mobile phones");
    }

    /**
     * get address from contact information
     *
     * @return
     */
    public String getAddressVisibility() {
        return getContactInfoVisByKey("address");
    }

    /**
     * get address from contact information
     *
     * @return
     */
    public String getEmailAddressVisibility() {
        return getContactInfoVisByKey("email");
    }

    /**
     * get work from edu work information
     *
     * @return
     */
    public String getWorksVisibility() {
        return getEduWorkInfoVisByKey("work");
    }
    
    /**
     * get education from edu work information
     *
     * @return
     */
    public String getEducationsVisibility() {
        return getEduWorkInfoVisByKey("education");
    }
    
    protected AttributeVisibility getAttributeVisibilityForElement(WebElement element){
        String val = element.getAttribute("data-tooltip-content");
        if(val.contains("Public")) return AttributeVisibility.PUBLIC;
        if(val.contains("Your friends of friends")) return AttributeVisibility.FRIEND_OF_FRIEND;
        if(val.contains("Your friends")) return AttributeVisibility.FRIEND;
        if(val.contains("Only me")) return AttributeVisibility.SELF;
        return AttributeVisibility.FRIEND;
    }
    
     protected String getBasicInfoVisByKey(String key) {
        Map<String, String> basicInfoVis = basicInfoVisCash.isEmpty() ? getBasicInformationVis() : basicInfoVisCash;
        return basicInfoVis != null && basicInfoVis.containsKey(key) ? basicInfoVis.get(key).replaceFirst("\\|", "") : null;
    }

    protected String getContactInfoVisByKey(String key) {
        Map<String, String> contactInfoVis = contactInfoVisCash.isEmpty() ? getContactInformationVis() : contactInfoVisCash;
        return contactInfoVis != null && contactInfoVis.containsKey(key) ? contactInfoVis.get(key).replaceFirst("\\|", "") : null;
    }

    protected String getEduWorkInfoVisByKey(String key) {
        Map<String, String> contactInfoVis = workEducationVisCash.isEmpty() ? getWorkEducationInformationVis() : workEducationVisCash;
        return contactInfoVis != null && contactInfoVis.containsKey(key) ? contactInfoVis.get(key).replaceFirst("\\|", "") : null;
    }
    
    protected Map<String, String> getBasicInformationVis() {
        loadUrl(new String[]{"sk=about", "section=contact-info"}, new String[]{"contact-info"});
        setWaitForElementLoadEnabled(false);
        Map<String, String> map = new HashMap<>();
        List<WebElement> elements;
        try {
            elements = driver.findElements(By.xpath(".//div[@id='pagelet_basic']/div/ul/li"));
        } catch (Exception e) {
            LOG.log(Level.WARNING, "unable to find .//div[@id='pagelet_basic']/div/ul/li");
            setWaitForElementLoadEnabled(true);
            return null;
        }
        for (WebElement element : elements) {
            try {
                String attrName = element.findElement(By.xpath("./div/div/span")).getText().toLowerCase();
                String attrValue = getAttributeVisibilityForElement(element.findElement(By.xpath("./div/div/div/ul/li[1]/a"))).name();
                map.put(attrName, attrValue);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "unable to get attribute from basic info");
            }
        }
        basicInfoVisCash = map;
        setWaitForElementLoadEnabled(true);
        return map;
    }
    
 
    protected Map<String, String> getContactInformationVis() {
        loadUrl(new String[]{"sk=about", "section=contact-info"}, new String[]{"contact-info"});

        try {
            setWaitForElementLoadEnabled(false);
            Map<String, String> attributes = new HashMap<>();
            List<WebElement> elements;
            elements = driver.findElements(By.xpath(".//*[@id='pagelet_contact']/div/div/ul/li"));
            for (WebElement element : elements) {
                try {
                    String name = element.findElement(By.xpath("./div/div/span")).getText().toLowerCase();
                    String val = "";
                    switch (name) {
                        case "mobile phones":
                            val = getMobilePhonesVis(element);
                            if (val != null) {
                                attributes.put("mobile phones", val);
                            }
                            break;
                        case "address":
                            val = getAddressVis(element);
                            if (val != null) {
                                attributes.put("address", val);
                            }
                            break;
                        case "email address":
                        case "email":
                            val = getEmailAddressVis(element);
                            if (val != null) {
                                attributes.put("email", val);
                            }
                            break;
                    }
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "unable to get attribute from contact information");
                }
            }

            contactInfoVisCash = attributes;
            setWaitForElementLoadEnabled(true);
            return attributes;
        } catch (Exception e) {

            LOG.log(Level.WARNING, "unable to contact information");
        }
        setWaitForElementLoadEnabled(true);
        return null;
    }

    protected String getMobilePhonesVis(WebElement element) {
        try {
            String attrValue = "";
            List<WebElement> elements;
            elements = element.findElements(By.xpath("./div/div/div/div/span/ul/li"));
            for (WebElement el : elements) {
                try {
                    attrValue = getAttributeVisibilityForElement(el.findElement(By.xpath("./ul/li[3]/a"))).name();
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "unable to get phone");
                }
            }
            return !attrValue.isEmpty() ? attrValue : null;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "unable to find phones");
        }
        return null;
    }

    protected String getEmailAddressVis(WebElement element) {
        try {
            String attrValue = "";
            List<WebElement> elements;
            elements = element.findElements(By.xpath("./div/div/div/div/span/ul/li"));
            for (WebElement el : elements) {
                try {
                    attrValue = getAttributeVisibilityForElement(el.findElement(By.xpath("./ul/li[2]/a"))).name();
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "unable to get email from basic info");
                }
            }
            
            return !attrValue.isEmpty() ? attrValue : null;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "unable to find email");
        }
        return null;
    }

    protected String getAddressVis(WebElement element) {
        try {
            String attrValue = "";
            attrValue = getAttributeVisibilityForElement(element.findElement(By.xpath("./div/div/div/ul/li[1]/a"))).name();
            return !attrValue.isEmpty() ? attrValue : null;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "unable to find address");
        }
        return null;
    }
       
    protected Map<String, String> getWorkEducationInformationVis() {
        loadUrl(new String[]{"sk=about", "section=education"}, new String[]{"education"});
        setWaitForElementLoadEnabled(false);
        Map<String, String> map = new HashMap<>();
        List<WebElement> elements;
        try {
            WebElement work = driver.findElement(By.xpath(".//div[@id='pagelet_eduwork' or @id='pagelet_edit_eduwork']//div[@data-pnref='work']/ul"));
            String val = getEduWorkListItemsVis(work);
            if (val != null) {
                map.put("work", val);
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "unable to find work pagelet");
        }
        try {
            WebElement edu = driver.findElement(By.xpath(".//div[@id='pagelet_eduwork' or @id='pagelet_edit_eduwork']//div[@data-pnref='edu']/ul"));
            String val = getEduWorkListItemsVis(edu);
            if (val != null) {
                map.put("education", val);
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "unable to find work pagelet");
        }
        setWaitForElementLoadEnabled(true);
        if (map.isEmpty()) {
            return null;
        }
        workEducationVisCash = map;
        return map;
    }

    protected String getEduWorkListItemsVis(WebElement element) {
        List<WebElement> elements;
        String attrValue = "";
        AttributeVisibility current = AttributeVisibility.SELF;
        try {
            
            for (WebElement elem : element.findElements(By.xpath("./li/div/ul/li[1]/a"))) {
                try {
                    AttributeVisibility v = getAttributeVisibilityForElement(elem);
                    if(current.isStrongerThan(v)) current = v;
                    
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "unable to get the education/work url");
                }
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "unable to get educations/works");
        }
        attrValue = current.name();
        return !attrValue.isEmpty() ? attrValue : null;
    }
}
