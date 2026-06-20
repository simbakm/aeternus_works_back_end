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
@Table(name = "renovation_ideas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RenovationIdea {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String beforeImageUrl;

    @Column(columnDefinition = "TEXT")
    private String afterImageUrl;

    @Column(columnDefinition = "TEXT")
    private String advice;

    @OneToMany(mappedBy = "renovationIdea", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<RenovationTip> tips = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public void addTip(RenovationTip tip) {
        tips.add(tip);
        tip.setRenovationIdea(this);
    }

    public void removeTip(RenovationTip tip) {
        tips.remove(tip);
        tip.setRenovationIdea(null);
    }
}
