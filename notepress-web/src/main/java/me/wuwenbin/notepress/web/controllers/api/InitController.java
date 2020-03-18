package me.wuwenbin.notepress.web.controllers.api;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.system.SysUser;
import me.wuwenbin.notepress.api.service.IParamService;
import me.wuwenbin.notepress.api.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wuwen
 */
@RestController
@RequestMapping("/init")
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class InitController extends NotePressBaseController {

    private final IParamService paramService;
    private final ISysUserService sysUserService;

    @GetMapping("/uploadPath")
    public NotePressResult uploadPath() {
        return paramService.fetchFileUploadInfo();
    }

    @SneakyThrows
    @PostMapping
    public NotePressResult init(SysUser sysUser) {
        String npAvatar = request.getParameter("np_avatar");
        if (StrUtil.isNotEmpty(npAvatar)) {
            sysUser.setAvatar(npAvatar);
        }
        return writeJson(() -> sysUserService.initAdministrator(sysUser));
    }

    @GetMapping("/status")
    public NotePressResult status() {
        return writeJson(paramService::fetchInitStatus);
    }
}
