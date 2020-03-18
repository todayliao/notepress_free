package me.wuwenbin.notepress.api.model.jwt;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * @author wuwen
 */
@Document(collection = "np_sys_sessions")
@Data
public class NotePressSession implements Serializable {

    private String jwtToken;

    public NotePressSession(String jwtToken) {
        this.jwtToken = jwtToken;
    }

}
