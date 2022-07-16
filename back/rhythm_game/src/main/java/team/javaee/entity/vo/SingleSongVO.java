package team.javaee.entity.vo;

import io.swagger.models.auth.In;
import lombok.Data;

import java.util.List;

@Data
public class SingleSongVO {
    private String songName;
    private Integer songId;
    private String uploader;
    private String songWriter;
    private String songCover;
    private String loadingText;
    private String loadedText;
    private Integer notesCount;
    private Integer songLength;
    private String defaultBackground;
    private String songUrl;
    private Float BPM;
    private Integer firstBeatDelay;
    private List<ChangeBackgroundOperationsVO> changeBackgroundOperations;
    private List<TracksVO> tracks;
}
