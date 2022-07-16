package team.javaee.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EditChartDTO {
    private Integer songId;
    private String uploaderId;
    private String songName;
    private String songWriter;
    private String songUrl;
    private String defaultBackground;
    private String songCover;
    private String loadingText;
    private String loadedText;
    private Float chartConstant;
}
