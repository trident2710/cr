/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inria.crawlerv2.driver;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 *
 * @author adychka
 */
public class FacebookPageInformationDriver extends BasicFacebookPageDriver {

    protected Map<String, String> basicInfoCash;
    protected Map<String, String> contactInfoCash;
    protected Map<String, String> workEducationCash;
          
    /**
     * maximum amount of friends which will be collected for the profile
     * necessary to avoid the case of huge amount of friends when crawling could take huge amount of time 
     */
    protected final int maxFriends;

    public FacebookPageInformationDriver(URI target, WebDriverOption option, int waitElemSec, int shortWaitMillis, int maxFriends) {
        super(target, option, waitElemSec, shortWaitMillis);
        this.maxFriends = maxFriends;
        initCash();
    }

    @Override
    public boolean start(String username, String password) {
        initCash();
        return super.start(username, password);
    }

    @Override
    public void setTarget(URI target) {
        super.setTarget(target);
        initCash();
    }

    /**
     * get user name consist first name, last name can consist of components >=1
     *
     * @return
     */
    public String getName() {
        loadUrl(null, null);
        try {
            return driver.findElement(By.id("fb-timeline-cover-name")).getText();
        } catch (Exception e) {
            LOG.log(Level.WARNING, "unable to find fb-timeline-cover-name");
        }
        return null;
    }

    /**
     * get the profile birth date
     *
     * @return
     */
    public String getBirthday() {
        String val = getBasicInfoByKey("birthday");
        if(val == null||val.isEmpty()){
            String s = getBasicInfoByKey("date of birth");
            if(s!=null) val+=s;
            s = getBasicInfoByKey("year of birth");
            if(s!=null) val+=" "+s;
        }
        return val;
    }
    

    /**
     * get the profile gender
     *
     * @return
     */
    public String getGender() {
        return getBasicInfoByKey("gender");
    }

    /**
     * get the profile gender interest
     *
     * @return
     */
    public String getGenderInterest() {
        return getBasicInfoByKey("interested in");
    }

    /**
     * get the profile religion
     *
     * @return
     */
    public String getReligion() {
        return getBasicInfoByKey("religious views");
    }

    /**
     * get the profile politic
     *
     * @return
     */
    public String getPolitic() {
        return getBasicInfoByKey("political views");
    }

    /**
     * get the profile languages
     *
     * @return
     */
    public List<String> getLanguages() {
        String val = getBasicInfoByKey("languages");
        return val != null ? Arrays.asList(val.split("\\|")) : null;
    }

    /**
     * get the mobile phones from contact information
     *
     * @return
     */
    public List<String> getMobilePhones() {
        String val = getContactInfoByKey("mobile phones");
        return val != null ? Arrays.asList(val.split("\\|")) : null;
    }

    /**
     * get address from contact information
     *
     * @return
     */
    public List<String> getAddress() {
        String val = getContactInfoByKey("address");
        return val != null ? Arrays.asList(val.split("\\|")) : null;
    }

    /**
     * get address from contact information
     *
     * @return
     */
    public List<String> getEmailAddress() {
        String val = getContactInfoByKey("email");
        return val != null ? Arrays.asList(val.split("\\|")) : null;
    }

    /**
     * get work from edu work information
     *
     * @return
     */
    public List<String> getWorks() {
        String val = getEduWorkInfoByKey("work");
        return val != null ? Arrays.asList(val.split("\\|")).stream().distinct().collect(Collectors.toList()) : null;
    }

    /**
     * get education from edu work information
     *
     * @return
     */
    public List<String> getEducations() {
        String val = getEduWorkInfoByKey("education");
        return val != null ? Arrays.asList(val.split("\\|")).stream().distinct().collect(Collectors.toList()) : null;
    }
    
    /**
     * get the list of URLs of profiles which are the friends of the target
     *
     * @return
     */
    public List<String> getFriends() {
        loadUrl(new String[]{"sk=friends"}, new String[]{"friends"});
        List<String> urls = new ArrayList<>();
        Map<WebElement, Boolean> friends = new HashMap<>();
        boolean isFinished;
        int loadingTries = 0;
        do {
            LOG.log(Level.INFO, "scrolled friends down");
            
            isFinished = true;
            scrollPage();
            waitForLoad();
            randomShortWait();

            try {
                List<WebElement> upd_friends = driver.findElements(By.xpath(".//ul[@data-pnref='friends']"));
                if (upd_friends != null && upd_friends.size() > friends.size()) {
                    setWaitForElementLoadEnabled(false);
                    updateFriendsMap(upd_friends, friends);
                    urls.addAll(collectUrlsFromFriendsMap(friends));
                    isFinished = false;
                    loadingTries=0;
                    setWaitForElementLoadEnabled(true);
                }
            } catch (Exception e) {
                LOG.log(Level.WARNING, "unable to find .//ul[@data-pnref='friends']");
            }
            if(isFinished){
                LOG.log(Level.INFO, "list was not updated, trying once more");
                scrollPageUp();
                loadingTries++;
            }

        } while (loadingTries<3&&urls.size()<maxFriends);
        LOG.log(Level.INFO, "urls size: {0}",urls.size());
        return urls;
    }

    protected String getBasicInfoByKey(String key) {
        Map<String, String> basicInfo = basicInfoCash.isEmpty() ? getBasicInformation() : basicInfoCash;
        return basicInfo != null && basicInfo.containsKey(key) ? basicInfo.get(key).replaceFirst("\\|", "") : null;
    }

    protected String getContactInfoByKey(String key) {
        Map<String, String> contactInfo = contactInfoCash.isEmpty() ? getContactInformation() : contactInfoCash;
        return contactInfo != null && contactInfo.containsKey(key) ? contactInfo.get(key).replaceFirst("\\|", "") : null;
    }

    protected String getEduWorkInfoByKey(String key) {
        Map<String, String> contactInfo = workEducationCash.isEmpty() ? getWorkEducationInformation() : workEducationCash;
        return contactInfo != null && contactInfo.containsKey(key) ? contactInfo.get(key).replaceFirst("\\|", "") : null;
    }

    protected Map<String, String> getContactInformation() {
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
                            val = getMobilePhones(element);
                            if (val != null) {
                                attributes.put("mobile phones", val);
                            }
                            break;
                        case "address":
                            val = getAddress(element);
                            if (val != null) {
                                attributes.put("address", val);
                            }
                            break;
                        case "email address":
                        case "email":
                            val = getEmailAddress(element);
                            if (val != null) {
                                attributes.put("email", val);
                            }
                            break;
                    }
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "unable to get attribute from contact information");
                }
            }

            contactInfoCash = attributes;
            setWaitForElementLoadEnabled(true);
            return attributes;
        } catch (Exception e) {

            LOG.log(Level.WARNING, "unable to contact information");
        }
        setWaitForElementLoadEnabled(true);
        return null;
    }

    protected String getMobilePhones(WebElement element) {
        try {
            String attrValue = "";
            List<WebElement> elements;
            elements = element.findElements(By.xpath("./div/div/div/div/span/ul/li"));
            for (WebElement el : elements) {
                try {
                    attrValue += "|" + el.findElement(By.xpath(".//span")).getText();
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

    protected String getEmailAddress(WebElement element) {
        try {
            String attrValue = "";
            List<WebElement> elements;
            elements = element.findElements(By.xpath("./div/div/div/div/span/ul/li"));
            for (WebElement el : elements) {
                try {
                    String v = (el.findElement(By.xpath("./a/span")).getText());
                    if(v==null||v.isEmpty()) v = (el.findElement(By.xpath("./a/span/span")).getText());
                    if(v==null||v.isEmpty()) v = el.findElement(By.xpath("./a")).getAttribute("href").replace("%40", "@").replace(":mailto", "");
                    attrValue += "|" + v;
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "unable to get email from basic info");
                }
            }
            if(attrValue.isEmpty()){
                elements = element.findElements(By.xpath("./div/div/div/div/span/ul/li/ul/li"));
                for (WebElement el : elements) {
                    try {
                        String v = (el.findElement(By.xpath("./a/span/span")).getText());
                        if(v==null||v.isEmpty()) v = el.findElement(By.xpath("./a")).getAttribute("href").replace("%40", "@").replace(":mailto", "");
                        attrValue += "|" + v;
                    } catch (Exception e) {
                        LOG.log(Level.WARNING, "unable to get email from basic info");
                    }
                }
            }
            return !attrValue.isEmpty() ? attrValue : null;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "unable to find email");
        }
        return null;
    }

    protected String getAddress(WebElement element) {
        try {
            String attrValue = "";
            List<WebElement> elements;
            elements = element.findElements(By.xpath("./div/div/div/div/span/span/ul/li"));
            for (WebElement el : elements) {
                try {
                    attrValue += "|" + (el.getText());
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "unable to get address from basic info");
                }
            }
            return !attrValue.isEmpty() ? attrValue : null;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "unable to find address");
        }
        return null;
    }

    protected void updateFriendsMap(List<WebElement> list, Map<WebElement, Boolean> map) {
        for (WebElement element : list) {
            if (!map.containsKey(element)) {
                map.put(element, false);
            }
        }
    }

    protected List<String> collectUrlsFromFriendsMap(Map<WebElement, Boolean> map) {
        List<String> urls = new ArrayList<>();
        for (Map.Entry<WebElement, Boolean> e : map.entrySet()) {
            if (!e.getValue()) {
                urls.addAll(getFriendUrlsFromContainer(e.getKey()));
                e.setValue(true);
            }
        }
        return urls;
    }

    protected List<String> getFriendUrlsFromContainer(WebElement container) {
        List<String> elems = new ArrayList<>();
        List<WebElement> elements;
        try {
            elements = container.findElements(By.xpath("./li"));
        } catch (Exception e) {
            LOG.log(Level.WARNING, "unable to find li in continer");
            return elems;
        }
        for (WebElement element : elements) {
            try {
                String ref = element.findElement(By.xpath("./div[@data-testid='friend_list_item']/a")).getAttribute("href");
                elems.add(ref);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "unable to find a");
            }
        }
        return elems;
    }

    protected Map<String, String> getBasicInformation() {
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
                String attrValue = "";
                for (WebElement e : element.findElements(By.xpath("./div/div/div/div/span"))) {
                    if (!e.getText().equals("·") && !e.getText().equals("")) {
                        attrValue += "|" + e.getText();
                    }
                }
                map.put(attrName, attrValue);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "unable to get attribute from basic info");
            }
        }
        basicInfoCash = map;
        setWaitForElementLoadEnabled(true);
        return map;
    }

    protected Map<String, String> getWorkEducationInformation() {
        loadUrl(new String[]{"sk=about", "section=education"}, new String[]{"education"});
        setWaitForElementLoadEnabled(false);
        Map<String, String> map = new HashMap<>();
        List<WebElement> elements;
        try {
            WebElement work = driver.findElement(By.xpath(".//div[@id='pagelet_eduwork' or @id='pagelet_edit_eduwork']//div[@data-pnref='work']/ul"));
            String val = getEduWorkListItems(work);
            if (val != null) {
                map.put("work", val);
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "unable to find work pagelet");
        }
        try {
            WebElement edu = driver.findElement(By.xpath(".//div[@id='pagelet_eduwork' or @id='pagelet_edit_eduwork']//div[@data-pnref='edu']/ul"));
            String val = getEduWorkListItems(edu);
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
        workEducationCash = map;
        return map;
    }

    protected String getEduWorkListItems(WebElement element) {
        List<WebElement> elements;
        String attrValue = "";
        try {
            for (WebElement elem : element.findElements(By.xpath("./li//a[1]"))) {
                try {
                    attrValue += "|" + elem.getAttribute("href");
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "unable to get the education/work url");
                }
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "unable to get educations/works");
        }
        return !attrValue.isEmpty() ? attrValue : null;
    }

    protected void initCash() {
        basicInfoCash = new HashMap<>();
        contactInfoCash = new HashMap<>();
        workEducationCash = new HashMap<>();
    }

}
