package com.example.aeternus_back_end.controller;

import com.example.aeternus_back_end.model.Project;
import com.example.aeternus_back_end.repository.ProjectRepository;
import com.example.aeternus_back_end.service.SupabaseStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectRepository projectRepository;
    private final SupabaseStorageService fileStorageService;

    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects() {
        return ResponseEntity.ok(projectRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable String id) {
        return projectRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody Project project) {
        if (project.getImages() != null) {
            for (int i = 0; i < project.getImages().size(); i++) {
                var img = project.getImages().get(i);
                if (img.getImageUrl() != null) {
                    img.setImageUrl(fileStorageService.uploadBase64Image(img.getImageUrl()));
                }
                if (img.getSortOrder() == null) {
                    img.setSortOrder(i);
                }
                img.setProject(project);
            }
        }
        return ResponseEntity.ok(projectRepository.save(project));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Project> updateProject(@PathVariable String id, @RequestBody Project projectDetails) {
        return projectRepository.findById(id)
                .map(project -> {
                    project.setTitle(projectDetails.getTitle());
                    project.setCategory(projectDetails.getCategory());
                    project.setLocation(projectDetails.getLocation());
                    project.setDescription(projectDetails.getDescription());
                    project.setStatus(projectDetails.getStatus());
                    project.setDate(projectDetails.getDate());
                    // update images safely here if needed
                    return ResponseEntity.ok(projectRepository.save(project));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProject(@PathVariable String id) {
        return projectRepository.findById(id)
                .map(project -> {
                    projectRepository.delete(project);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
