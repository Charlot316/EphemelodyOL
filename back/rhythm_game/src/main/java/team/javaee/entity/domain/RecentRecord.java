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
public class RecentRecord implements Serializable {

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
     * 玩家ID
     */
    private String userId;

    /**
     * 游玩分数
     */
    private Integer score;

    /**
     * pure数
     */
    private Integer pure;

    /**
     * far数
     */
    private Integer far;

    /**
     * lost数
     */
    private Integer lost;

    /**
     * combo数
     */
    private Integer combo;

    /**
     * 单次成绩潜力值
     */
    private Double potential;

    /**
     * 游玩的时间
     */
    private LocalDateTime time;

    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public RecentRecord() {

    }

    public RecentRecord(Integer songId, String userId, Integer score, Integer pure, Integer far, Integer lost, Integer combo, Double potential, LocalDateTime time) {
        this.songId = songId;
        this.userId = userId;
        this.score = score;
        this.pure = pure;
        this.far = far;
        this.lost = lost;
        this.combo = combo;
        this.potential = potential;
        this.time = time;
    }


}
