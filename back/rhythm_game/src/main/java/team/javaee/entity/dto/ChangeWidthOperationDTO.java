package team.javaee.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChangeWidthOperationDTO {
    @com.fasterxml.jackson.annotation.JsonAlias("startTime")
    private Integer startTiming;
    @com.fasterxml.jackson.annotation.JsonAlias("endTime")
    private Integer endTiming;
    private Float endWidth;
    private Float startWidth;
}
