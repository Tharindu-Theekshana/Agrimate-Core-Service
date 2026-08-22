package com.agrimate.service.model.user;

import com.agrimate.service.model.account.Account;
import com.agrimate.service.model.baseEntity.BaseEntity;
import com.agrimate.service.model.userRole.UserRole;
import com.agrimate.service.model.role.RoleName;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_username", columnList = "username", unique = true),
        @Index(name = "idx_users_email", columnList = "email", unique = true)
})
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @OneToOne(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Account account;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserRole> userRoles = new LinkedHashSet<>();

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }
    public Set<UserRole> getUserRoles() { return userRoles; }
    public void setUserRoles(Set<UserRole> userRoles) { this.userRoles = userRoles; }

    public Set<RoleName> getRoleNames() {
        Set<RoleName> names = new HashSet<>();
        for (UserRole ur : userRoles) {
            if (ur.getRole() != null) names.add(ur.getRole().getName());
        }
        return names;
    }

    public boolean hasRole(RoleName name) {
        return getRoleNames().contains(name);
    }

    public RoleName primaryRole() {
        Set<RoleName> names = getRoleNames();
        if (names.contains(RoleName.ADMIN)) return RoleName.ADMIN;
        if (names.contains(RoleName.AGRONOMIST)) return RoleName.AGRONOMIST;
        return RoleName.FARMER;
    }
}
