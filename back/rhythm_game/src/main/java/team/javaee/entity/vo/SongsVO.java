package team.javaee.entity.vo;

import lombok.Data;

import java.util.List;

@Data
public class SongsVO {
    private SongInfoVO songInfo;
    private MyRecordVO1 myRecord;
    private List<TenBestRecordsVO> tenBestRecords;
}
