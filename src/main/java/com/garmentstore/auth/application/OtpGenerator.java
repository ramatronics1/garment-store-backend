package com.garmentstore.auth.application;
import org.springframework.stereotype.Component; import java.security.SecureRandom;
@Component public class OtpGenerator{private final SecureRandom secureRandom=new SecureRandom(); public String generateSixDigitOtp(){return String.format("%06d",secureRandom.nextInt(1_000_000));}}
