package com.garmentstore.auth.application.event;
import com.garmentstore.auth.domain.OtpPurpose; import java.time.Instant;
public record OtpVerifiedEvent(Long userId,OtpPurpose purpose,Instant verifiedAt){}
