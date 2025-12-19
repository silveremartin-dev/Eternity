package org.game.eternity2.server.security;

import io.grpc.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * gRPC interceptor for JWT authentication.
 * Validates JWT tokens on incoming requests.
 */
public class AuthInterceptor implements ServerInterceptor {

    @SuppressWarnings("unused")
    private static final Logger LOGGER = LogManager.getLogger(AuthInterceptor.class);
    private static final Metadata.Key<String> AUTH_HEADER = Metadata.Key.of("Authorization",
            Metadata.ASCII_STRING_MARSHALLER);

    public static final Context.Key<Integer> USER_ID_KEY = Context.key("userId");
    public static final Context.Key<String> USERNAME_KEY = Context.key("username");
    public static final Context.Key<String> ROLE_KEY = Context.key("role");

    private final JwtProvider jwtProvider;
    private final boolean enabled;

    public AuthInterceptor() {
        this(new JwtProvider(),
                Boolean.parseBoolean(System.getenv().getOrDefault("AUTH_ENABLED", "true")));
    }

    public AuthInterceptor(JwtProvider jwtProvider, boolean enabled) {
        this.jwtProvider = jwtProvider;
        this.enabled = enabled;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        if (!enabled) {
            return next.startCall(call, headers);
        }

        String authHeader = headers.get(AUTH_HEADER);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            call.close(Status.UNAUTHENTICATED.withDescription("Missing or invalid Authorization header"), headers);
            return new ServerCall.Listener<>() {
            };
        }

        String token = authHeader.substring(7);
        Integer userId = jwtProvider.getUserId(token);

        if (userId == null) {
            call.close(Status.UNAUTHENTICATED.withDescription("Invalid or expired token"), headers);
            return new ServerCall.Listener<>() {
            };
        }

        String username = jwtProvider.getUsername(token);
        String role = jwtProvider.getRole(token);

        Context context = Context.current()
                .withValue(USER_ID_KEY, userId)
                .withValue(USERNAME_KEY, username)
                .withValue(ROLE_KEY, role);

        return Contexts.interceptCall(context, call, headers, next);
    }
}
