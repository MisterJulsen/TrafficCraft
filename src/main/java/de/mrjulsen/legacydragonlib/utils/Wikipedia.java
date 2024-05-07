package de.mrjulsen.legacydragonlib.utils;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import de.mrjulsen.legacydragonlib.DragonLibConstants;
import de.mrjulsen.trafficcraft.ModMain;

public class Wikipedia {

    protected static final String WIKIPEDIA_BASE_URL = "https://www.wikipedia.org/";

    protected static final Map<String, WikipediaArticle> articles = new HashMap<>();

    public static void addArticle(String... articleId) {
        for (String id : articleId) {
            articles.put(id, new WikipediaArticle(id));
        }
    }

    public static WikipediaArticle getArticle(String id) {
        return articles.get(id);
    }

    public static Collection<WikipediaArticle> getArticles() {
        return articles.values();
    }

    public static class WikipediaArticle {

        protected boolean loadingFinished = false;
        protected String json;
        protected Map<String, String> articleLanguages = new HashMap<>();
        protected String fallbackUrl = null;

        public WikipediaArticle(String articleId) {
            new Thread(() -> {
                Thread.currentThread().setName("Wikipedia Article Loader");
                try {
                    URL url = new URL(String.format("https://www.wikidata.org/w/api.php?action=wbgetentities&format=json&props=sitelinks&ids=%s", articleId));
                    Scanner scan = new Scanner(url.openStream());
                    String str = "";
                    while (scan.hasNext())
                        str += scan.nextLine();
                    scan.close();
                    extractSiteTitleMap(str, articleId);
                } catch (Exception e) {
                    ModMain.LOGGER.warn("Could not get wikipedia article data.", e);
                }
                loadingFinished = true;
            }).start();
        }

        protected void extractSiteTitleMap(String jsonData, String entityId) {
            JsonObject jsonObject = DragonLibConstants.GSON.fromJson(jsonData, JsonObject.class);

            JsonObject entities = jsonObject.getAsJsonObject("entities");
            JsonObject entity = entities.getAsJsonObject(entityId);
            JsonObject sitelinks = entity.getAsJsonObject("sitelinks");

            Map<String, String> siteTitleMap = new HashMap<>();

            Set<Map.Entry<String, JsonElement>> entries = sitelinks.entrySet();
            for (Map.Entry<String, JsonElement> entry : entries) {
                String site = entry.getKey();
                JsonObject sitelink = entry.getValue().getAsJsonObject();
                String title = sitelink.get("title").getAsString();
                siteTitleMap.put(site, title);
            }

            articleLanguages = siteTitleMap;
        }

        public boolean isLoaded() {
            return loadingFinished;
        }

        public void setFallbackUrl(String fallbackUrl) {
            this.fallbackUrl = fallbackUrl;
        }

        public String getFallbackUrl() {
            return fallbackUrl == null ? WIKIPEDIA_BASE_URL : fallbackUrl;
        }

        public String getArticleUrl(String language) {
            String langId = String.format("%swiki", language);
            String url = getFallbackUrl();
            if (articleLanguages.containsKey(langId)) {                
                url = String.format("https://%s.wikipedia.org/wiki/%s", language, articleLanguages.get(langId));
            } else if (articleLanguages.containsKey("enwiki")) {                
                url = String.format("https://en.wikipedia.org/wiki/%s", language, articleLanguages.get("enwiki"));
            } else if (articleLanguages.size() > 0) {                
                Entry<String, String> firstEntry = articleLanguages.entrySet().stream().findFirst().get();
                url = String.format("https://%s.wikipedia.org/wiki/%s", firstEntry.getKey().replace("wiki", ""), firstEntry.getValue());
            }
            
            return url.replace(" ", "_");
        }
    }
}
