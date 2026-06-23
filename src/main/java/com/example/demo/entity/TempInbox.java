package com.example.demo.entity;

import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "temp_inboxes")
public class TempInbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private String token;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime stworzone;

    @OneToMany(mappedBy = "inbox", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ReceivedEmail> emails;

    public TempInbox() {
    }

    public TempInbox(String email, String token) {
        this.email = email;
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getStworzone() {
        return stworzone;
    }

    public void setStworzone(LocalDateTime stworzone) {
        this.stworzone = stworzone;
    }

    public List<ReceivedEmail> getEmails() {
        return emails;
    }

    public void setEmails(List<ReceivedEmail> emails) {
        this.emails = emails;
    }
}
