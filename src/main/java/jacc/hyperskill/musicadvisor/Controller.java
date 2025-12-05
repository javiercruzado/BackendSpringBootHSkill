package jacc.hyperskill.musicadvisor;

import com.google.gson.Gson;
import jacc.hyperskill.musicadvisor.model.CategoryItem;
import jacc.hyperskill.musicadvisor.model.Item;
import jacc.hyperskill.musicadvisor.model.PlayListItem;
import jacc.hyperskill.musicadvisor.model.response.*;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static jacc.hyperskill.musicadvisor.server.InterfaceConstants.*;

public class Controller {

    private final int pageSize;
    private String accessToken;
    private final String resourceUrl;

    Controller(int pageSize, String resourceUrl) {
        this.pageSize = pageSize;
        this.resourceUrl = resourceUrl;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    private ArrayList<Item> newItems = new ArrayList<>();

    public int loadNewItems() {
        var data = getDataFromAPI(accessToken, resourceUrl + "/v1/browse/new-releases");
        var newReleasesData = new Gson().fromJson(data, NewInSpotifyResponse.class);
        if (newReleasesData != null && newReleasesData.albums != null) {
            newItems = newReleasesData.albums.items;
        }
        return newItems.size();
    }

    public List<Item> getNewItems(int page) {
        return newItems.stream().skip((long) (page - 1) * pageSize).limit(pageSize).toList();
    }

    private ArrayList<PlayListItem> featuredItems = new ArrayList<>();

    public int loadFeaturedItems() {
        var data = getDataFromAPI(accessToken, resourceUrl + "/v1/browse/featured-playlists");
        var featuredItemsData = new Gson().fromJson(data, FeaturedInSpotifyResponse.class);
        if (featuredItemsData != null && featuredItemsData.playlists != null) {
            featuredItems = featuredItemsData.playlists.items;
        }
        return featuredItems.size();
    }

    public List<PlayListItem> getFeaturedItems(int page) {
        return featuredItems.stream().skip((long) (page - 1) * pageSize).limit(pageSize).toList();
    }

    private List<CategoryItem> categories = new ArrayList<>();

    public int loadCategories() {
        var data = getDataFromAPI(accessToken, resourceUrl + "/v1/browse/categories");
        var categoriesData = new Gson().fromJson(data, CategoriesInSpotifyResponse.class);
        if (categoriesData != null && categoriesData.categories != null) {
            categories = categoriesData.categories.items;
            categories.sort(Comparator.comparing(CategoryItem::getName).reversed());
        }
        return categories.size();
    }

    public List<CategoryItem> getCategories(int page) {
        return categories.stream().skip((long) (page - 1) * pageSize).limit(pageSize).toList();
    }

    public String getCategoryId(String category) {
        return categories.stream().filter(i -> i.name.equals(category)).findFirst().orElse(new CategoryItem()).id;
    }

    public List<PlayListItem> playListItems = new ArrayList<>();

    public int loadPlayLists(String categoryId) {
        String apiPath = resourceUrl + String.format("/v1/browse/categories/%s/playlists", categoryId);
        var data = getDataFromAPI(accessToken, apiPath);
        var playListData = new Gson().fromJson(data, PlayListResponse.class);
        if (playListData != null && playListData.playlists != null) {
            playListItems = playListData.playlists.items;
        } else {
            var errorResponse = new Gson().fromJson(data, ErrorResponse.class);
            if (errorResponse != null) {
                System.out.println(errorResponse.error.message);
            }
        }
        return playListItems.size();
    }

    public List<PlayListItem> getPlayListItems(int page) {
        return playListItems.stream().skip((long) (page - 1) * pageSize).limit(pageSize).toList();
    }

    private static String getDataFromAPI(String accessToken, String apiPath) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .uri(URI.create(apiPath))
                .GET()
                .build();
        try (HttpClient client = HttpClient.newBuilder().build()) {
            HttpResponse<String> response =
                    client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException | InterruptedException e) {
            System.out.printf("Exception %s", e.getMessage());
        }
        return "";
    }

    public static String getAuthToken(String code, String tokenUrl) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(tokenUrl))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Authorization", "Basic " + Base64.getEncoder()
                        .encodeToString((CLIENT_ID + ":" + CLIENT_SECRET).getBytes()))
                .POST(HttpRequest.BodyPublishers.ofString(
                        "grant_type=authorization_code&code=" + code +
                                "&redirect_uri=" + URLEncoder.encode(APP_REDIRECT_URL, StandardCharsets.UTF_8)))
                .build();

        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newBuilder().build()) {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();

            System.out.println("Success!");

            SpotifyTokenResponse tokenResponse =
                    new Gson().fromJson(responseBody, SpotifyTokenResponse.class);
            if (tokenResponse != null && !"".equals(tokenResponse.getAccess_token())) {
                return tokenResponse.getAccess_token();

            } else {
                System.out.println("Error getting token!");
            }
        } catch (IOException | InterruptedException e) {
            System.out.printf("Exception %s", e.getMessage());
        }
        return "";
    }
}