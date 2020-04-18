package me.wuwenbin.notepress.springboot.container.listener;

import lombok.extern.slf4j.Slf4j;
import me.wuwenbin.notepress.api.constants.NotePressConstants;
import me.wuwenbin.notepress.api.model.entity.system.SysUser;
import me.wuwenbin.notepress.api.utils.NotePressUtils;
import org.springframework.data.mongodb.core.MongoTemplate;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * @author wuwen
 */
@Slf4j
public class NotePressSessionListener implements HttpSessionListener {

    private static final MongoTemplate MONGO_TEMPLATE = NotePressUtils.getBean(MongoTemplate.class);

    /**
     * Notification that a session was created.
     *
     * @param se the notification event
     */
    @Override
    public void sessionCreated(HttpSessionEvent se) {
    }

    /**
     * Notification that a session is about to be invalidated.
     *
     * @param se the notification event
     */
    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        SysUser sessionUser = (SysUser) se.getSession().getAttribute(NotePressConstants.SESSION_USER_KEY);
        MONGO_TEMPLATE.remove(sessionUser, "np_user_logged_in");
    }
}
