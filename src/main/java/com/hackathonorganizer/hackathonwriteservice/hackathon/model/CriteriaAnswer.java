package com.hackathonorganizer.hackathonwriteservice.hackathon.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class CriteriaAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer value;

    @ManyToOne
    @JoinColumn(name = "criteria_id")
    private Criteria criteria;
}
