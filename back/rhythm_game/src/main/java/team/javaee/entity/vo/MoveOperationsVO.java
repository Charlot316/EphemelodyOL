package team.javaee.entity.vo;

import lombok.Data;

@Data
public class MoveOperationsVO {
    private Integer id;
    private Integer startTime;
    private Integer endTime;
    private String endX;
    private String startX;
}
