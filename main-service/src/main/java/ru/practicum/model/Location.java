package ru.practicum.model;

import lombok.*;

import javax.persistence.Embeddable;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class Location {
    private float lat;
    private float lon;
}
