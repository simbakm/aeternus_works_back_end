package com.example.aeternus_back_end.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "renovation_tips")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RenovationTip {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "renovation_idea_id", nullable = false)
    @JsonIgnore
    private RenovationIdea renovationIdea;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String tipText;

    @Column(nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;
}
