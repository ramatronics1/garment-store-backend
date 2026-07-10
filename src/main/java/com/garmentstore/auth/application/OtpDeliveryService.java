package com.garmentstore.auth.application;
import com.garmentstore.auth.domain.OtpDeliveryChannel; import lombok.extern.slf4j.Slf4j; import org.springframework.stereotype.Service;
@Slf4j @Service public class OtpDeliveryService{ public void sendOtp(String target, OtpDeliveryChannel channel, String otp){ log.info("OTP delivery requested via {} to masked target {}",channel,mask(target)); } private String mask(String value){return value==null||value.length()<=4?"****":"****"+value.substring(value.length()-4);} }
