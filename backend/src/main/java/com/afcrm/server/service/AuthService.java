package com.afcrm.server.service;

import com.afcrm.server.dto.AuthRequest;
import com.afcrm.server.dto.AuthResponse;
import com.afcrm.server.model.Role;
import com.afcrm.server.model.User;
import com.afcrm.server.repository.UserRepository;
import com.afcrm.server.security.CustomUserDetails;
import com.afcrm.server.security.JwtService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${GOOGLE_CLIENT_ID:your-google-client-id}")
    private String googleClientId;

    public AuthResponse login(AuthRequest request) {
        if (request.getIdToken() != null && !request.getIdToken().isEmpty()) {
            // Google OAuth2 Validation
            try {
                GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                        .setAudience(Collections.singletonList(googleClientId))
                        .build();

                GoogleIdToken idToken = verifier.verify(request.getIdToken());
                if (idToken != null) {
                    GoogleIdToken.Payload payload = idToken.getPayload();
                    String email = payload.getEmail();

                    User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not registered in system"));
                    String jwtToken = jwtService.generateToken(new CustomUserDetails(user));
                    return new AuthResponse(jwtToken);
                } else {
                    throw new RuntimeException("Invalid Google ID Token");
                }
            } catch (Exception e) {
                throw new RuntimeException("Google Authentication Failed: " + e.getMessage());
            }
        } else {
            // Traditional Email/Password Auth
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
            var user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow();
            var jwtToken = jwtService.generateToken(new CustomUserDetails(user));
            return new AuthResponse(jwtToken);
        }
    }
}
