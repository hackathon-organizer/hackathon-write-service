package com.hackathonorganizer.hackathonwriteservice.hackathon.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

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

    @NotEmpty(message = "Name can not be empty!")
    private String name;

    @NotNull(message = "Hackathon can not be null!")
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
