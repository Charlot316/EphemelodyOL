package team.javaee.entity.domain;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class Song implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    private String songName;

    private String songWriter;

    private Float chartConstant;

    private Integer status;

    private Integer playTime;

    private String uploaderId;

    private String songCover;

    private String defaultBackground;

    private Integer notesCount;

    private String loadingText;

    private String loadedText;

    private Date uploadDate;

    private Integer songLength;

    private String songUrl;

    private Float bpm;

    private Integer firstBeatDelay;

    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
