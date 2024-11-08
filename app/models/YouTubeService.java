package models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.api.Configuration;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

public class YouTubeService {

    private static final Logger logger = Logger.getLogger(YouTubeService.class.getName());
    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    public YouTubeService(Configuration config) {
        this.apiKey = config.underlying().getString("youtube.api.key");
        this.httpClient = HttpClient.newHttpClient();
    }

    // Fetches channel details (unchanged for readability functions)
    public CompletionStage<JsonNode> fetchChannelDetails(String channelId) {
        String url = "https://www.googleapis.com/youtube/v3/channels?part=snippet,statistics&id=" + channelId + "&key=" + apiKey;
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(responseBody -> {
                    try {
                        JsonNode json = objectMapper.readTree(responseBody);
                        return json.has("items") ? json.get("items").get(0) : null;
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Error parsing channel details", e);
                        return null;
                    }
                });
    }

    // Fetches the latest 10 videos for the channel
    public CompletionStage<JsonNode> fetchChannelVideos(String channelId) {
        String url = "https://www.googleapis.com/youtube/v3/search?part=snippet&channelId=" + channelId + "&maxResults=10&order=date&type=video&key=" + apiKey;
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(responseBody -> {
                    try {
                        JsonNode json = objectMapper.readTree(responseBody);
                        return json.has("items") ? json.get("items") : null;
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Error fetching channel videos", e);
                        return null;
                    }
                });
    }

    // Description readability function for search results (unchanged)
    public CompletionStage<JsonNode> fetchVideos(String query) {
        String url = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=10&q=" + query + "&key=" + apiKey;
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(this::parseResponse);
    }

    private JsonNode parseResponse(String responseBody) {
        JsonNode json = null;
        try {
            json = objectMapper.readTree(responseBody);
            if (json != null && json.has("items")) {
                for (JsonNode item : json.get("items")) {
                    addReadabilityScores(item);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error parsing response", e);
        }
        return json;
    }

    private void addReadabilityScores(JsonNode item) {
        String description = item.get("snippet").get("description").asText();
        double fkGrade = calculateFleschKincaidGrade(description);
        double readingEase = calculateFleschReadingEase(description);

        ((ObjectNode) item.get("snippet")).put("fkGrade", fkGrade);
        ((ObjectNode) item.get("snippet")).put("readingEase", readingEase);
    }

    private double calculateFleschKincaidGrade(String text) {
        int wordCount = countWords(text);
        int sentenceCount = countSentences(text);
        int syllableCount = countSyllablesInText(text);

        if (wordCount == 0 || sentenceCount == 0) {
            return 0;
        }

        double gradeLevel = 0.39 * ((double) wordCount / sentenceCount) + 11.8 * ((double) syllableCount / wordCount) - 15.59;
        return Math.round(gradeLevel * 100.0) / 100.0;
    }

    private double calculateFleschReadingEase(String text) {
        int wordCount = countWords(text);
        int sentenceCount = countSentences(text);
        int syllableCount = countSyllablesInText(text);

        if (wordCount == 0 || sentenceCount == 0) {
            return 0;
        }

        double readingEase = 206.835 - 1.015 * ((double) wordCount / sentenceCount) - 84.6 * ((double) syllableCount / wordCount);
        return Math.round(readingEase * 100.0) / 100.0;
    }

    private int countWords(String text) {
        String[] words = text.split("\\s+");
        return words.length;
    }

    private int countSentences(String text) {
        String[] sentences = text.split("[.!?]");
        return sentences.length;
    }

    private int countSyllables(String word) {
        int count = 0;
        boolean lastWasVowel = false;
        String vowels = "aeiouy";
        word = word.toLowerCase();

        for (char wc : word.toCharArray()) {
            if (vowels.indexOf(wc) != -1) {
                if (!lastWasVowel) {
                    count++;
                }
                lastWasVowel = true;
            } else {
                lastWasVowel = false;
            }
        }

        if (word.endsWith("e")) {
            count--;
        }
        return Math.max(count, 1);
    }

    private int countSyllablesInText(String text) {
        int syllableCount = 0;
        String[] words = text.split("\\s+");
        for (String word : words) {
            syllableCount += countSyllables(word);
        }
        return syllableCount;
    }
}
