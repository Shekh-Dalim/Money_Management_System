package in.dalim.moneymanager.repository;

import in.dalim.moneymanager.entity.ProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<ProfileEntity ,Long> {

    //TODO Select * from tbl_profiles where email = ?
    Optional<ProfileEntity> findByEmail(String email);

}
