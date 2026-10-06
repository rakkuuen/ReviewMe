package Model.Library;

// One achievement of a Steam game. unlockedAt is Steam's raw epoch seconds
public class Achievement {
    private int appId;
    private String apiName, title, description, iconUrl, iconGrayUrl;
    private boolean hidden, unlocked;
    private Double globalPercent;
    private Long unlockedAt;

    public Achievement(int appId, String apiName){
        this.appId = appId;
        this.apiName = apiName;
    }

    public int GetAppId(){ return appId; }
    public String GetApiName(){ return apiName; }

    public String GetTitle(){ return title; }
    public void SetTitle(String title){ this.title = title; }

    public String GetDescription(){ return description; }
    public void SetDescription(String description){ this.description = description; }

    public boolean IsHidden(){ return hidden; }
    public void SetHidden(boolean hidden){ this.hidden = hidden; }

    public String GetIconUrl(){ return iconUrl; }
    public void SetIconUrl(String iconUrl){ this.iconUrl = iconUrl; }

    public String GetIconGrayUrl(){ return iconGrayUrl; }
    public void SetIconGrayUrl(String iconGrayUrl){ this.iconGrayUrl = iconGrayUrl; }

    public Double GetGlobalPercent(){ return globalPercent; }
    public void SetGlobalPercent(Double globalPercent){ this.globalPercent = globalPercent; }

    public boolean IsUnlocked(){ return unlocked; }
    public void SetUnlocked(boolean unlocked){ this.unlocked = unlocked; }

    public Long GetUnlockedAt(){ return unlockedAt; }
    public void SetUnlockedAt(Long unlockedAt){ this.unlockedAt = unlockedAt; }
}
