package team.javaee.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrackDTO {
    private Integer type;
    private String key;
    private Integer startTiming;
    private Integer endTiming;
    private Float positionX;
    private Float width;
    @JsonProperty("R")
    private Integer r;
    @JsonProperty("G")
    private Integer g;
    @JsonProperty("B")
    private Integer b;
    private List<NoteDTO> notes;
    private List<MoveOperationDTO> moveOperations;
    private List<ChangeWidthOperationDTO> changeWidthOperations;
    private List<ChangeColorOperationDTO> changeColorOperations;
}
