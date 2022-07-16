package team.javaee.entity.vo;

import lombok.Data;

@Data
public class SongInfoVO {
    private String songName;
    private String uploader;
    private String songCover;
    private String songWriter;
    private String songDifficulty;
    private String songId;
    private String defaultBackground;
    private String chartConstant;
    private String loadedText;
    private String loadingText;
    public String status;
}
