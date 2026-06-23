package com.example.demo.repo;
import com.example.demo.entity.TempInbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

public interface TempInboxRep extends JpaRepository<TempInbox,Long>{
    Optional<TempInbox> findByToken(String token);
    Optional<TempInbox> findByEmail(String email);
    @Transactional
    void deleteByStworzoneBefore(LocalDateTime expiry);

}
