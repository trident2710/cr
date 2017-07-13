/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inria.crawlerv2.engine.account;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import inria.crawlerv2.utils.FileUtils;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author adychka
 */
public class AccountManagerImpl implements AccountManager {

    private String file;
    private List<Account> accounts;
    private static final Logger LOG = Logger.getLogger(AccountManager.class.getName());

    public AccountManagerImpl(String file) {
        this.file = file;
    }

    @Override
    public List<Account> getAllAccounts() {
        if (accounts == null) {
            loadAccounts();
        }
        return accounts;
    }

    @Override
    public List<Account> getBannedAccounts() {
        return getByIsBanned(true);
    }

    @Override
    public List<Account> getWorkingAccounts() {
        return getByIsBanned(false);
    }

    @Override
    public Account getRandomWorkingAccount() {
        return getRandomWorking();
    }

    @Override
    public Account addAccount(String login, String password) {
        if (accounts == null) {
            loadAccounts();
        }
        Account a = new Account(login, password, true);
        accounts.add(a);
        save();
        return a;
    }

    @Override
    public void removeAccount(Account account) {
        if (accounts == null) {
            loadAccounts();
        }
        accounts.remove(account);
        save();
    }

    @Override
    public void save() {
        if (accounts == null) {
            return;
        }
        JsonArray array = new JsonArray();

        accounts.stream().map((e) -> {
            JsonObject object = new JsonObject();
            object.addProperty("login", e.getLogin());
            object.addProperty("password", e.getPassword());
            object.addProperty("isBlocked", e.isBanned());
            return object;
        }).forEachOrdered((object) -> {
            array.add(object);
        });
        JsonWriter writer;
        try {
            FileUtils.writeObjectToFile("/", file, array);
            loadAccounts();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "unable to write to file", ex);
        }
    }

    private void loadAccounts() {
        try {
            accounts = new ArrayList<>();
            JsonArray array;
            array = new JsonParser().parse(new FileReader(file)).getAsJsonArray();
            for (int i = 0; i < array.size(); i++) {
                JsonObject object = array.get(i).getAsJsonObject();
                Account a = new Account(object.get("login").getAsString(), object.get("password").getAsString(), object.get("isBlocked").getAsBoolean());
                accounts.add(a);
            }
        } catch (FileNotFoundException ex) {
            LOG.log(Level.WARNING, "file does not exist, creating the new file in the root", ex);
            file = "login_accounts.json";

            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("[]");
                writer.flush();
                writer.close();
                loadAccounts();
            } catch (FileNotFoundException ex1) {
                LOG.log(Level.SEVERE, "unable to create new file", ex1);
            }
        }
    }

    private List<Account> getByIsBanned(boolean requirement) {
        if (accounts == null) {
            loadAccounts();
        }
        return accounts.stream()
                .filter(a -> a.isBanned() == requirement)
                .collect(Collectors.toList());
    }

    private Account getRandomWorking() {
        if (accounts == null) {
            loadAccounts();
        }
        List<Account> working = getByIsBanned(false);
        if (working.isEmpty()) {
            return null;
        }
        return working.get(new Random().nextInt(working.size()));
    }
}
