package me.wuwenbin.notepress.web.controllers.api.admin;

import lombok.RequiredArgsConstructor;
import me.wuwenbin.notepress.api.constants.ParamKeyConstant;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.Param;
import me.wuwenbin.notepress.api.query.ContentQuery;
import me.wuwenbin.notepress.api.query.SysUserQuery;
import me.wuwenbin.notepress.api.service.IContentService;
import me.wuwenbin.notepress.api.service.IParamService;
import me.wuwenbin.notepress.api.service.ISysUserService;
import me.wuwenbin.notepress.api.utils.NotePressServerUtils;
import me.wuwenbin.notepress.web.controllers.api.NotePressBaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wuwen
 */
@RestController
@RequestMapping("/admin/index")
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class AdminIndexController extends NotePressBaseController {

    private final IParamService paramService;
    private final IContentService contentService;
    private final ISysUserService userService;

    @GetMapping("/menu")
    public NotePressResult menuJson() {
        return writeJson(paramService::fetchIndexMenu);
    }

    @GetMapping("/dashboard")
    public NotePressResult dashboard() {
        Map<String, Object> pMap = new HashMap<>(15);
        pMap.put("postCnt", contentService.count());
        pMap.put("regUser", userService.count(SysUserQuery.buildByAdmin(false)));
        pMap.put("ip", "--");
        pMap.put("pv", "--");
        pMap.put("todayPostCnt", contentService.count(ContentQuery.buildTodayCount()));
        pMap.put("todayRegUser", userService.count(SysUserQuery.buildTodayCount()));
        pMap.put("todayIp", "--");
        pMap.put("todayPv", "--");
        pMap.put("system_started_datetime", paramService.fetchParamByName(ParamKeyConstant.SYSTEM_STARTED_DATETIME).getDataBean(Param.class).getValue());
        pMap.put("notepress-version", NotePressServerUtils.version());
        pMap.put("layui", NotePressServerUtils.layuiVersion());
        pMap.put("os", NotePressServerUtils.osName());
        pMap.put("jdk", NotePressServerUtils.javaVersion());
        pMap.put("cpu", NotePressServerUtils.cpu());
        pMap.put("mem", NotePressServerUtils.maxMemory());
        return writeJsonOk(pMap);
    }
}
