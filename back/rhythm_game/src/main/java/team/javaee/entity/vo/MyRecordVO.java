package team.javaee.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyRecordVO {
    double bestScore;
    int recordStatus;
    int ranking;
}
