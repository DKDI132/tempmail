package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "received_emails")
public class ReceivedEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sender;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime receivedAt;

    @ManyToOne
    @JoinColumn(name = "inbox_id", nullable = false)
    @JsonIgnore
    private TempInbox inbox;

    public ReceivedEmail() {
    }

    public ReceivedEmail(String sender, String subject, String body, TempInbox inbox) {
        this.sender = sender;
        this.subject = subject;
        this.body = body;
        this.inbox = inbox;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    public TempInbox getInbox() {
        return inbox;
    }

    public void setInbox(TempInbox inbox) {
        this.inbox = inbox;
    }
}
