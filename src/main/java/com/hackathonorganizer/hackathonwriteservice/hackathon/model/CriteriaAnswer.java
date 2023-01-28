package com.hackathonorganizer.hackathonwriteservice.hackathon.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CriteriaAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer value;

    @NotNull
    private Long teamId;

    @NotNull
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "criteria_id")
    private Criteria criteria;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CriteriaAnswer answer)) return false;

        if (!Objects.equals(id, answer.id)) return false;
        if (!userId.equals(answer.userId)) return false;
        return criteria.equals(answer.criteria);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + userId.hashCode();
        result = 31 * result + criteria.hashCode();
        return result;
    }
}
