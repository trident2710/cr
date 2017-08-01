/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inria.crawlerv2.provider;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import inria.crawlerv2.driver.AttributeVisibility;
import inria.crawlerv2.driver.BasicFacebookPageDriver;
import inria.crawlerv2.driver.FacebookPageInformationDriver;
import inria.crawlerv2.driver.FacebookSelfPageInformationDriver;
import static inria.crawlerv2.provider.AttributeProvider.FIND_ID_URL;
import static inria.crawlerv2.provider.AttributeProvider.TARGET_WITH_ID_TEMPLATE;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author adychka
 */
public class FacebookAttributeVisibilityProvider implements AttributeProvider{
    
    private static final Logger LOG = Logger.getLogger(FacebookAttributeProvider.class.getName());
    private final FacebookSelfPageInformationDriver fpid;
    private URI url;
    private final int maxItemsToCollect = 10;
    
    public FacebookAttributeVisibilityProvider(URI url, FacebookSelfPageInformationDriver driver) {
        this.url = url;
        this.fpid = driver;
    }

    @Override
    public boolean loginWithCredentials(String username, String password) {
        return this.fpid.start(username, password);
    }

    @Override
    public void getAttributeAsync(AttributeName name, AttributeCallback callback) {
        JsonElement response = null;
        Object[] val = null;
        try {
            val = getAttributeByName(name);
        } catch (BasicFacebookPageDriver.PageNotFoundException | IllegalArgumentException e) {
        }

        if (val == null) {
            callback.onError(name, "unable to get response");
            return;
        }
        if (val[0] instanceof String) {
            String v = (String) val[0];
            if (!v.isEmpty()) {
                response = new JsonPrimitive(v);
            }
        }
        if (val[0] instanceof List) {
            List<String> v = (List<String>) val[0];
            if (!v.isEmpty()) {
                JsonArray array = new JsonArray();
                v.forEach((s) -> {
                    array.add(s);
                });
                response = array;
            }
        }
        if (response == null) {
            callback.onError(name, "unable to get response");
            return;
        }
        JsonObject res = new JsonObject();
        res.add("value", response);
        res.addProperty("visibility", (String)val[1]);
        callback.onAttributeCollected(name, res);
    }

    @Override
    public JsonElement getAttribute(AttributeName name) throws CollectException {
        JsonElement response = null;
        Object[] val = null;
        try {
            val = getAttributeByName(name);
        } catch (BasicFacebookPageDriver.PageNotFoundException | IllegalArgumentException e) {
            e.printStackTrace();
        }

        if (val == null) {
            throw new CollectException(name);
        }
        if (val[0] instanceof String) {
            String v = (String) val[0];
            if (!v.isEmpty()) {
                response = new JsonPrimitive(v);
            }
        }
        if (val[0] instanceof List) {
            List<String> v = (List<String>) val[0];
            if (!v.isEmpty()) {
                JsonArray array = new JsonArray();
                v.forEach((s) -> {
                    array.add(s);
                });
                response = array;
            }
        }
        if (response == null) {
            throw new CollectException(name);
        }
        JsonObject res = new JsonObject();
        res.add("value", response);
        res.addProperty("visibility", (String)val[1]);
        return res;
    }

    @Override
    public void transformTargetWithId(String id) {
        try {
            this.url = new URI(TARGET_WITH_ID_TEMPLATE + id);
            this.fpid.setTarget(url);
        } catch (URISyntaxException ex) {
        }
    }

    @Override
    public void finishSession() {
        fpid.finish();
    }
    
    private Object[] getAttributeByName(AttributeName name) {
        switch (name) {
            case ID:
                return new Object[]{collectId(),AttributeVisibility.PUBLIC.name()};
            case FIRST_NAME:
                return new Object[]{getFirstName(),AttributeVisibility.PUBLIC.name()};
            case LAST_NAME:
                return new Object[]{getLastName(),AttributeVisibility.PUBLIC.name()};
            case BIRTHDAY:
                return new Object[]{fpid.getBirthday(),fpid.getBirthdayVisibility()};
            case RELIGIOUS_VIEW:
                return new Object[]{fpid.getReligion(),fpid.getReligionVisibility()};
            case POLITICAL_VIEW:
                return new Object[]{fpid.getPolitic(),fpid.getPoliticVisibility()};
            case GENDER:
                return new Object[]{fpid.getGender(),AttributeVisibility.PUBLIC.name()};
            case GENDER_INTERESTS:
                return new Object[]{fpid.getGenderInterest(),fpid.getGenderInterestVisibility()};
            case LANGUAGES:
                return new Object[]{fpid.getLanguages(),fpid.getLanguagesVisibility()};
            case PHONES:
                return new Object[]{fpid.getMobilePhones(),fpid.getMobilePhonesVisibility()};
            case ADDRESS:
                return new Object[]{fpid.getAddress(),fpid.getAddressVisibility()};
            case EMAIL_ADDRESS:
                return new Object[]{fpid.getEmailAddress(),fpid.getEmailAddressVisibility()};
            case WORK_IDS:
                return new Object[]{getPageIdsFromUrls(fpid.getWorks()),fpid.getWorksVisibility()};
            case EDUCATION_IDS:
                return new Object[]{getPageIdsFromUrls(fpid.getEducations()),fpid.getEducationsVisibility()};
            default:
                return null;
        }
    }
    
    private String collectId() {
        return collectId(url.toString());
    }

    private String collectId(String url) {
        LOG.log(Level.INFO, "collecting the id for the page: {0}", url);
        //if there is already id in url
        if (url.contains("id=")) {
            return url.split("&")[0].split("=")[1];
        }
        url = url.split(Pattern.quote("?"))[0];

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("url", url);
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.postForObject(FIND_ID_URL, map, String.class);
        try {
            return new JsonParser().parse(response).getAsJsonObject().get("id").getAsString();
        } catch (JsonSyntaxException | IllegalStateException e) {
            LOG.log(Level.WARNING, "unable to collect id for the profile: {0}", url);
        }
        return null;
    }
    
    private String getFirstName() {
        String name = fpid.getName();
        return name != null ? name.split(" ")[0] : name;
    }

    private String getLastName() {
        String name = fpid.getName();
        if (name == null) {
            return null;
        }
        String[] items = name.split(" ");
        if (items.length == 1) {
            return null;
        }
        return String.join(" ", Arrays.copyOfRange(items, 1, items.length));
    }
    
     private List<String> getPageIdsFromUrls(List<String> pageUrls) {
        if (pageUrls == null || pageUrls.isEmpty()) {
            return null;
        }
        Collections.shuffle(pageUrls);
        pageUrls = pageUrls.subList(0, Math.min(maxItemsToCollect, pageUrls.size()));
        List<String> ids = new ArrayList<>();
        pageUrls.forEach((u) -> {
            try {
                ids.add(collectId(u));
            } catch (Exception e) {
                ids.add(u);
                LOG.log(Level.WARNING, "unable to collect id for the page: {0}", u);
            }
        });
        return ids;
    }
    
}
