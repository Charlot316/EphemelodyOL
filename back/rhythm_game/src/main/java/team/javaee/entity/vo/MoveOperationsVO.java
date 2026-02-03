package team.javaee.entity.vo;

import lombok.Data;

@Data
public class MoveOperationsVO {
    private Integer id;
    private Integer startTiming;
    private Integer endTiming;
    private String endX;
    private String startX;
}
