package com.academiaindustry.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "institutions")
public class Institution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String name;

    @Size(max = 1000)
    @Column(length = 1000)
    private String website;

    @Size(max = 200)
    @Column(length = 200)
    private String location;

    @Size(max = 2000)
    @Column(length = 2000)
    private String description;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_user_id", unique = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private User account;

    protected Institution() {
    }

    public Institution(String name) {
        this.name = name;
    }

    public Institution(String name, String website, String location, String description, User account) {
        this.name = name;
        this.website = website;
        this.location = location;
        this.description = description;
        this.account = account;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public User getAccount() { return account; }
    public void setAccount(User account) { this.account = account; }
}