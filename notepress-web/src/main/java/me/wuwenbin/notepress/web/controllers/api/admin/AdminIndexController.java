package me.wuwenbin.notepress.web.controllers.api.admin;

import lombok.RequiredArgsConstructor;
import me.wuwenbin.notepress.api.constants.ParamKeyConstant;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.Param;
import me.wuwenbin.notepress.api.model.entity.system.SysLog;
import me.wuwenbin.notepress.api.query.ContentQuery;
import me.wuwenbin.notepress.api.query.SysUserQuery;
import me.wuwenbin.notepress.api.service.IContentService;
import me.wuwenbin.notepress.api.service.IParamService;
import me.wuwenbin.notepress.api.service.ISysUserService;
import me.wuwenbin.notepress.api.utils.NotePressServerUtils;
import me.wuwenbin.notepress.web.controllers.api.NotePressBaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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
    private final MongoTemplate mongoTemplate;

    @GetMapping("/menu")
    public NotePressResult menuJson() {
        return writeJson(paramService::fetchIndexMenu);
    }

    @GetMapping("/dashboard")
    public NotePressResult dashboard() {
        Map<String, Object> pMap = new HashMap<>(15);
        pMap.put("postCnt", contentService.count());
        pMap.put("regUser", userService.count(SysUserQuery.buildByAdmin(false)));
        pMap.put("ip", ipSum(null));
        pMap.put("pv", pvSum(null).size());
        pMap.put("todayPostCnt", contentService.count(ContentQuery.buildTodayCount()));
        pMap.put("todayRegUser", userService.count(SysUserQuery.buildTodayCount()));
        pMap.put("todayIp", ipSum(LocalDate.now()));
        pMap.put("todayPv", pvSum(LocalDate.now()).size());
        pMap.put("system_started_datetime", paramService.fetchParamByName(ParamKeyConstant.SYSTEM_STARTED_DATETIME).getDataBean(Param.class).getValue());
        pMap.put("notepress-version", NotePressServerUtils.version());
        pMap.put("layui", NotePressServerUtils.layuiVersion());
        pMap.put("os", NotePressServerUtils.osName());
        pMap.put("jdk", NotePressServerUtils.javaVersion());
        pMap.put("cpu", NotePressServerUtils.cpu());
        pMap.put("mem", NotePressServerUtils.maxMemory());
        return writeJsonOk(pMap);
    }

    private List<SysLog> pvSum(LocalDate time) {
        Criteria criteria = Criteria.byExample(
                Example.of(
                        SysLog.builder().url("/content/").build(),
                        ExampleMatcher.matching().withMatcher("url", ExampleMatcher.GenericPropertyMatchers.contains()))
        );
        if (time != null) {
            criteria.and("time").gte(time);
        }
        return mongoTemplate.find(
                Query.query(criteria), SysLog.class, "np_sys_log");
    }

    private int ipSum(LocalDate time) {
        List<SysLog> sysLogs = pvSum(time).stream().collect(Collectors.collectingAndThen(
                Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(SysLog::getIpAddr))), ArrayList::new));
        return sysLogs.size();
    }
}
