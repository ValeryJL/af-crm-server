package com.afcrm.server.service;

import com.afcrm.server.dto.AuthRequest;
import com.afcrm.server.dto.GoogleLoginRequest;
import com.afcrm.server.dto.AuthResponse;
import com.afcrm.server.dto.RegisterInvitedRequest;
import com.afcrm.server.dto.UserDto;
import com.afcrm.server.model.Invitation;
import com.afcrm.server.model.Role;
import com.afcrm.server.model.User;
import com.afcrm.server.repository.InvitationRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final InvitationRepository invitationRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Value("${GOOGLE_CLIENT_ID:your-google-client-id}")
    private String googleClientId;

    public AuthResponse login(AuthRequest request) {
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

    public AuthResponse loginWithGoogle(GoogleLoginRequest request) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(request.getIdToken());
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();

                User user = userRepository.findByEmail(email).orElseGet(() -> {
                    // Auto-register new Google user as TECH
                    String name = (String) payload.get("name");
                    String givenName = (String) payload.get("given_name");
                    String familyName = (String) payload.get("family_name");

                    User newUser = User.builder()
                            .email(email)
                            .password(passwordEncoder.encode(java.util.UUID.randomUUID().toString())) // Random password for social users
                            .role(Role.TECH)
                            .nombre(givenName != null ? givenName : (name != null ? name : "Google"))
                            .apellido(familyName != null ? familyName : "User")
                            .status("ACTIVE")
                            .theme("light")
                            .oauthEnabled(true) // Explicitly enable for auto-registered users
                            .build();
                    return userRepository.save(newUser);
                });

                if (!user.isOauthEnabled()) {
                    throw new RuntimeException("Google authentication is disabled for this account");
                }

                String jwtToken = jwtService.generateToken(new CustomUserDetails(user));
                return new AuthResponse(jwtToken);
            } else {
                throw new RuntimeException("Invalid Google ID Token");
            }
        } catch (Exception e) {
            throw new RuntimeException("Google Authentication Failed: " + e.getMessage());
        }
    }

    public AuthResponse registerAdmin(UserDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.SUPER_ADMIN)
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .telefono(request.getTelefono())
                .status("ACTIVE")
                .theme(request.getTheme() != null ? request.getTheme() : "light")
                .customConfiguration(request.getCustomConfiguration())
                .build();

        userRepository.save(user);

        // Auto-login after registration
        return login(new AuthRequest(request.getEmail(), request.getPassword()));
    }

    public AuthResponse registerInvited(RegisterInvitedRequest request) {
        Invitation invitation = invitationRepository.findByToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid invitation token"));

        if (invitation.isUsed()) {
            throw new RuntimeException("Invitation token already used");
        }

        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Invitation token expired");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(invitation.getRole())
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .telefono(request.getTelefono())
                .status("ACTIVE")
                .theme("light")
                .oauthEnabled(true)
                .build();

        userRepository.save(user);

        invitation.setUsed(true);
        invitationRepository.save(invitation);

        return login(new AuthRequest(request.getEmail(), request.getPassword()));
    }

    public boolean isSetupRequired() {
        return !userRepository.existsByRole(Role.SUPER_ADMIN);
    }
}
