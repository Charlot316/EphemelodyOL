package team.javaee.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChangeBackgroundOperationDTO {
    @com.fasterxml.jackson.annotation.JsonAlias("startTime")
    private Integer startTiming;
    @com.fasterxml.jackson.annotation.JsonAlias("endTime")
    private Integer endTiming;
    private String background;
    private Integer assetId;
}
