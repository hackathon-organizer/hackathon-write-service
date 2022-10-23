package com.hackathonorganizer.hackathonwriteservice.hackathon.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.*;

@Entity
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
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

    @OneToMany(mappedBy = "criteria", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<CriteriaAnswer> criteriaAnswers = new HashSet<>();

    public void addAnswer(CriteriaAnswer answer) {

        criteriaAnswers.add(answer);
    }
}
