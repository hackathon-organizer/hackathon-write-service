package com.hackathonorganizer.hackathonwriteservice.hackathon.service;

import com.hackathonorganizer.hackathonwriteservice.exception.HackathonException;
import com.hackathonorganizer.hackathonwriteservice.creator.TestDataUtils;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.Criteria;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.CriteriaAnswer;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.Hackathon;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.dto.CriteriaAnswerDto;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.dto.CriteriaRequest;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.dto.HackathonRequest;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.dto.HackathonResponse;
import com.hackathonorganizer.hackathonwriteservice.hackathon.repository.CriteriaAnswerRepository;
import com.hackathonorganizer.hackathonwriteservice.hackathon.repository.CriteriaRepository;
import com.hackathonorganizer.hackathonwriteservice.hackathon.repository.HackathonRepository;
import com.hackathonorganizer.hackathonwriteservice.keycloak.KeycloakService;
import com.hackathonorganizer.hackathonwriteservice.utils.RestCommunicator;
import com.hackathonorganizer.hackathonwriteservice.utils.UserPermissionValidator;
import com.hackathonorganizer.hackathonwriteservice.utils.dto.UserResponseDto;
import com.sun.security.auth.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HackathonServiceTests {

    @InjectMocks
    private HackathonService hackathonService;

    @Mock
    private HackathonRepository hackathonRepository;

    @Mock
    private CriteriaRepository criteriaRepository;

    @Mock
    private CriteriaAnswerRepository criteriaAnswerRepository;

    @Mock
    private UserPermissionValidator userPermissionValidator;

    @Mock
    private RestCommunicator restCommunicator;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private Principal principal;

    @Mock
    private KeycloakService keycloakService;

    @Captor
    private ArgumentCaptor<Hackathon> hackathonCaptor;

    @Captor
    private ArgumentCaptor<Criteria> criteriaCaptor;

    @Captor
    private ArgumentCaptor<CriteriaAnswer> criteriaAnswerCaptor;

    private final Hackathon mockHackathon = TestDataUtils.buildHackathonMock();

    @Test
    void shouldCreateNewHackathon() {
        // given
        doAnswer(invocation -> invocation.getArgument(0)).when(hackathonRepository).save(any(Hackathon.class));

        // when
        HackathonResponse hackathonResponse = hackathonService.createHackathon(TestDataUtils.buildHackathonRequest(), principal);

        // then
        verify(hackathonRepository).save(hackathonCaptor.capture());
        Hackathon captured = hackathonCaptor.getValue();

        assertThat(captured.getName()).isEqualTo(hackathonResponse.name());
        assertThat(captured.getDescription()).isEqualTo(hackathonResponse.description());
    }

    @Test
    void shouldUpdateHackathon() {
        // given
        Long hackathonId = 5L;

        when(hackathonRepository.findById(hackathonId)).thenReturn(Optional.of(mockHackathon));
        doAnswer(invocation -> invocation.getArgument(0)).when(hackathonRepository).save(any(Hackathon.class));

        HackathonRequest hackathonRequest = TestDataUtils.buildHackathonRequest();

        mockUserVerification();

        // when
        HackathonResponse hackathonResponse = hackathonService.updateHackathon(5L, hackathonRequest, principal);

        // then
        verify(hackathonRepository).save(hackathonCaptor.capture());
        Hackathon captured = hackathonCaptor.getValue();

        assertThat(captured.getName()).isEqualTo(hackathonResponse.name());
        assertThat(captured.getDescription()).isEqualTo(hackathonResponse.description());
    }

    @Test
    void shouldThrowErrorWhenHackathonWithGivenIdIsNotFound() {
        //given
        Long hackathonId = 6L;

        when(hackathonRepository.findById(anyLong())).thenReturn(Optional.empty());

        mockUserVerification();

        //when
        Throwable thrown =
                catchThrowable(() -> hackathonService.updateHackathon(hackathonId, TestDataUtils.buildHackathonRequest(),
                        principal));

        //then
        verify(hackathonRepository).findById(hackathonId);
        assertThat(thrown).isExactlyInstanceOf(HackathonException.class);
    }

    @Test
    void shouldAddUserToHackathonParticipants() {
        // given
        Long hackathonId = 5L;
        Long userId = 55L;

        when(hackathonRepository.findById(hackathonId)).thenReturn(Optional.of(mockHackathon));
        doAnswer(invocation -> invocation.getArgument(0)).when(hackathonRepository).save(any(Hackathon.class));

        // when
        hackathonService.assignUserToHackathon(hackathonId, userId, principal);

        // then
        verify(hackathonRepository).findById(anyLong());
        verify(hackathonRepository).save(hackathonCaptor.capture());
        Hackathon captured = hackathonCaptor.getValue();

        assertThat(captured.getHackathonParticipantsIds().size()).isEqualTo(1);
        assertThat(captured.getHackathonParticipantsIds()).contains(userId);
    }

    @Test
    void shouldNotAddSameUserTwiceToHackathonParticipants() {
        // given
        Long hackathonId = 5L;
        Long userId = 55L;

        mockHackathon.getHackathonParticipantsIds().add(userId);

        when(hackathonRepository.findById(hackathonId)).thenReturn(Optional.of(mockHackathon));
        doAnswer(invocation -> invocation.getArgument(0)).when(hackathonRepository).save(any(Hackathon.class));

        // when
        hackathonService.assignUserToHackathon(hackathonId, userId, principal);

        // then
        verify(hackathonRepository).findById(anyLong());
        verify(hackathonRepository).save(hackathonCaptor.capture());

        Hackathon captured = hackathonCaptor.getValue();

        assertThat(captured.getHackathonParticipantsIds().size()).isEqualTo(1);
    }

    @Test
    void shouldRemoveUserFromHackathonParticipants() {
        // given
        Long hackathonId = 5L;
        Long userId = 55L;

        mockHackathon.getHackathonParticipantsIds().add(userId);

        when(hackathonRepository.findById(hackathonId)).thenReturn(Optional.of(mockHackathon));
        doAnswer(invocation -> invocation.getArgument(0)).when(hackathonRepository).save(any(Hackathon.class));

        mockUserVerification();

        // when
        hackathonService.removeUserFromHackathonParticipants(hackathonId, userId, principal);

        // then
        verify(hackathonRepository).findById(anyLong());
        verify(hackathonRepository).save(hackathonCaptor.capture());
        Hackathon captured = hackathonCaptor.getValue();

        assertThat(captured.getHackathonParticipantsIds().size()).isEqualTo(0);
    }

    @Test
    void shouldAddRatingCriteriaToHackathon() {
        // given
        Long hackathonId = 5L;

        CriteriaRequest criteriaRequest = new CriteriaRequest(null, "crit1", hackathonId);
        CriteriaRequest criteriaRequest2 = new CriteriaRequest(null, "crit2", hackathonId);

        List<CriteriaRequest> criteria = List.of(criteriaRequest, criteriaRequest2);

        when(hackathonRepository.findById(hackathonId)).thenReturn(Optional.of(mockHackathon));
        doAnswer(invocation -> invocation.getArgument(0)).when(criteriaRepository).save(any(Criteria.class));

        mockUserVerification();

        // when
        List<CriteriaRequest> result = hackathonService.addRateCriteriaToHackathon(hackathonId, criteria, principal);

        // then
        verify(hackathonRepository).findById(anyLong());
        verify(criteriaRepository, times(2)).save(criteriaCaptor.capture());
        Criteria captured = criteriaCaptor.getValue();

        assertThat(captured.getName()).isEqualTo(result.get(1).name());
    }

    @Test
    void shouldUpdateRatingCriteriaInHackathon() {
        // given
        Long hackathonId = 5L;
        Long criteriaId = 1L;

        CriteriaRequest criteriaRequest = new CriteriaRequest(criteriaId, "crit34", hackathonId);

        List<CriteriaRequest> criteria = List.of(criteriaRequest);

        Criteria criteriaMock = new Criteria(criteriaId, "crit1", mockHackathon, Set.of());

        when(hackathonRepository.findById(hackathonId)).thenReturn(Optional.of(mockHackathon));
        when(criteriaRepository.findById(criteriaRequest.id())).thenReturn(Optional.of(criteriaMock));
        doAnswer(invocation -> invocation.getArgument(0)).when(criteriaRepository).save(any(Criteria.class));

        principal = new UserPrincipal("id");
        when(restCommunicator.getUserByKeycloakId("id")).thenReturn(new UserResponseDto(
                1L, "user", "desc", "id", mockHackathon.getId(), 5L, Set.of()));

        // when
        hackathonService.updateRateCriteriaInHackathon(hackathonId, criteria, principal);

        // then
        verify(hackathonRepository).findById(anyLong());
        verify(criteriaRepository).save(criteriaCaptor.capture());
        Criteria captured = criteriaCaptor.getValue();

        assertThat(captured.getName()).isEqualTo(criteriaRequest.name());
    }

    @Test
    void shouldSaveTeamRatingAnswers() {
        // given
        Long hackathonId = 5L;
        Long criteriaId = 1L;

        CriteriaAnswerDto criteriaDto1 = new CriteriaAnswerDto(null, criteriaId, 50, 6L, 1L);
        CriteriaAnswerDto criteriaDto2 = new CriteriaAnswerDto(null, criteriaId, 70, 6L, 1L);

        List<CriteriaAnswerDto> criteriaAnswers = List.of(criteriaDto1, criteriaDto2);

        Criteria criteriaMock = new Criteria(criteriaId, "crit1", mockHackathon, Set.of());

        when(hackathonRepository.findById(hackathonId)).thenReturn(Optional.of(mockHackathon));
        when(criteriaRepository.findById(criteriaId)).thenReturn(Optional.of(criteriaMock));
        doAnswer(invocation -> invocation.getArgument(0)).when(criteriaAnswerRepository).save(any(CriteriaAnswer.class));

        principal = new UserPrincipal("id");
        when(restCommunicator.getUserByKeycloakId("id")).thenReturn(new UserResponseDto(
                1L, "user", "desc", "id", mockHackathon.getId(), 5L, Set.of()));

        // when
        List<CriteriaAnswerDto> result = hackathonService.saveCriteriaAnswers(hackathonId, criteriaAnswers, principal);

        // then
        verify(hackathonRepository).findById(anyLong());
        verify(criteriaRepository, times(2)).findById(anyLong());
        verify(criteriaAnswerRepository, times(2)).save(criteriaAnswerCaptor.capture());
        CriteriaAnswer captured = criteriaAnswerCaptor.getValue();

        assertThat(captured.getValue()).isEqualTo(result.get(1).value());
        assertThat(captured.getCriteria().getId()).isEqualTo(result.get(1).criteriaId());
        assertThat(result.size()).isEqualTo(criteriaAnswers.size());
    }

    @Test
    void shouldUpdateTeamRatingAnswers() {
        // given
        Long hackathonId = 5L;
        Long criteriaId = 1L;

        CriteriaAnswerDto criteriaAnswerDto = new CriteriaAnswerDto(5L, criteriaId, 50, 6L, 1L);

        List<CriteriaAnswerDto> criteriaAnswers = List.of(criteriaAnswerDto);

        Criteria criteriaMock = new Criteria(criteriaId, "crit1", mockHackathon, Set.of());
        CriteriaAnswer criteriaAnswerMock = new CriteriaAnswer(5L, 80, 6L, 1L, criteriaMock);

        when(hackathonRepository.findById(hackathonId)).thenReturn(Optional.of(mockHackathon));
        when(criteriaRepository.findById(criteriaId)).thenReturn(Optional.of(criteriaMock));
        when(criteriaAnswerRepository.findById(criteriaAnswerDto.id())).thenReturn(Optional.of(criteriaAnswerMock));
        doAnswer(invocation -> invocation.getArgument(0)).when(criteriaAnswerRepository).save(any(CriteriaAnswer.class));

        principal = new UserPrincipal("id");
        when(restCommunicator.getUserByKeycloakId("id")).thenReturn(new UserResponseDto(
                1L, "user", "desc", "id", mockHackathon.getId(), 5L, Set.of()));

        // when
        List<CriteriaAnswerDto> result = hackathonService.saveCriteriaAnswers(hackathonId, criteriaAnswers, principal);

        // then
        verify(hackathonRepository).findById(anyLong());
        verify(criteriaRepository).findById(anyLong());
        verify(criteriaAnswerRepository).save(criteriaAnswerCaptor.capture());
        CriteriaAnswer captured = criteriaAnswerCaptor.getValue();

        assertThat(captured.getValue()).isEqualTo(result.get(0).value());
    }

    @Test
    void shouldDeleteCriteria() {
        // given
        Long hackathonId = 5L;
        Long criteriaId = 1L;

        when(hackathonRepository.findById(hackathonId)).thenReturn(Optional.of(mockHackathon));

        mockUserVerification();

        // when
        hackathonService.deleteCriteria(hackathonId, criteriaId, principal);

        // then
        verify(hackathonRepository).findById(anyLong());
        verify(criteriaRepository).deleteById(anyLong());
    }

    private void mockUserVerification() {
        when(userPermissionValidator.verifyUser(any(Principal.class), anyLong())).thenReturn(true);
    }
}
