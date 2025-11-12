package com.keneyamuso.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ordonnances")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ordonnance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patiente_id", nullable = false)
    private Patiente patiente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medecin_id", nullable = false)
    private ProfessionnelSante medecin;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_prenatale_id", unique = true)
    private ConsultationPrenatale consultationPrenatale;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_postnatale_id", unique = true)
    private ConsultationPostnatale consultationPostnatale;

    @ElementCollection
    @CollectionTable(name = "ordonnance_medicaments", joinColumns = @JoinColumn(name = "ordonnance_id"))
    private List<Medicament> medicaments = new ArrayList<>();
    
    @Column(columnDefinition = "TEXT")
    private String observations;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @LastModifiedDate
    private LocalDateTime dateModification;
}
