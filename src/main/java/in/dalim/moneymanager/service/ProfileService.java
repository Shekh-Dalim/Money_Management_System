package in.dalim.moneymanager.service;

import in.dalim.moneymanager.dto.AuthDTO;
import in.dalim.moneymanager.dto.ProfileDTO;
import in.dalim.moneymanager.entity.ProfileEntity;
import in.dalim.moneymanager.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    // TODO function is written to register a new user profile
    public ProfileDTO registerProfile(ProfileDTO profileDTO) {

        ProfileEntity newProfile = toEntity(profileDTO);            // TODO: Convert ProfileDTO into ProfileEntity
        newProfile.setActivationToken(UUID.randomUUID().toString()); // TODO: Create a unique activation token for the new profile
        newProfile = profileRepository.save(newProfile);            // TODO: Save the profile data into the database
        // Send activation email
        String activationLink = "http://localhost:8080/api/v1.0/activate?token=" + newProfile.getActivationToken();
        String subject = "Activate your Money Manager account";
        String body = "Click on the following link to activate your account: " + activationLink;
        emailService.sendEmail(newProfile.getEmail(), subject, body);
        return toDTO(newProfile);                                    // TODO: Convert saved Entity back to DTO and return it
    }


    // TODO: Convert ProfileDTO into ProfileEntity to save data in the database
    public ProfileEntity toEntity(ProfileDTO profileDTO) {
        return ProfileEntity.builder()
                .id(profileDTO.getId())
                .fullName(profileDTO.getFullName())
                .email(profileDTO.getEmail())
                .password(passwordEncoder.encode(profileDTO.getPassword()))  // TODO To hash the user's plain-text password before storing it in the database for security.
                .profileImageUrl(profileDTO.getProfileImageUrl())
                .createdAt(profileDTO.getCreatedAt())
                .updatedAt(profileDTO.getUpdatedAt())
                .build();
    }

    // TODO: Convert ProfileEntity into ProfileDTO to send data to the client
    public ProfileDTO toDTO(ProfileEntity profileEntity) {
        return ProfileDTO.builder()
                .id(profileEntity.getId())
                .fullName(profileEntity.getFullName())
                .email(profileEntity.getEmail())
                .profileImageUrl(profileEntity.getProfileImageUrl())
                .createdAt(profileEntity.getCreatedAt())
                .updatedAt(profileEntity.getUpdatedAt())
                .build();
    }

    // TODO This method is used to activate a user's account after they click the activation link sent to their email.
    public boolean activateProfile(String activationToken) {
        return profileRepository.findByActivationToken(activationToken)
                .map(profile -> {
                    profile.setIsActive(true);
                    profileRepository.save(profile);
                    return true;
                })
                .orElse(false);
    }

    // TODO: Check if the user's account is active
    public boolean isAccountActive(String email) {
        return profileRepository.findByEmail(email)
                .map(ProfileEntity::getIsActive)
                .orElse(false);
    }

    // TODO: Get the currently logged-in user's profile
    public ProfileEntity getCurrentProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return profileRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Profile not found with email: " + authentication.getName()));
    }

    //TODO: Get the user's profile
    public ProfileDTO getPublicProfile(String email) {
        ProfileEntity currentUser = null;

        if (email == null) {
            currentUser = getCurrentProfile();
        } else {
            currentUser = profileRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Profile not found with email: " + email));
        }

        return ProfileDTO.builder()
                .id(currentUser.getId())
                .fullName(currentUser.getFullName())
                .email(currentUser.getEmail())
                .profileImageUrl(currentUser.getProfileImageUrl())
                .createdAt(currentUser.getCreatedAt())
                .updatedAt(currentUser.getUpdatedAt())
                .build();
    }


    // TODO This method verifies the user's login credentials and, if valid, returns the JWT token and user's profile.
    public Map<String, Object> authenticateAndGenerateToken(AuthDTO authDTO) {
        try {

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authDTO.getEmail(),
                            authDTO.getPassword()
                    )
            );

            // Generate JWT token
            return Map.of(
                    "token", "JWT token",
                    "user", getPublicProfile(authDTO.getEmail())
            );

        } catch (Exception e) {
            throw new RuntimeException("Invalid email or password");
        }
    }
}
