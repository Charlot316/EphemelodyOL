package team.javaee.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import team.javaee.entity.domain.SongAsset;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChartContentDTO {
    private String songId;
    private Float bpm;
    private Integer firstBeatDelay;
    private List<ChangeBackgroundOperationDTO> changeBackgroundOperations;
    private List<TrackDTO> tracks;
    private List<SongAsset> assets;
}
