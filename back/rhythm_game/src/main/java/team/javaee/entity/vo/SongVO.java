package team.javaee.entity.vo;

import lombok.Data;

@Data
public class SongVO {
    private String songName;
    private Integer songId;
    private String uploaderId;
    private String songWriter;
    private String songCover;
    private String loadingText;
    private Integer notesCount;
    private Integer songLength;
    private String defaultBackground;
    private String songUrl;
    private String status;
    private String chartConstant;
    private String loadedText;
}
