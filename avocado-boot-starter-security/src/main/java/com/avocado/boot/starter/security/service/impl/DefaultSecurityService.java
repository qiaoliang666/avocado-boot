package com.avocado.boot.starter.security.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.avocado.boot.starter.core.exception.BusinessException;
import com.avocado.boot.starter.core.utils.JsonUtils;
import com.avocado.boot.starter.security.bean.AccessToken;
import com.avocado.boot.starter.security.bean.Authentication;
import com.avocado.boot.starter.security.properties.SecurityProperties;
import com.avocado.boot.starter.security.service.ISecurityService;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Date;
import java.util.UUID;

import static com.avocado.boot.starter.security.enums.SecurityErrorType.NO_ACCESS_ERROR;

/**
 * @author ：qiaoliang
 */
public class DefaultSecurityService implements ISecurityService {

    private final SecurityProperties securityProperties;

    public DefaultSecurityService(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Override
    public Authentication getAuthentication(String token) throws JsonProcessingException {
        try {
            //  验证 Jwt Token 的颁发者,有效期
            DecodedJWT jwt = JWT.require(Algorithm.HMAC256(securityProperties.getIssuer()))
                    .withIssuer(securityProperties.getIssuer()).build().verify(token);
            //  将凭证信息序列化成对象
            return JsonUtils.toBean(jwt.getClaim(securityProperties.getCert()).asString(),Authentication.class);
        }catch (JWTVerificationException e){
            e.printStackTrace();
            throw new BusinessException(NO_ACCESS_ERROR);
        }
    }

    @Override
    public AccessToken generateToken(Authentication authentication) throws JsonProcessingException {
        //  生成JwtToken
        String token = JWT.create().withIssuer(securityProperties.getIssuer())
                //  唯一标识
                .withJWTId(UUID.randomUUID().toString())
                //  凭证信息
                .withClaim(securityProperties.getCert(), JsonUtils.toJson(authentication))
                //  到期时间（毫秒值）
                .withExpiresAt(new Date(System.currentTimeMillis() + securityProperties.getTime()))
                .sign(Algorithm.HMAC256(securityProperties.getIssuer()));

        return new AccessToken(token,System.currentTimeMillis() + securityProperties.getTime());
    }
}
