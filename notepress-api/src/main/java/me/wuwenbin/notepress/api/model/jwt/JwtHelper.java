package me.wuwenbin.notepress.api.model.jwt;

import lombok.Data;

/**
 * @author wuwenbin
 */
@Data
public class JwtHelper {

    private String clientId;
    private String base64Secret;
    private String name;
    private int expiresMillSecond;
}
