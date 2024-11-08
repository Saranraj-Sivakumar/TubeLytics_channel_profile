package models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;

public class ChannelProfileService {

    private final YouTubeService youTubeService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    public ChannelProfileService(YouTubeService youTubeService) {
        this.youTubeService = youTubeService;
    }

    public CompletionStage<JsonNode> fetchChannelProfile(String channelId) {
        CompletionStage<JsonNode> channelDetails = youTubeService.fetchChannelDetails(channelId);
        CompletionStage<JsonNode> channelVideos = youTubeService.fetchChannelVideos(channelId);

        return channelDetails.thenCombine(channelVideos, (details, videos) -> {
            for (JsonNode video : videos) {
                // Add the videoId attribute based on conditions
                String videoId = video.path("id").path("videoId").asText(null);
                if (videoId == null || videoId.isEmpty()) {
                    videoId = video.path("snippet").path("resourceId").path("videoId").asText("N/A");
                }
                ((ObjectNode) video).put("videoId", videoId);
            }

            return createCombinedProfileJson(details, videos);
        });
    }

    private JsonNode createCombinedProfileJson(JsonNode channelInfo, JsonNode videoItems) {
        ObjectNode profileJson = objectMapper.createObjectNode();
        profileJson.set("channelInfo", channelInfo);
        profileJson.set("videos", videoItems);
        return profileJson;
    }
}
