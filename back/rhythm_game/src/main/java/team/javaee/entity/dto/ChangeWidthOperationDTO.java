package team.javaee.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChangeWidthOperationDTO {
    private Integer startTime;
    private Integer endTime;
    private Float endWidth;
    private Float startWidth;
}
