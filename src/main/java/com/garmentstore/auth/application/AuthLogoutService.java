package com.garmentstore.auth.application;

import com.garmentstore.auth.dto.LogoutRequest;
import com.garmentstore.auth.dto.LogoutResponse;
import com.garmentstore.common.audit.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthLogoutService {
    private final RefreshTokenService refreshTokenService;
        private final AuditLogService auditLogService;

    @Transactional
    public LogoutResponse logout(LogoutRequest request) {
        int revokedCount = refreshTokenService.revokeCurrent(request.refreshToken());
        auditLogService.record(null, null, "LOGOUT", "REFRESH_TOKEN", null, "{\"revokedTokenCount\":" + revokedCount + "}", null);
            return new LogoutResponse("LOGGED_OUT", revokedCount);
    }

    @Transactional
    public LogoutResponse logoutAll(Long userId) {
        int revokedCount = refreshTokenService.revokeAllForUser(userId);
        auditLogService.record(userId, null, "LOGOUT_ALL", "USER", String.valueOf(userId), "{\"revokedTokenCount\":" + revokedCount + "}", null);
            return new LogoutResponse("LOGGED_OUT_ALL", revokedCount);
    }
}
