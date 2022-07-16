package team.javaee.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RankVO {
    String userId;
    int score;
    int pure;
    int combo;
    int rank;
}
