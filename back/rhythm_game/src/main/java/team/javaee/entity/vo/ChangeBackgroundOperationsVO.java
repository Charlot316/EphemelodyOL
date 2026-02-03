package team.javaee.entity.vo;

import lombok.Data;

@Data
public class ChangeBackgroundOperationsVO {
    private Integer id;
    private Integer startTiming;
    private Integer endTiming;
    private String background;
    private Integer assetId;
}
