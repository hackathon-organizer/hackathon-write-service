package com.hackathonorganizer.hackathonwriteservice.hackathon.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
public class CriteriaAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer value;

    @NotNull(message = "Team can not be null!")
    private Long teamId;

    @NotNull(message = "User id can not be null!")
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "criteria_id")
    private Criteria criteria;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CriteriaAnswer that = (CriteriaAnswer) o;

        if (!Objects.equals(teamId, that.teamId))
            return false;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        int result = teamId != null ? teamId.hashCode() : 0;
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        return result;
    }
}
