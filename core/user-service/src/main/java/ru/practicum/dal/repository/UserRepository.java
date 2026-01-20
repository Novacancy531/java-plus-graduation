package ru.practicum.dal.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.dal.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
