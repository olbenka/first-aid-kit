package ru.project.entity;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.persistence.*;

@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@Table(name = "raw_data")
@Entity
public class RawData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //like serial
    private Long id;
    @Type(type="jsonb")
    @Column(columnDefinition = "jsonb")
    private Update event;
}
