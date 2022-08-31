package com.hackathonorganizer.hackathonwriteservice.team.utils.model;

import com.hackathonorganizer.hackathonwriteservice.team.model.Team;
import lombok.*;

import javax.persistence.*;
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

        @NotNull
        private Long fromUserId;

        @NotNull
        private Long toUserId;

        @Enumerated(EnumType.STRING)
        private InvitationStatus invitationStatus;

        @NotNull
        private String teamName;

//        @NotNull
//        private Long teamId;

        @ManyToOne(fetch = FetchType.LAZY)
        private Team team;
}
