package team.javaee.entity.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author nwh
 * @since 2022-05-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Song implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     * 歌曲id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 歌曲名称
     */
    private String songName;

    /**
     * 歌曲作者
     */
    private String songWriter;

    /**
     * 谱面定数
     */
    private Float chartConstant;

    /**
     * 0-保存但未发布 1-发布但不是认定谱面 2-认定谱面
     */
    private Integer status;

    /**
     * 所有玩家的游玩次数
     */
    private Integer playTime;

    /**
     * 上传者信息
     */
    private String uploaderId;

    /**
     * 歌曲封面图的路径
     */
    private String songCover;

    /**
     * 歌曲的默认背景
     */
    private String defaultBackground;

    /**
     * 歌曲的note总数
     */
    private Integer notesCount;

    /**
     * 加载文字
     */
    private String loadingText;

    /**
     * 加载完的文字
     */
    private String loadedText;

    /**
     * 上传时间
     */
    private Date uploadDate;

    /**
     * 歌曲长度（毫秒）
     */
    private Integer songLength;

    /**
     * 歌曲音频的url
     */
    private String songUrl;

    /**
     * BPM
     */
    private Float BPM;

    /**
     * 第一拍延迟
     */
    private Integer firstBeatDelay;

    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


}
