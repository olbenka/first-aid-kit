package ru.project.entity;

import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "medication")
@Entity
public class Medication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Integer quantity;

    private LocalDate expiryDate;
    private String briefInfo;

    @OneToOne
    private AppPhoto photo;

    @OneToOne
    private AppDocument document;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private AppUser user;
}
