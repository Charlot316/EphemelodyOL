package team.javaee.entity.vo;

import lombok.Data;

@Data
public class ChangeColorOperationsVO {
    private Integer id;
    private Integer startTime;
    private Integer endTime;
    private Integer endR;
    private Integer endG;
    private Integer endB;
    private Integer startR;
    private Integer startG;
    private Integer startB;
}
