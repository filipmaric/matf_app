package rs.ac.bg.matf.auth

data class LoginRequest(
    val username: String,
    val password: String,
    val deviceId: String,
    val deviceName: String,
)

data class RadiusUser(
    val id: Long,
    val radiusUsername: String,
)

data class SessionInfo(
    val id: Long,
    val deviceId: String,
    val deviceName: String,
    val expiresAt: String,
)

data class LoginResponse(
    val token: String,
    val tokenType: String,
    val expiresAt: String,
    val user: RadiusUser,
    val session: SessionInfo,
)

data class MeResponse(
    val user: RadiusUser,
    val session: SessionInfo,
)

data class AuthSession(
    val token: String,
    val username: String,
    val deviceId: String,
    val deviceName: String,
    val expiresAt: String,
)
