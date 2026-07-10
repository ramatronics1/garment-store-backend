package com.garmentstore.auth.application.event;
import java.time.Instant;
public record UserRegisteredEvent(Long userId,String email,String mobile,Instant registeredAt){}
