package com.hackathonorganizer.hackathonwriteservice.hackathon.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Criteria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    private String name;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "hackathon_id")
    private Hackathon hackathon;

    @OneToMany(mappedBy = "criteria")
    @Builder.Default
    private List<CriteriaAnswer> criteriaAnswers = new ArrayList<>();
}
