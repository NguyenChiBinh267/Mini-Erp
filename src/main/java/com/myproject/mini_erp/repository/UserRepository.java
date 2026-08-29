package com.myproject.mini_erp.repository;

import com.myproject.mini_erp.entity.User;
import com.myproject.mini_erp.enums.RecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query(value = "SELECT nextval('user_business_id_seq')", nativeQuery = true)
    Long nextUserBusinessId();

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findAllByRecordStatus(RecordStatus recordStatus);

    Optional<User> findByIdAndRecordStatus(Long id, RecordStatus recordStatus);

    List<User> findAllByRecordStatusAndPurgeAtLessThanEqual(RecordStatus recordStatus, LocalDateTime purgeAt);
}
