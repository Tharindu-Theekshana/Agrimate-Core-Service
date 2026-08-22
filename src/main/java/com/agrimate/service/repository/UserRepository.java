package com.agrimate.service.repository;

import com.agrimate.service.model.user.User;
import com.agrimate.service.model.account.AgronomistStatus;
import com.agrimate.service.model.role.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Query("select distinct u from User u "
            + "left join fetch u.account "
            + "left join fetch u.userRoles ur left join fetch ur.role "
            + "where u.id = :id")
    Optional<User> findDetailById(@Param("id") Long id);

    @Query("select distinct u from User u "
            + "left join fetch u.account "
            + "left join fetch u.userRoles ur left join fetch ur.role "
            + "where lower(u.username) = lower(:identifier) or lower(u.email) = lower(:identifier)")
    Optional<User> findDetailByUsernameOrEmail(@Param("identifier") String identifier);

    @Query("select distinct u from User u "
            + "left join fetch u.account "
            + "left join fetch u.userRoles ur left join fetch ur.role")
    List<User> findAllDetailed();

    @Query("select distinct u from User u "
            + "left join fetch u.account "
            + "join fetch u.userRoles ur join fetch ur.role r "
            + "where r.name = :role")
    List<User> findAllByRole(@Param("role") RoleName role);

    @Query("select count(a) from Account a where a.agronomistStatus = :status")
    long countByAgronomistStatus(@Param("status") AgronomistStatus status);
}
