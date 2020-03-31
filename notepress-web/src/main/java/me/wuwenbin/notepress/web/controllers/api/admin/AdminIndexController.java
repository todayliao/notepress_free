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
import org.springframework.util.StringUtils;
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
        Criteria c1 = new Criteria();
        Criteria c2 = new Criteria();
        Criteria c3 = new Criteria();
        Criteria c41 = new Criteria();
        Criteria c42 = new Criteria();
        Criteria c43 = new Criteria();
        Criteria c44 = new Criteria();
        String statisticsMethodType = toRNull(paramService.fetchParamByName(ParamKeyConstant.STATISTICS_METHOD), Param.class, Param::getValue);
        if (!StringUtils.isEmpty(statisticsMethodType)) {
            List<String> dbSetList = Arrays.asList(statisticsMethodType.split("\\|"));
            if (dbSetList.contains("admin")) {
                c1 = newCriteria("/admin/");
            }
            if (dbSetList.contains("content")) {
                c2 = newCriteria("/content/");
            }
            if (dbSetList.contains("home_index")) {
                c3 = newCriteria("/index");
            }
            if (dbSetList.contains("other")) {
                c41 = newCriteria("/purchase");
                c42 = newCriteria("/note");
                c43 = newCriteria("/token/ubs");
                c44 = newCriteria("/res");
            }
        }
        Criteria c = new Criteria();
        c.orOperator(c1, c2, c3, c41, c42, c43, c44);
        if (time != null) {
            c.and("time").gte(time);
        }
        return mongoTemplate.find(
                Query.query(c), SysLog.class, "np_sys_log");
    }

    private int ipSum(LocalDate time) {
        List<SysLog> sysLogs = pvSum(time).stream().collect(Collectors.collectingAndThen(
                Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(SysLog::getIpAddr))), ArrayList::new));
        return sysLogs.size();
    }


    private Criteria newCriteria(String url) {
        return Criteria.byExample(
                Example.of(SysLog.builder().url(url).build(),
                        ExampleMatcher.matching().withMatcher("url", ExampleMatcher.GenericPropertyMatchers.contains())));
    }
}
