package team.javaee.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChangeBackgroundOperationDTO {
    private Integer startTime;
    private String background;
}
