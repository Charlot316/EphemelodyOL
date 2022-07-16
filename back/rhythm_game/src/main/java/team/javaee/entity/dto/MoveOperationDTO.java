package team.javaee.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MoveOperationDTO {
    private Integer startTime;
    private Integer endTime;
    private Float endX;
    private Float startX;
}
