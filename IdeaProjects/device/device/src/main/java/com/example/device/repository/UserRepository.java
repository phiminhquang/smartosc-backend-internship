package com.example.device.repository;

import com.example.device.model.User;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.email = :email")
    Optional<User> findByEmailForUpdate(@Param("email") String email);

    boolean existsByEmail(String email);

    List<User> findDistinctByRoles_Name(String roleName);
    List<User> findDistinctByRoles_NameIn(List<String> roleNames);

    @Query("""
    select
        count(distinct u.id) as totalUsers,
        coalesce(sum(case when r.name = :employee then 1 else 0 end), 0) as employees,
        coalesce(sum(case when r.name = :itStaff then 1 else 0 end), 0) as itStaff,
        coalesce(sum(case when r.name = :admin then 1 else 0 end), 0) as admins
    from User u
    left join u.roles r
""")
    UserStatistics getStatistics(
            @Param("employee") String employee,
            @Param("itStaff") String itStaff,
            @Param("admin") String admin
    );

    interface UserStatistics {
        long getTotalUsers();
        long getEmployees();
        long getItStaff();
        long getAdmins();
    }

    @Query("""
    select distinct u from User u
    left join fetch u.roles
    where u.id in :ids
""")
    List<User> findAllWithRolesByIdIn(@Param("ids") List<UUID> ids);

}
