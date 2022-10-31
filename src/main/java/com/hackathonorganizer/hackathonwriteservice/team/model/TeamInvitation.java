package com.hackathonorganizer.hackathonwriteservice.team.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hackathonorganizer.hackathonwriteservice.team.model.Team;
import com.hackathonorganizer.hackathonwriteservice.team.model.InvitationStatus;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Entity(name = "team_invitations")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamInvitation  {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @NotEmpty(message = "From user name can not be empty!")
        private String fromUserName;

        @NotNull(message = "To user id can not be null!")
        private Long toUserId;

        @Enumerated(EnumType.STRING)
        private InvitationStatus invitationStatus;

        @NotEmpty(message = "Team name can not be empty!")
        private String teamName;

        @ManyToOne(fetch = FetchType.LAZY)
        @JsonIgnore
        private Team team;
}
