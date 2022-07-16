package team.javaee.entity.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class TracksVO {
    private Integer id;
    private Integer type;
    private String key;
    private Integer startTiming;
    private Integer endTiming;
    private String positionX;
    private String width;
    @JsonProperty("R")
    private Integer R;
    @JsonProperty("G")
    private Integer G;
    @JsonProperty("B")
    private Integer B;
    List<NotesVO> notes;
    List<MoveOperationsVO> moveOperations;
    List<ChangeWidthOperationsVO> changeWidthOperations;
    List<ChangeColorOperationsVO> changeColorOperations;
}
