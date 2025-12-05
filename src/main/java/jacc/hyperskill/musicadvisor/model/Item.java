package jacc.hyperskill.musicadvisor.model;

import java.util.ArrayList;
import java.util.Arrays;

public class Item {
    public String album_type;
    public ArrayList<Artist> artists;
    public ArrayList<String> available_markets;
    public ExternalUrls external_urls;
    public String href;
    public String id;
    public ArrayList<Image> images;
    public String name;
    public String release_date;
    public String release_date_precision;
    public int total_tracks;
    public String type;
    public String uri;


    @Override
    public String toString() {
        String artistString = Arrays.toString(artists.stream().map(x -> x.name).toArray());
        return String.format("%s%n%s%n%s%n", name, artistString, external_urls.spotify);
    }
}
