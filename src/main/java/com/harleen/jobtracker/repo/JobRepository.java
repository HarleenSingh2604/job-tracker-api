package com.harleen.jobtracker.repo;

import com.harleen.jobtracker.model.JobApplication;
import com.harleen.jobtracker.model.Status;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByOwner(String owner);
    List<JobApplication> findByOwnerAndStatus(String owner, Status status);
    Optional<JobApplication> findByIdAndOwner(Long id, String owner);
    List<JobApplication> findByOwnerAndStatusAndFollowUpDateLessThanEqual(String owner, Status status, LocalDate date);
}
