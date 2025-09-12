package org.shuzhi.PO;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 文件信息
 * </p>
 *
 * @author chat2db
 * @since 2025-09-09
 */
@Getter
@Setter
@TableName("rag_file_info")
public class RagFileInfoPO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    /**
     * 文件初始名称
     */
    private String fileInitialName;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 文件地址
     */
    private String filePath;

    /**
     * 文件大小
     */
    private String fileSize;

    /**
     * 是否经过向量
     */
    private Boolean sync;

    /**
     * 同步时间
     */
    private Date syncDate;

    /**
     * 同步用户
     */
    private String syncCreatorId;

    /**
     * 创建人id
     */
    private String creatorId;

    /**
     * 创建人名称
     */
    private String creatorName;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createDate;
}
