package com.hackathonorganizer.hackathonwriteservice.team.service;

import com.hackathonorganizer.hackathonwriteservice.exception.TeamException;
import com.hackathonorganizer.hackathonwriteservice.creator.TestDataUtils;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.Hackathon;
import com.hackathonorganizer.hackathonwriteservice.hackathon.service.HackathonService;
import com.hackathonorganizer.hackathonwriteservice.keycloak.KeycloakService;
import com.hackathonorganizer.hackathonwriteservice.team.model.InvitationStatus;
import com.hackathonorganizer.hackathonwriteservice.team.model.Team;
import com.hackathonorganizer.hackathonwriteservice.team.model.TeamInvitation;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamInvitationRequest;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamRequest;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamResponse;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamVisibilityStatusRequest;
import com.hackathonorganizer.hackathonwriteservice.team.repository.TeamInvitationRepository;
import com.hackathonorganizer.hackathonwriteservice.team.repository.TeamRepository;
import com.hackathonorganizer.hackathonwriteservice.utils.RestCommunicator;
import com.hackathonorganizer.hackathonwriteservice.utils.UserPermissionValidator;
import com.hackathonorganizer.hackathonwriteservice.utils.dto.UserResponseDto;
import com.hackathonorganizer.hackathonwriteservice.websocket.service.NotificationService;
import com.sun.security.auth.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.Mockito.*;

class TeamServiceTest {

    @InjectMocks
    private TeamService teamService;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamInvitationRepository teamInvitationRepository;

    @Mock
    private HackathonService hackathonService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private RestCommunicator restCommunicator;

    @Mock
    private KeycloakService keycloakService;

    @Mock
    private UserPermissionValidator userPermissionValidator;

    @Mock
    private Principal principal;

    @Captor
    private ArgumentCaptor<Team> teamCaptor;

    @Captor
    private ArgumentCaptor<TeamInvitation> teamInvitationCaptor;

    private final Hackathon mockHackathon = TestDataUtils.buildHackathonMock();
    private final Team mockTeam = TestDataUtils.buildTeamMock();
    private final TeamInvitation teamInvitationMock = TestDataUtils.buildTeamInvitationMock();

    private final TeamRequest teamRequest = TestDataUtils.buildTeamRequest();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreateTeam() {
        // given
        when(hackathonService.getHackathonById(anyLong())).thenReturn(mockHackathon);
        when(hackathonService.isUserHackathonParticipant(anyLong(), anyLong())).thenReturn(true);
        doAnswer(invocation -> invocation.getArgument(0)).when(teamRepository).save(any(Team.class));

        mockUserVerification();

        // when

        TeamResponse result = teamService.createTeam(teamRequest, principal);

        // then

        verify(hackathonService).getHackathonById(anyLong());
        verify(hackathonService).isUserHackathonParticipant(anyLong(), anyLong());
        verify(teamRepository, times(2)).save(teamCaptor.capture());
        Team team = teamCaptor.getValue();

        assertThat(team.getOwnerId()).isEqualTo(result.ownerId());
        assertThat(team.getTags()).isEqualTo(result.tags());
        assertThat(team.getHackathon().getId()).isEqualTo(result.hackathonId());
        assertThat(team.getId()).isEqualTo(team.getChatRoomId());
    }

    @Test
    void shouldEditTeamById() {
        // given
        when(teamRepository.findById(anyLong())).thenReturn(Optional.of(mockTeam));
        doAnswer(invocation -> invocation.getArgument(0)).when(teamRepository).save(any(Team.class));

        mockUserVerification();

        TeamRequest updatedTeam = new TeamRequest(
                1L,
                "update name",
                true,
                "update description",
                mockHackathon.getId(),
                List.of());

        // when

        TeamResponse result = teamService.updateTeamById(5L, updatedTeam, principal);

        // then

        verify(teamRepository).findById(anyLong());
        verify(teamRepository).save(teamCaptor.capture());
        Team team = teamCaptor.getValue();

        assertThat(team.getName()).isEqualTo(result.name());
        assertThat(team.getDescription()).isEqualTo(result.description());
    }

    @Test
    void shouldProcessInvitation() {
        // given
        when(teamRepository.findById(anyLong())).thenReturn(Optional.of(mockTeam));
        doAnswer(invocation -> invocation.getArgument(0)).when(teamInvitationRepository).save(any(TeamInvitation.class));
        doAnswer(invocation -> invocation.getArgument(0)).when(teamRepository).save(any(Team.class));

        // when

        teamService.processInvitation(5L, 15L, "fromUserUsername");

        // then
        verify(teamRepository).findById(anyLong());
        verify(teamRepository).save(teamCaptor.capture());
        verify(teamInvitationRepository).save(teamInvitationCaptor.capture());
        Team team = teamCaptor.getValue();
        TeamInvitation teamInvitation = teamInvitationCaptor.getValue();

        assertThat(teamInvitation.getInvitationStatus()).isEqualTo(InvitationStatus.PENDING);
        assertThat(team.getInvitations().size()).isEqualTo(1);
    }

    @Test
    void shouldUpdateInvitationStatusAccepted() {
        // given
        when(teamRepository.findById(anyLong())).thenReturn(Optional.of(mockTeam));
        when(teamInvitationRepository.findById(anyLong())).thenReturn(Optional.of(teamInvitationMock));
        doAnswer(invocation -> invocation.getArgument(0)).when(teamInvitationRepository).save(any(TeamInvitation.class));
        doAnswer(invocation -> invocation.getArgument(0)).when(teamRepository).save(any(Team.class));

        TeamInvitationRequest teamInvitationRequest = new TeamInvitationRequest(
                teamInvitationMock.getId(),
                "fromUsername",
                15L,
                InvitationStatus.ACCEPTED,
                mockTeam.getName(),
                mockTeam.getId());

        // when

        teamService.updateInvitationStatus(teamInvitationRequest, principal);

        // then
        verify(teamRepository).findById(anyLong());
        verify(teamRepository).save(teamCaptor.capture());
        verify(teamInvitationRepository).save(teamInvitationCaptor.capture());
        Team team = teamCaptor.getValue();
        TeamInvitation teamInvitation = teamInvitationCaptor.getValue();

        assertThat(teamInvitation.getInvitationStatus()).isEqualTo(InvitationStatus.ACCEPTED);
        assertThat(team.getTeamMembersIds().size()).isEqualTo(1);
    }

    @Test
    void shouldUpdateInvitationStatusRejected() {
        // given
        when(teamRepository.findById(anyLong())).thenReturn(Optional.of(mockTeam));
        when(teamInvitationRepository.findById(anyLong())).thenReturn(Optional.of(teamInvitationMock));
        doAnswer(invocation -> invocation.getArgument(0)).when(teamInvitationRepository).save(any(TeamInvitation.class));

        TeamInvitationRequest teamInvitationRequest = new TeamInvitationRequest(
                teamInvitationMock.getId(),
                "fromUsername",
                15L,
                InvitationStatus.REJECTED,
                mockTeam.getName(),
                mockTeam.getId());

        // when

        teamService.updateInvitationStatus(teamInvitationRequest, principal);

        // then
        verify(teamRepository).findById(anyLong());
        verify(teamInvitationRepository).save(teamInvitationCaptor.capture());
        TeamInvitation teamInvitation = teamInvitationCaptor.getValue();

        assertThat(teamInvitation.getInvitationStatus()).isEqualTo(InvitationStatus.REJECTED);
    }

    @Test
    void shouldAddUserToTeam() {
        // given
        when(teamRepository.findById(anyLong())).thenReturn(Optional.of(mockTeam));
        doAnswer(invocation -> invocation.getArgument(0)).when(teamRepository).save(any(Team.class));

        // when

        teamService.addUserToTeam(mockTeam.getId(), 1L, principal);

        // then
        verify(teamRepository).findById(anyLong());
        verify(teamRepository).save(teamCaptor.capture());
        Team team = teamCaptor.getValue();

        assertThat(team.getTeamMembersIds().size()).isEqualTo(1);
    }

    @Test
    void shouldNotAddUserToTeam() {
        // given
        when(teamRepository.findById(anyLong())).thenReturn(Optional.of(mockTeam));
        doAnswer(invocation -> invocation.getArgument(0)).when(teamRepository).save(any(Team.class));

        mockTeam.setIsOpen(false);

        // when

        Throwable thrown =
                catchThrowable(() -> teamService.addUserToTeam(mockTeam.getId(), 1L, principal));

        // then
        verify(teamRepository).findById(anyLong());
        assertThat(thrown).isExactlyInstanceOf(TeamException.class);
    }

    @Test
    void shouldCloseTeamForMembers() {
        //given

        when(teamRepository.findById(anyLong())).thenReturn(Optional.of(mockTeam));
        doAnswer(invocation -> invocation.getArgument(0)).when(teamRepository).save(any(Team.class));

        // when

        boolean result = teamService.openOrCloseTeamForMembers(
                mockTeam.getId(),
                new TeamVisibilityStatusRequest(1L, false));

        //then

        verify(teamRepository).findById(anyLong());
        verify(teamRepository).save(teamCaptor.capture());
        Team team = teamCaptor.getValue();

        assertThat(result).isFalse();
        assertThat(team.getIsOpen()).isFalse();
    }

    @Test
    void shouldOpenTeamForMembers() {
        //given

        when(teamRepository.findById(anyLong())).thenReturn(Optional.of(mockTeam));
        doAnswer(invocation -> invocation.getArgument(0)).when(teamRepository).save(any(Team.class));

        mockTeam.setIsOpen(false);

        // when

        boolean result = teamService.openOrCloseTeamForMembers(
                mockTeam.getId(),
                new TeamVisibilityStatusRequest(1L, true));

        //then

        verify(teamRepository).findById(anyLong());
        verify(teamRepository).save(teamCaptor.capture());
        Team team = teamCaptor.getValue();

        assertThat(result).isTrue();
        assertThat(team.getIsOpen()).isTrue();
    }

    @Test
    void shouldNotOpenTeamForMembers() {
        //given

        when(teamRepository.findById(anyLong())).thenReturn(Optional.of(mockTeam));

        mockTeam.setIsOpen(false);

        // when

        Throwable thrown =
                catchThrowable(() -> teamService.openOrCloseTeamForMembers(
                        mockTeam.getId(),
                        new TeamVisibilityStatusRequest(111L, true)));

        //then

        verify(teamRepository).findById(anyLong());

        assertThat(thrown).isExactlyInstanceOf(TeamException.class);
    }

    private void mockUserVerification() {
        principal = new UserPrincipal("id");
        when(restCommunicator.getUserByKeycloakId(anyString())).thenReturn(new UserResponseDto(
                1L, "user", "desc", "id", mockHackathon.getId(), 5L, Set.of()));
        when(userPermissionValidator.verifyUser(any(Principal.class), anyLong())).thenReturn(true);
    }
}