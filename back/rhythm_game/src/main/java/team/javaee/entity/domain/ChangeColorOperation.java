package team.javaee.entity.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

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
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChangeColorOperation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 歌曲id
     */
    private String songId;

    /**
     * 所存在的轨道的编号
     */
    private Integer basedTrack;

    /**
     * 操作开始时间
     */
    private Integer startTiming;

    /**
     * 操作结束时间
     */
    private Integer endTiming;

    /**
     * 操作的起始R
     */
    private Integer startR;

    /**
     * 操作的起始G
     */
    private Integer startG;

    /**
     * 操作的起始B
     */
    private Integer startB;

    /**
     * 操作的目的R
     */
    private Integer endR;

    /**
     * 操作的目的G
     */
    private Integer endG;

    /**
     * 操作的目的B
     */
    private Integer endB;

    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
