package com.example.aeternus_back_end.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "services")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceInfo {

    @Id
    private String id; // we use slug as id, e.g., 'building-construction'

    @Column(nullable = false)
    private String title;

    private String icon;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    @ElementCollection
    @CollectionTable(name = "service_benefits", joinColumns = @JoinColumn(name = "service_id"))
    @Column(name = "benefit_text")
    @Builder.Default
    private List<String> benefits = new ArrayList<>();

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<ServiceProcessStep> process = new ArrayList<>();

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<ServiceFaq> faqs = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public void addProcessStep(ServiceProcessStep step) {
        process.add(step);
        step.setService(this);
    }

    public void removeProcessStep(ServiceProcessStep step) {
        process.remove(step);
        step.setService(null);
    }

    public void addFaq(ServiceFaq faq) {
        faqs.add(faq);
        faq.setService(this);
    }

    public void removeFaq(ServiceFaq faq) {
        faqs.remove(faq);
        faq.setService(null);
    }
}
