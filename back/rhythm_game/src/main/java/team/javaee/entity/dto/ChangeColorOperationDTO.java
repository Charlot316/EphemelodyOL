package team.javaee.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChangeColorOperationDTO {
    @com.fasterxml.jackson.annotation.JsonAlias("startTime")
    private Integer startTiming;
    @com.fasterxml.jackson.annotation.JsonAlias("endTime")
    private Integer endTiming;
    private Integer endR;
    private Integer endG;
    private Integer endB;
    private Integer startR;
    private Integer startG;
    private Integer startB;
}
