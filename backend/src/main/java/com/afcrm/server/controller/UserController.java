import com.afcrm.server.dto.UserDto;
import com.afcrm.server.model.User;
import com.afcrm.server.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDto userDto) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }
        
        User user = User.builder()
                .email(userDto.getEmail())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .role(userDto.getRole())
                .nombre(userDto.getNombre())
                .apellido(userDto.getApellido())
                .telefono(userDto.getTelefono())
                .status(userDto.getStatus())
                .theme(userDto.getTheme() != null ? userDto.getTheme() : "light")
                .customConfiguration(userDto.getCustomConfiguration())
                .build();
                
        return ResponseEntity.ok(userRepository.save(user));
    }
}
