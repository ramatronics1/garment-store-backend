package com.garmentstore.auth.application;

import com.garmentstore.auth.domain.OtpDeliveryChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OtpDeliveryService {
    private static final Logger log = LoggerFactory.getLogger(OtpDeliveryService.class);

    public void sendOtp(String target, OtpDeliveryChannel channel, String otp) {
        log.info("OTP delivery requested via {} to masked target {}", channel, mask(target));
    }

    private String mask(String value) {
        return value == null || value.length() <= 4 ? "****" : "****" + value.substring(value.length() - 4);
    }
}
