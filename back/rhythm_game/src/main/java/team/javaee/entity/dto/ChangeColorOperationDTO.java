package team.javaee.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChangeColorOperationDTO {
    private Integer startTime;
    private Integer endTime;
    private Integer endR;
    private Integer endG;
    private Integer endB;
    private Integer startR;
    private Integer startG;
    private Integer startB;
}
