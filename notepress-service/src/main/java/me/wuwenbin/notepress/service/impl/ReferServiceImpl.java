package me.wuwenbin.notepress.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import me.wuwenbin.notepress.api.constants.enums.ReferTypeEnum;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.Refer;
import me.wuwenbin.notepress.api.model.entity.system.SysUser;
import me.wuwenbin.notepress.api.service.IReferService;
import me.wuwenbin.notepress.service.mapper.ReferMapper;
import me.wuwenbin.notepress.service.utils.NotePressSessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * created by Wuwenbin on 2019/11/28 at 2:24 下午
 *
 * @author wuwenbin
 */
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class ReferServiceImpl extends ServiceImpl<ReferMapper, Refer> implements IReferService {

    private final ReferMapper referMapper;

    @Override
    public NotePressResult hasBind(String source, String uuid) {
        String findIsBindSql = "select count(1) from np_refer where json_extract(refer_extra,'$.source') = ?;";
        int cnt = referMapper.queryNumberByArray(findIsBindSql, Integer.class, source);
        if (cnt == 0) {
            return NotePressResult.createOk("未绑定本站账号，可以操作！", true);
        } else {
            Refer refer = referMapper.findUserReferBySourceAndUuid(source, uuid);
            return NotePressResult.createOk("此帐号已被绑定，请更换之后再做操作！", refer);
        }
    }

    @Override
    public NotePressResult bind(long userId, String uuid, String source) {
        NotePressResult s = hasBind(source, uuid);
        //如果还未绑定本站账号，则开始绑定操作
        if (s.isSuccess() && s.getBoolData()) {
            SysUser sessionUser = NotePressSessionUtils.getSessionUser();
            int referCnt = referMapper.insertThirdUser(uuid, String.valueOf(userId),
                    genReferExtra(ReferTypeEnum.THIRD_USER, source),
                    LocalDateTime.now(), sessionUser != null ? sessionUser.getId() : null);
            if (referCnt == 1) {
                return NotePressResult.createOk("绑定成功！", true);
            }
        }
        return NotePressResult.createErrorMsg(s.getMsg());
    }
}
