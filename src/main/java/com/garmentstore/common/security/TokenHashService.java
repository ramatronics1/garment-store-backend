package com.garmentstore.common.security;
import org.springframework.stereotype.Service; import java.nio.charset.StandardCharsets; import java.security.*; import java.util.HexFormat;
@Service public class TokenHashService { public String sha256(String rawValue){ try{MessageDigest digest=MessageDigest.getInstance("SHA-256"); return HexFormat.of().formatHex(digest.digest(rawValue.getBytes(StandardCharsets.UTF_8)));}catch(NoSuchAlgorithmException ex){throw new IllegalStateException("SHA-256 algorithm unavailable",ex);}}}
