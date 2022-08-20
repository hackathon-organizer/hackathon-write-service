package com.hackathonorganizer.hackathonwriteservice.team.model;

import com.hackathonorganizer.hackathonwriteservice.hackathon.model.Hackathon;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Set;

@Entity
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    private Long ownerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotEmpty
    private Hackathon hackathon;

    @ElementCollection
    @CollectionTable(name = "team_members", joinColumns = @JoinColumn(name =
            "team_id"))
    @Column(name = "team_member_id")
    private Set<Long> teamMembersIds;

    @ManyToMany
    @JoinTable(name = "team_tags", joinColumns = @JoinColumn(name = "team_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private List<Tag> tags;
}
