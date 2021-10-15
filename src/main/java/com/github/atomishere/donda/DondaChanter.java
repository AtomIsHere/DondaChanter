package com.github.atomishere.donda;

import de.umass.lastfm.Authenticator;
import de.umass.lastfm.Caller;
import de.umass.lastfm.Session;
import de.umass.lastfm.Track;
import de.umass.lastfm.scrobble.ScrobbleData;
import de.umass.lastfm.scrobble.ScrobbleResult;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DondaChanter {
    private final String apiKey;
    private final String apiSecret;

    private final ScrobbleData dondaScrobble = new ScrobbleData("Kanye West", "Donda Chant", -1, -1, "Donda", "Kanye West", null, 1, null);

    private Session session;

    public DondaChanter(String apiKey, String apiSecret) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    public void login(String username, String password) {
        session = Authenticator.getMobileSession(username, password, apiKey, apiSecret);
    }

    public void donda() {
        int now = (int) (System.currentTimeMillis() / 1000);
        dondaScrobble.setTimestamp(now);
        ScrobbleResult result = Track.scrobble(dondaScrobble, session);
        System.out.println("ok: " + (result.isSuccessful() && !result.isIgnored()));
    }

    public static void main(String[] args) {
        File configFile = new File("config.properties");
        if(!configFile.exists()) {
            try(InputStream in = DondaChanter.class.getClassLoader().getResourceAsStream("config.properties")) {
                Files.copy(Objects.requireNonNull(in), configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Config template copied from jar file, please fill out the config file then run the program again.");
            } catch (IOException e) {
                e.printStackTrace();
            }

            return;
        }

        Properties config = new Properties();
        try(Reader reader = new FileReader(configFile)) {
            config.load(reader);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        DondaChanter dondaChanter = loadConfig(config);
        int delay = Integer.parseInt(config.getProperty("delay", "2"));

        final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(dondaChanter::donda, delay, delay, TimeUnit.SECONDS);
    }

    public static DondaChanter loadConfig(@NotNull Properties properties) {
        Caller.getInstance().setUserAgent(properties.getProperty("userAgent", "tst"));

        DondaChanter donda = new DondaChanter(properties.getProperty("apiKey"), properties.getProperty("apiSecret"));
        donda.login(properties.getProperty("username"), properties.getProperty("password"));

        return donda;
    }
}
