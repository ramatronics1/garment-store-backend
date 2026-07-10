package com.garmentstore.common.security;
import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties(prefix="app.security.jwt") public record JwtProperties(String issuer,long accessTokenExpiryMinutes,long refreshTokenExpiryDays,String secret){}
