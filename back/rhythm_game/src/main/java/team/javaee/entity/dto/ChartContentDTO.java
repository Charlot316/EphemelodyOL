package team.javaee.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChartContentDTO {
    private Integer songId;
    private Float BPM;
    private Integer firstBeatDelay;
    private List<ChangeBackgroundOperationDTO> changeBackgroundOperations;
    private List<TrackDTO> tracks;
}
