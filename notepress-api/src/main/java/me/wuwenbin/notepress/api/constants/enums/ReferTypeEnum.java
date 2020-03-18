package me.wuwenbin.notepress.api.constants.enums;

import com.baomidou.mybatisplus.core.enums.IEnum;

/**
 * 参照类型
 * self_id为本身
 * refer_id为参照关联的id
 *
 * @author wuwenbin
 */
public enum ReferTypeEnum implements IEnum<String> {

    /**
     * self_id为第三方uuid
     * refer_id为user_id
     */
    THIRD_USER,

    /**
     * self_id为content_id
     * refer_id为dict_id（category类型）
     */
    CONTENT_CATEGORY,

    /**
     * self_id为content_id
     * refer_id为dict_id（tag类型）
     */
    CONTENT_TAG;

    @Override
    public String getValue() {
        return this.name().toLowerCase();
    }
}
