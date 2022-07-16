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
public class MoveOperation implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 歌曲ID
     */
    private Integer songId;

    /**
     * 所存在的轨道的编号
     */
    private Integer basedTrack;

    /**
     * 操作开始时间
     */
    private Integer startTime;

    /**
     * 操作结束时间
     */
    private Integer endTime;

    /**
     * 操作的起始坐标
     */
    private Float startX;

    /**
     * 操作的目的坐标
     */
    private Float endX;

    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


}
