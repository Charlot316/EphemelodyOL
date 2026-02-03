package team.javaee.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MoveOperationDTO {
    @com.fasterxml.jackson.annotation.JsonAlias("startTime")
    private Integer startTiming;
    @com.fasterxml.jackson.annotation.JsonAlias({ "endTime", "timing" })
    private Integer endTiming;
    private Float endX;
    private Float startX;
}
