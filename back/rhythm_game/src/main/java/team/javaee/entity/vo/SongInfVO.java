package team.javaee.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SongInfVO {
    String songName;
    String uploader;
    String songCover;
    String songWriter;
    double songDifficulty;
}
