/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inria.crawlerv2.provider;

/**
 * declares the requirement for the crawler, which fields it should collect from
 * the account
 *
 * @author adychka
 */
public enum AttributeName {
    ID,
    FIRST_NAME,
    LAST_NAME,
    FRIEND_IDS,
    BIRTH_DATE,
    BIRTH_YEAR,
    GENDER,
    GENDER_INTERESTS,
    POLITICAL_VIEW,
    RELIGIOUS_VIEW,
    LANGUAGES,
    PHONES,
    ADDRESS,
    EMAIL_ADDRESS,
    WORK_IDS,
    EDUCATION_IDS;

    public String getName() {
        return this.name().toLowerCase();
    }
}
