package com.hackathonorganizer.hackathonwriteservice.team.model;

import com.hackathonorganizer.hackathonwriteservice.hackathon.model.Hackathon;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
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
@ToString
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "Name can not be empty!")
    private String name;

    @NotNull(message = "Owner id can not be null!")
    private Long ownerId;

    @NotEmpty(message = "Description can not be empty!")
    private String description;

    @NotNull
    @Builder.Default
    @ColumnDefault("true")
    private Boolean isOpen = true;

    private Long chatRoomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull(message = "Hackathon can not be null!")
    private Hackathon hackathon;

    @ElementCollection
    @CollectionTable(name = "team_members", joinColumns = @JoinColumn(name = "team_id"))
    @Column(name = "team_member_id")
    @Builder.Default
    private Set<Long> teamMembersIds = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "team_tags", joinColumns = @JoinColumn(name = "team_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    @Builder.Default
    private List<Tag> tags = new ArrayList<>();

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<TeamInvitation> invitations = new HashSet<>();

    public void addUserToTeam(Long userId) {

        if (!teamMembersIds.add(userId)) {
            log.info("User with id: {} is already added to team", userId);
        }
    }

    public void removeUserFromTeam(Long userId) {

        if (!teamMembersIds.remove(userId)) {
            log.info("User with id: {} is not in team", userId);
        }
    }

    public void addUserInvitationToTeam(TeamInvitation teamInvitation) {

        if (!invitations.add(teamInvitation)) {
            log.info("Invitation with id: {} already added to team", teamInvitation.getId());
        }
    }
}
