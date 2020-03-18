package me.wuwenbin.notepress.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import me.wuwenbin.notepress.api.model.entity.Hide;
import me.wuwenbin.notepress.api.service.IHideService;
import me.wuwenbin.notepress.service.mapper.HideMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author wuwenbin
 */
@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
@Transactional(rollbackFor = Exception.class)
public class HideServiceImpl extends ServiceImpl<HideMapper, Hide> implements IHideService {
}
