package com.hackathonorganizer.hackathonwriteservice.hackathon.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Hackathon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    @Column(name = "organizer_info")
    private String organizerInfo;

    @Column(name = "event_start_date")
    private LocalDateTime eventStartDate;

    @Column(name = "event_end_date")
    private LocalDateTime eventEndDate;
}
