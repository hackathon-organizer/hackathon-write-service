package com.hackathonorganizer.hackathonwriteservice.team.model;

import javax.persistence.*;

@Entity
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hackathon_id")
    private Long hackathonId;

    @Column(name = "owner_id")
    private Long ownerId;
}
