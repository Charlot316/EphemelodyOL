package team.javaee.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NoteDTO {
    private Integer noteType;
    private String key;
    private Integer timing;
    private Integer endTiming;
}
