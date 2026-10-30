package Model.Reviews;

// One change to a review's identity: its title, its Steam link, or both. Build one with Link, Unlink, Rename or LinkAndRename
public class SteamLinkChange {
    private int reviewId;
    private String newTitle;
    private boolean setsLink;
    private Integer steamAppId;

    private SteamLinkChange(int reviewId, String newTitle, boolean setsLink, Integer steamAppId){
        this.reviewId = reviewId;
        this.newTitle = newTitle;
        this.setsLink = setsLink;
        this.steamAppId = steamAppId;
    }

    public static SteamLinkChange Link(int reviewId, int steamAppId){ return new SteamLinkChange(reviewId, null, true, steamAppId); }
    public static SteamLinkChange Unlink(int reviewId){ return new SteamLinkChange(reviewId, null, true, null); }
    public static SteamLinkChange Rename(int reviewId, String newTitle){ return new SteamLinkChange(reviewId, newTitle, false, null); }
    public static SteamLinkChange LinkAndRename(int reviewId, int steamAppId, String newTitle){ return new SteamLinkChange(reviewId, newTitle, true, steamAppId); }

    // The same change with a different new title
    public SteamLinkChange WithNewTitle(String title){ return new SteamLinkChange(reviewId, title, setsLink, steamAppId); }

    public int GetReviewId(){ return reviewId; }

    // Null when the title stays as it is
    public String GetNewTitle(){ return newTitle; }

    // Whether the Steam link is being set (to GetSteamAppId, which is null for removing it) rather than left alone
    public boolean SetsLink(){ return setsLink; }
    public Integer GetSteamAppId(){ return steamAppId; }
}
