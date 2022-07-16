package team.javaee.entity.vo;

import lombok.Data;

@Data
public class NotesVO {
    private Integer id;
    private Integer noteType;
    private String key;
    private Integer timing;
    private Integer endTiming;
}
