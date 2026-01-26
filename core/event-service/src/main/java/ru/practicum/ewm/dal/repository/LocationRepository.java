package ru.practicum.ewm.dal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.dal.entity.Location;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findFirstByLatAndLon(Float lat, Float lon);
}
