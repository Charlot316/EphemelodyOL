package team.javaee.entity.vo;

import lombok.Data;

@Data
public class ChangeWidthOperationsVO {
    private Integer id;
    private Integer startTime;
    private Integer endTime;
    private String endWidth;
    private String startWidth;
}
