package jacc.hyperskill.musicadvisor.model;

import java.util.ArrayList;

public class PlayListItem {
    public boolean collaborative;
    public String description;
    public ExternalUrls external_urls;
    public String href;
    public String id;
    public ArrayList<Image> images;
    public String name;
    public Owner owner;
    public String snapshot_id;
    public Tracks tracks;
    public String type;
    public String uri;

    public String toString() {
        return String.format("%s%n%s%n", name, external_urls.spotify);
    }
}
