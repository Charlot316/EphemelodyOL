package team.javaee.entity.dto;

import lombok.Data;

@Data
public class PublicChartsDTO {
    private Integer status;
    private String searchType;
    private String searchContent;
    private String sortType;
    private String sortWay;
}
