package me.wuwenbin.notepress.web.controllers.api.admin;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.system.SysNotice;
import me.wuwenbin.notepress.api.model.entity.system.SysUser;
import me.wuwenbin.notepress.api.service.ISysNoticeService;
import me.wuwenbin.notepress.api.service.ISysUserService;
import me.wuwenbin.notepress.web.controllers.api.NotePressBaseController;
import me.wuwenbin.notepress.service.utils.NotePressJwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;

/**
 * @author wuwen
 */
@RestController
@RequestMapping("/admin/session")
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class AdminSessionController extends NotePressBaseController {

    private final ISysNoticeService sysNoticeService;
    private final ISysUserService sysUserService;

    @GetMapping("/user")
    public NotePressResult sessionUser() {
        SysUser sessionUser = NotePressJwtUtils.getUser();
        sessionUser.setPassword(null);
        return writeJsonOk(sessionUser);
    }

    @GetMapping("/notice")
    public NotePressResult newMessages() {
        return writeJsonOk(sysNoticeService.count(Wrappers.<SysNotice>query().eq("is_read", false)));
    }

    @PostMapping("/checkPass")
    public NotePressResult checkPass(@NotNull String md5Pass) {
        String username = NotePressJwtUtils.getUsername();
        String dbPass = SecureUtil.md5(md5Pass);
        SysUser sysUser = sysUserService.getOne(Wrappers.<SysUser>query()
                .eq("username", username)
                .eq("password", dbPass)
                .eq("admin", true)
                .eq("status", true));
        return writeJsonJudgedNull(sysUser, "验证成功！", "验证出错！");
    }

    @GetMapping("/redirect2Index")
    public NotePressResult redirect2Index() {
        return writeJson(NotePressResult::createOk);
    }

}
