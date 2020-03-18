package me.wuwenbin.notepress.web.controllers.api.admin;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.system.SysUser;
import me.wuwenbin.notepress.api.model.layui.query.LayuiTableQuery;
import me.wuwenbin.notepress.api.query.ReferQuery;
import me.wuwenbin.notepress.api.query.SysNoticeQuery;
import me.wuwenbin.notepress.api.service.IReferService;
import me.wuwenbin.notepress.api.service.ISysNoticeService;
import me.wuwenbin.notepress.api.service.ISysUserService;
import me.wuwenbin.notepress.web.controllers.api.NotePressBaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * created by Wuwenbin on 2019/11/29 at 3:36 下午
 *
 * @author wuwenbin
 */
@RestController
@RequestMapping("/admin/user")
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class AdminUserController extends NotePressBaseController {

    private final ISysUserService userService;
    private final IReferService referService;
    private final ISysNoticeService noticeService;

    @PostMapping
    public NotePressResult userList(Page<SysUser> sysUserPage, @RequestBody LayuiTableQuery<SysUser> layuiTableQuery) {
        //查询非管理员用户
        layuiTableQuery.getExtra().setAdmin(false);
        return writeJsonLayuiTable(userService.findUserList(sysUserPage, layuiTableQuery));
    }


    @PostMapping("/update")
    public NotePressResult updateUser(@NotNull SysUser sysUser) {
        if (StrUtil.isNotEmpty(sysUser.getPassword())) {
            sysUser.setPassword(SecureUtil.md5(sysUser.getPassword()));
        }
        return writeJson(() -> userService.updateUserById(sysUser));
    }

    @PostMapping("/add")
    public NotePressResult addUser(@Valid SysUser sysUser, BindingResult bindingResult) {
        if (!bindingResult.hasErrors()) {
            sysUser.setPassword(SecureUtil.md5(sysUser.getPassword()));
            return userService.addUser(sysUser);
        }
        return writeJsonJsr303(bindingResult.getFieldErrors());
    }

    @PostMapping("/disable")
    public NotePressResult disableUser(@RequestParam Long id, @RequestParam(defaultValue = "true") Boolean status) {
        SysUser editUser = SysUser.builder().id(id).status(status).build();
        return writeJson(() -> userService.updateUserById(editUser));
    }

    @PostMapping("/delete")
    public NotePressResult deleteUser(@RequestParam Long id) {
        boolean res = userService.removeById(id);
        referService.remove(ReferQuery.buildByUser(String.valueOf(id)));
        noticeService.remove(SysNoticeQuery.build("user_id", id));
        return writeJsonJudgedBool(res, "删除成功！", "删除失败！");
    }

}
