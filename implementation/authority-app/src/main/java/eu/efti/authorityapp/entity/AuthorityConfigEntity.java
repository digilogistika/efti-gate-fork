package eu.efti.authorityapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "authority_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthorityConfigEntity {
    
    @Id
    @Column(name = "gate_api_key", nullable = false, unique = true, length = 100)
    private String gateApiKey;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}