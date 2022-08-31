package com.hackathonorganizer.hackathonwriteservice.team.model;

import com.hackathonorganizer.hackathonwriteservice.hackathon.model.Hackathon;
import com.hackathonorganizer.hackathonwriteservice.team.utils.model.TeamInvitation;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @NotEmpty
    private Long ownerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotEmpty
    private Hackathon hackathon;

    @ElementCollection
    @CollectionTable(name = "team_members", joinColumns = @JoinColumn(name =
            "team_id"))
    @Column(name = "team_member_id")
    private Set<Long> teamMembersIds = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "team_tags", joinColumns = @JoinColumn(name = "team_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private List<Tag> tags;

    @OneToMany(mappedBy = "team")
    private Set<TeamInvitation> invitations = new HashSet<>();

    public void addUserToTeam(Long userId) {

        if (!teamMembersIds.add(userId)) {
            log.info("User with id: {} is already added to team", userId);
        }
    }

    public void removeUserFromTeam(Long userId) {

        if (!teamMembersIds.remove(userId)) {
            log.info("User with id: {} is already added to team", userId);
        }
    }

    public void addUserInvitationToTeam(TeamInvitation teamInvitation) {
        invitations.add(teamInvitation);
    }
}
