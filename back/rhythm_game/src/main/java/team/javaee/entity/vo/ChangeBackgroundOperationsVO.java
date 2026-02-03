package team.javaee.entity.vo;

import lombok.Data;

@Data
public class ChangeBackgroundOperationsVO {
    private Integer id;
    private Integer startTime;
    private Integer endTime;
    private String background;
    private Integer assetId;
}
