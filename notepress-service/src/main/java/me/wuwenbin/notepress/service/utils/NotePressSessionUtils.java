package me.wuwenbin.notepress.service.utils;

import cn.hutool.core.util.StrUtil;
import me.wuwenbin.notepress.api.constants.NotePressConstants;
import me.wuwenbin.notepress.api.model.entity.system.SysUser;
import me.wuwenbin.notepress.api.model.jwt.NotePressSession;
import me.wuwenbin.notepress.api.utils.NotePressServletUtils;
import me.wuwenbin.notepress.api.utils.NotePressUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author wuwen
 */
public class NotePressSessionUtils {

    private static final MongoTemplate MONGO_TEMPLATE = NotePressUtils.getBean(MongoTemplate.class);

    public static SysUser getSessionUser() {
        if (isAdminReq()) {
            String token = NotePressJwtUtils.getJwtToken();
            if (StrUtil.isNotEmpty(token)) {
                NotePressSession npSession = MONGO_TEMPLATE.findOne(
                        Query.query(Criteria.where("jwtToken").is(token)),
                        NotePressSession.class, "np_sys_sessions");
                if (npSession != null) {
                    String username = NotePressJwtUtils.getUsername();
                    if (StrUtil.isNotEmpty(username)) {
                        return NotePressJwtUtils.getUser();
                    }
                }
            }
            return null;
        } else {
            HttpSession session = NotePressServletUtils.getSession();
            return (SysUser) session.getAttribute(NotePressConstants.SESSION_USER_KEY);
        }
    }

    public static void setSessionUser(SysUser sessionUser, String jwtToken) {
        if (isAdminReq() && StrUtil.isNotEmpty(jwtToken)) {
            MONGO_TEMPLATE.insert(new NotePressSession(jwtToken), "np_sys_sessions");
        } else {
            HttpSession session = NotePressServletUtils.getSession();
            session.setAttribute(NotePressConstants.SESSION_USER_KEY, sessionUser);
            session.setMaxInactiveInterval(7200);
        }
    }


    public static void invalidSessionUser() {
        if (isAdminReq()) {
            String token = NotePressJwtUtils.getJwtToken();
            MONGO_TEMPLATE.remove(Query.query(Criteria.where("jwtToken").is(token)),
                    NotePressSession.class, "np_sys_sessions");
        } else {
            HttpSession session = NotePressServletUtils.getSession();
            SysUser sessionUser = (SysUser) session.getAttribute(NotePressConstants.SESSION_USER_KEY);
            session.removeAttribute(NotePressConstants.SESSION_USER_KEY);
            session.invalidate();
            MONGO_TEMPLATE.remove(sessionUser, "np_user_logged_in");
        }
    }

    private static boolean isAdminReq() {
        HttpServletRequest request = NotePressServletUtils.getRequest();
        return request.getRequestURI().startsWith("/admin/");
    }

}
