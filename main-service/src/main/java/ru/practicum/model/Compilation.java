package ru.practicum.model;

import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "compilations")
public class Compilation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToMany
    @JoinTable(
            name = "event_compilation",
            joinColumns = @JoinColumn(name = "c_id"),
            inverseJoinColumns = @JoinColumn(name = "e_id"))
    @ToString.Exclude
    private Set<Event> events;
    private Boolean pinned;
    private String title;
}
