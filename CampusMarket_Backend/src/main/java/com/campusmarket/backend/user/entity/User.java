package com.campusmarket.backend.user.entity;

import com.campusmarket.backend.college.entity.College;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password; // BCrypt hash, never plain text

    @Column(nullable = false)
    private boolean verified = false;

    @Column(name = "joined_date", nullable = false)
    private LocalDateTime joinedDate = LocalDateTime.now();

    private String verificationToken;
private LocalDateTime verificationTokenExpiry;

public String getVerificationToken() { return verificationToken; }
public void setVerificationToken(String verificationToken) { this.verificationToken = verificationToken; }

public LocalDateTime getVerificationTokenExpiry() { return verificationTokenExpiry; }
public void setVerificationTokenExpiry(LocalDateTime verificationTokenExpiry) { this.verificationTokenExpiry = verificationTokenExpiry; }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    // getters and setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public LocalDateTime getJoinedDate() { return joinedDate; }
    public void setJoinedDate(LocalDateTime joinedDate) { this.joinedDate = joinedDate; }

    public College getCollege() { return college; }
    public void setCollege(College college) { this.college = college; }
}