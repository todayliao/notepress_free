package me.wuwenbin.notepress.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.Deal;
import me.wuwenbin.notepress.api.service.IDealService;
import me.wuwenbin.notepress.service.mapper.DealMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author wuwen
 */
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class DealServiceImpl extends ServiceImpl<DealMapper, Deal> implements IDealService {

    private final DealMapper dealMapper;

    @Override
    public NotePressResult findCoinSumByUserId(Long userId) {
        String sql = "select sum(deal_amount) from np_deal where user_id = ?";
        int c = dealMapper.queryNumberByArray(sql, Integer.class, userId);
        return NotePressResult.createOkData(c);
    }

}
