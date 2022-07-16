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
public class Track implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 歌曲id
     */
    private Integer songId;

    /**
     * o-虚轨 1-实轨
     */
    private Integer type;

    /**
     * 轨道对应的按键
     */
    private String keyX;

    /**
     * 轨道的出现时间
     */
    private Integer startTiming;

    /**
     * 轨道的消失时间
     */
    private Integer endTiming;

    /**
     * 轨道横坐标
     */
    private Float positionX;

    /**
     * 宽度
     */
    private Float width;

    /**
     * R
     */
    @TableField("R")
    private Integer r;

    /**
     * G
     */
    @TableField("G")
    private Integer g;

    /**
     * B
     */
    @TableField("B")
    private Integer b;

    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


}
