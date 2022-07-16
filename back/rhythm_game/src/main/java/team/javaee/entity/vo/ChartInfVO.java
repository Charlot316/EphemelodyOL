package team.javaee.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChartInfVO {
    SongInfVO songInfo;
    MyRecordVO myRecord;
    List<TenBestRecordsVO> tenBestRecords;
}
