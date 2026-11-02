package com.harleen.jobtracker.web;

import com.harleen.jobtracker.dto.Dtos.JobRequest;
import com.harleen.jobtracker.model.JobApplication;
import com.harleen.jobtracker.model.Status;
import com.harleen.jobtracker.repo.JobRepository;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/jobs")
public class JobController {
    private final JobRepository jobs;

    public JobController(JobRepository jobs) {
        this.jobs = jobs;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobApplication create(@Valid @RequestBody JobRequest req, Principal me) {
        JobApplication job = new JobApplication();
        job.setOwner(me.getName());
        apply(job, req);
        return jobs.save(job);
    }

    @GetMapping
    public List<JobApplication> list(@RequestParam(required = false) Status status, Principal me) {
        return status == null ? jobs.findByOwner(me.getName())
                              : jobs.findByOwnerAndStatus(me.getName(), status);
    }

    @GetMapping("/follow-ups")
    public List<JobApplication> followUpsDue(Principal me) {
        return jobs.findByOwnerAndStatusAndFollowUpDateLessThanEqual(me.getName(), Status.APPLIED, LocalDate.now());
    }

    @GetMapping("/{id}")
    public JobApplication get(@PathVariable Long id, Principal me) {
        return find(id, me);
    }

    @PutMapping("/{id}")
    public JobApplication update(@PathVariable Long id, @Valid @RequestBody JobRequest req, Principal me) {
        JobApplication job = find(id, me);
        apply(job, req);
        return jobs.save(job);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Principal me) {
        jobs.delete(find(id, me));
    }

    private JobApplication find(Long id, Principal me) {
        return jobs.findByIdAndOwner(id, me.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));
    }

    private void apply(JobApplication job, JobRequest req) {
        job.setCompany(req.company());
        job.setTitle(req.title());
        job.setLocation(req.location());
        job.setStatus(req.status() == null ? Status.APPLIED : req.status());
        LocalDate applied = req.appliedDate() == null ? LocalDate.now() : req.appliedDate();
        job.setAppliedDate(applied);
        job.setFollowUpDate(req.followUpDate() == null ? applied.plusDays(5) : req.followUpDate());
        job.setNotes(req.notes());
    }
}
