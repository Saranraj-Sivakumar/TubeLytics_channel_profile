package controllers;

import play.mvc.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.YouTubeService;
import models.ChannelProfileService;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.StreamSupport;

import javax.inject.Inject;

public class HomeController extends Controller {

    private final YouTubeService youTubeService;
    private final ChannelProfileService channelProfileService;
    private final List<JsonNode> searchResults = new LinkedList<>();
    private static final int MAX_QUERIES = 10;

    @Inject
    public HomeController(YouTubeService youTubeService, ChannelProfileService channelProfileService) {
        this.youTubeService = youTubeService;
        this.channelProfileService = channelProfileService;
    }

    // Existing search functionality
    public CompletionStage<Result> search(String query) {
        return youTubeService.fetchVideos(query).thenApply(json -> {
            addSearchResult(json);

            // Calculate the average readability scores
            double avgFleschKincaidGrade = roundToTwoDecimals(calculateAverage(json, "fkGrade"));
            double avgFleschReadingEase = roundToTwoDecimals(calculateAverage(json, "readingEase"));

            // Add the averages to the JSON response
            ObjectNode resultWithAverages = json.deepCopy();
            resultWithAverages.put("avgFleschKincaidGrade", avgFleschKincaidGrade);
            resultWithAverages.put("avgFleschReadingEase", avgFleschReadingEase);

            return ok(play.libs.Json.toJson(resultWithAverages));
        });
    }

    // Calculate the average readability scores
    private double calculateAverage(JsonNode json, String scoreField) {
        return StreamSupport.stream(json.get("items").spliterator(), false)
                .mapToDouble(item -> item.get("snippet").get(scoreField).asDouble())
                .average()
                .orElse(0.0);
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private void addSearchResult(JsonNode newResult) {
        searchResults.add(0, newResult);
        if (searchResults.size() > MAX_QUERIES) {
            searchResults.remove(searchResults.size() - 1);
        }
    }

    // Home page rendering
    public Result index() {
        return ok(views.html.index.render("TubeLytics - YouTube Search"));
    }

    // New method to handle channel profile requests
    public CompletionStage<Result> channelProfile(String id) {
        return channelProfileService.fetchChannelProfile(id).thenApply(profileData -> {
            // Extracting "channelInfo" and "videos" JsonNodes from the combined profile data
            JsonNode channelInfo = profileData.get("channelInfo");
            JsonNode videos = profileData.get("videos");

            // Convert videos JsonNode to List<JsonNode> if necessary for rendering
            List<JsonNode> videoList = new LinkedList<>();
            videos.forEach(videoList::add);

            // Render the channel profile view with channelInfo and videoList
            return ok(views.html.ChannelProfile.render(channelInfo, videoList));
        });
    }
}
