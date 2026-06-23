package com.example.demo.repo;
import com.example.demo.entity.ReceivedEmail;
import org.springframework.data.jpa.repository.JpaRepository;


public interface MailsRep extends JpaRepository<ReceivedEmail,Long> {
}
