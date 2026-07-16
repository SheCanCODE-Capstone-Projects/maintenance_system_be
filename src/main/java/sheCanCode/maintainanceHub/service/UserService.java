package sheCanCode.maintainanceHub.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import sheCanCode.maintainanceHub.dto.PasswordChangeDTO;
import sheCanCode.maintainanceHub.dto.UserRequestDTO;
import sheCanCode.maintainanceHub.dto.UserResponseDTO;
import sheCanCode.maintainanceHub.entity.User;
import sheCanCode.maintainanceHub.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserResponseDTO register(UserRequestDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail()))
            throw new RuntimeException("EMAIL_EXISTS");

        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(dto.getRole())
                .build();

        return toResponse(userRepository.save(user));
    }

    public List<UserResponseDTO> getAll() {
        return userRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public UserResponseDTO getById(Long id) {
        return toResponse(findUser(id));
    }

    public UserResponseDTO update(Long id, UserRequestDTO dto) {
        User user = findUser(id);
        if (!user.getEmail().equals(dto.getEmail()) && userRepository.existsByEmail(dto.getEmail()))
            throw new RuntimeException("EMAIL_EXISTS");
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        return toResponse(userRepository.save(user));
    }

    public void changePassword(Long id, PasswordChangeDTO dto) {
        User user = findUser(id);
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword()))
            throw new RuntimeException("WRONG_PASSWORD");
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    public void delete(Long id) {
        userRepository.delete(findUser(id));
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));
    }

    private UserResponseDTO toResponse(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
