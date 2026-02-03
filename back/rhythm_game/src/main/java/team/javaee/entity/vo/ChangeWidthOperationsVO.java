package team.javaee.entity.vo;

import lombok.Data;

@Data
public class ChangeWidthOperationsVO {
    private Integer id;
    private Integer startTiming;
    private Integer endTiming;
    private String endWidth;
    private String startWidth;
}
