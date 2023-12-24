package ru.project.entity;


import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import ru.project.entity.enums.UserState;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "app_user")
@Entity
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //like serial
    private Long id;
    private Long telegramUserId;
    @CreationTimestamp // текущая дата на момент сохранения в бд
    private LocalDateTime firstLoginDate;
    private String firstName;
    private String userName;
    private String email;
    private Boolean isActive;
    @Enumerated(EnumType.STRING)
    private UserState userState;


}
