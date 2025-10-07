package generator.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 定时任务表
 * @TableName scheduled_task
 */
@TableName(value ="scheduled_task")
@Data
public class scheduled_task implements Serializable {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务 KEY（存在内存中）
     */
    private String tasKey;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务 corn 表达式
     */
    private String taskCron;

    /**
     * 任务描述
     */
    private String taskDesc;

    /**
     * 任务 Bean 名称（执行任务的 bean）
     */
    private String taskBean;

    /**
     * 任务状态（0-关闭, 1-开启）
     */
    private Integer taskStatus;

    /**
     * 是否删除（0-正常, 1-删除）
     */
    private Integer isDelete;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}