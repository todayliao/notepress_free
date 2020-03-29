package me.wuwenbin.notepress.api.service;

import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.Deal;
import me.wuwenbin.notepress.api.service.base.INotePressService;

/**
 * @author wuwen
 */
public interface IDealService extends INotePressService<Deal> {

    /**
     * 查找某位用户的硬币数量
     *
     * @param userId
     * @return
     */
    NotePressResult findCoinSumByUserId(Long userId);
}
