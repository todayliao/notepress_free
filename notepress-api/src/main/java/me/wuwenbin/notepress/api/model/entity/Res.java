package me.wuwenbin.notepress.api.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import me.wuwenbin.notepress.api.annotation.query.SimpleCondition;
import me.wuwenbin.notepress.api.annotation.query.WrapperCondition;
import me.wuwenbin.notepress.api.model.entity.base.BaseEntity;

/**
 * @author wuwen
 */
@Builder
@EqualsAndHashCode(callSuper = true)
@Data
public class Res extends BaseEntity<Res> {

    @TableId(type = IdType.INPUT)
    private String id;
    @WrapperCondition(SimpleCondition.like)
    private String resHash;
    private String resUrl;
    private Double resFsizeBytes;
    private Integer coin;
}
