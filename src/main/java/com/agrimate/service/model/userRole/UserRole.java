package com.agrimate.service.model.userRole;

import com.agrimate.service.model.baseEntity.BaseEntity;
import com.agrimate.service.model.role.Role;
import com.agrimate.service.model.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** Join entity assigning a {@link Role} to a {@link User} (many-to-many). */
@Entity
@Table(name = "user_roles", uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_role", columnNames = {"user_id", "role_id"})
})
public class UserRole extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    public UserRole() {}

    public UserRole(User user, Role role) {
        this.user = user;
        this.role = role;
    }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
