package me.wuwenbin.notepress.web.controllers.api.theme;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import me.wuwenbin.notepress.api.constants.enums.ReferTypeEnum;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.Dictionary;
import me.wuwenbin.notepress.api.model.entity.Param;
import me.wuwenbin.notepress.api.model.entity.Res;
import me.wuwenbin.notepress.api.model.entity.system.SysNotice;
import me.wuwenbin.notepress.api.model.entity.system.SysUser;
import me.wuwenbin.notepress.api.query.ReferQuery;
import me.wuwenbin.notepress.api.service.*;
import me.wuwenbin.notepress.service.utils.NotePressSessionUtils;
import me.wuwenbin.notepress.web.controllers.api.NotePressBaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author wuwen
 */
@Controller
@RequestMapping("/token/ubs")
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class NotePressUbsController extends NotePressBaseController {

    private final ISysUserService userService;
    private final IDictionaryService dictionaryService;
    private final ISysNoticeService noticeService;
    private final IContentService contentService;
    private final IReferService referService;
    private final IResService resService;
    private final IParamService paramService;

    @GetMapping
    public String index(Model model) {
        //数量前30的tag
        List<Dictionary> tagListTop30 = toListBeanNull(dictionaryService.top30TagList());
        model.addAttribute("tagList", tagListTop30);
        model.addAttribute("s", "");
        //noinspection ConstantConditions
        int commentCnt = noticeService.count(Wrappers.<SysNotice>query().eq("user_id", NotePressSessionUtils.getSessionUser().getId()));
        model.addAttribute("commentCnt", commentCnt);
        int purchaseCnt = referService.count(ReferQuery.buildBySelfIdAndType(NotePressSessionUtils.getSessionUser().getId().toString(), ReferTypeEnum.USER_RES));
        model.addAttribute("purchaseCnt", purchaseCnt);

        return "ubs";
    }

    @PostMapping("/updateInfo")
    @ResponseBody
    public NotePressResult updateInfo(String nickname, String newPwd) {
        return writeJson(() -> userService.userUpdateInfo(nickname, newPwd));
    }

    @PostMapping("/checkPwd")
    @ResponseBody
    public NotePressResult checkPwd(String pwd) {
        pwd = SecureUtil.md5(pwd);
        SysUser sessionUser = NotePressSessionUtils.getSessionUser();
        if (sessionUser != null) {
            if (sessionUser.getPassword().contentEquals(pwd)) {
                return writeJsonOkMsg("检验成功！");
            }
        }
        return writeJsonErrorMsg("校验失败！");
    }

    @PostMapping("/myComments")
    @ResponseBody
    public NotePressResult myComments(Page<SysNotice> page) {
        SysUser sessionUser = NotePressSessionUtils.getSessionUser();
        page.addOrder(OrderItem.desc("gmt_create"));
        //noinspection ConstantConditions
        IPage<SysNotice> nPage = noticeService.page(page, Wrappers.<SysNotice>query().eq("user_id", sessionUser.getId()));
        List<SysNotice> noticeList = nPage.getRecords();
        Map<String, String> contentIdTitleMap = noticeList.stream()
                .filter(sysNotice -> !"-1".contentEquals(sysNotice.getContentId()))
                .collect(Collectors.toMap(SysNotice::getContentId, sysNotice -> contentService.getById(sysNotice.getContentId()).getTitle(), (v1, v2) -> v2));
        contentIdTitleMap.putIfAbsent("-1", "游客/用户留言");
        Map<String, Object> resMap = MapUtil.of("comments", nPage);
        resMap.put("titles", contentIdTitleMap);
        return writeJsonOk(resMap);
    }

    @PostMapping("/myPurchased")
    @ResponseBody
    public NotePressResult myPurchased(Page<Res> page) {
        SysUser sessionUser = NotePressSessionUtils.getSessionUser();
        page.addOrder(OrderItem.desc("gmt_create"));
        assert sessionUser != null;
        List<String> resIds = referService.list(
                ReferQuery.buildBySelfIdAndType(sessionUser.getId().toString(), ReferTypeEnum.USER_RES))
                .stream()
                .map(refer -> resService.getById(refer.getReferId()).getId()).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(resIds)) {
            resIds = Collections.singletonList("0");
        }
        IPage<Res> rPage = resService.page(page, Wrappers.<Res>query().in("id", resIds));
        return writeJsonOk(rPage);
    }

    @PostMapping("/create/order")
    @ResponseBody
    public String createOrder(@RequestParam BigDecimal price, @RequestParam String type) {
        Param domainParam = toBeanNull(paramService.fetchParamByName("recharge_server_domain"), Param.class);
        Param keyParam = toBeanNull(paramService.fetchParamByName("recharge_sign_secretKey"), Param.class);
        if (domainParam != null && keyParam != null) {
            String domain = domainParam.getValue();
            String key = keyParam.getValue();
            String url = domain + "/pay/order?userId={}&type={}&price={}&sign={}&_t=" + System.currentTimeMillis();
            long userId = Objects.requireNonNull(NotePressSessionUtils.getSessionUser()).getId();
            String sign = SecureUtil.md5(SecureUtil.md5(price + type) + key);
            url = StrUtil.format(url, userId, type, price, sign);
            return url;
        }
        return null;
    }
}
