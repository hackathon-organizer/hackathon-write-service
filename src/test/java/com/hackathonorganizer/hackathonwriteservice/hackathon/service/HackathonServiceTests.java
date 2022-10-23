package com.hackathonorganizer.hackathonwriteservice.hackathon.service;

import com.hackathonorganizer.hackathonwriteservice.hackathon.exception.HackathonException;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.Hackathon;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.dto.HackathonRequest;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.dto.HackathonResponse;
import com.hackathonorganizer.hackathonwriteservice.hackathon.repository.HackathonRepository;
import com.hackathonorganizer.hackathonwriteservice.utils.RestCommunicator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

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
    private RestCommunicator restCommunicator;
    @Captor
    private ArgumentCaptor<Hackathon> hackathonCaptor;

    private Hackathon mockHackathon = Hackathon.builder()
            .id(5L)
            .name("Hackathon")
            .description("Desc")
            .organizerInfo("Org Info")
            .isActive(true)
            .eventStartDate(LocalDateTime.of(2022, 12, 12, 13, 0))
            .eventEndDate(LocalDateTime.of(2022, 12, 13, 13, 0))
            .teams(new ArrayList<>())
            .build();

    @Test
    void shouldCreateNewHackathon() {
        // given
        doAnswer(invocation -> invocation.getArgument(0)).when(hackathonRepository)
                .save(any(Hackathon.class));

        // when
        HackathonResponse hackathonResponse =
                hackathonService.createHackathon(buildHackathonRequest());

        // then
        verify(hackathonRepository).save(hackathonCaptor.capture());
        Hackathon captured = hackathonCaptor.getValue();

        assertThat(captured.getName()).isEqualTo(hackathonResponse.name());
        assertThat(captured.getDescription()).isEqualTo(hackathonResponse.description());
    }

    @Test
    void shouldEditHackathon() {
        // given
        Long hackathonId = 5L;

        when(hackathonRepository.findById(hackathonId)).thenReturn(Optional.of(mockHackathon));
        doAnswer(invocation -> invocation.getArgument(0)).when(hackathonRepository)
                .save(any(Hackathon.class));

        // when
        HackathonResponse hackathonResponse = hackathonService
                .updateHackathonData(5L, new HackathonRequest("Edited " +
                        "Hackathon",
                        "Edited Desc", "Org info",
                        LocalDateTime.now(), LocalDateTime.now().plusDays(1), 1L));

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

        //when
        Throwable thrown =
                catchThrowable(() -> hackathonService.updateHackathonData(hackathonId,
                        new HackathonRequest("Edited Hackathon",
                                "Edited Desc", "Org info",
                                LocalDateTime.now(),
                                LocalDateTime.now().plusDays(1), 1L)));

        //then
        verify(hackathonRepository).findById(anyLong());
        assertThat(thrown).isExactlyInstanceOf(HackathonException.class);
    }

    @Test
    void shouldDeactivateHackathon() {
        // given
        Long hackathonId = 5L;

        when(hackathonRepository.findById(hackathonId)).thenReturn(Optional.of(mockHackathon));

        // when
        hackathonService.deactivateHackathon(hackathonId);

        // then
        verify(hackathonRepository).findById(anyLong());
    }

    @Test
    void shouldAddUserToHackathonParticipants() {
        // given
        Long hackathonId = 5L;
        Long userId = 55L;

        when(hackathonRepository.findById(hackathonId)).thenReturn(Optional.of(mockHackathon));
        doAnswer(invocation -> invocation.getArgument(0)).when(hackathonRepository)
                .save(any(Hackathon.class));

        // when
        hackathonService.assignUserToHackathon(hackathonId, userId);

        // then
        verify(hackathonRepository).findById(anyLong());
        verify(hackathonRepository).save(hackathonCaptor.capture());
        Hackathon captured = hackathonCaptor.getValue();

        assertThat(captured.getHackathonParticipantsIds().size()).isEqualTo(1);
    }

    @Test
    void shouldNotAddSameUserTwiceToHackathonParticipants() {
        // given
        Long hackathonId = 5L;
        Long userId = 55L;

        mockHackathon.getHackathonParticipantsIds().add(userId);

        when(hackathonRepository.findById(hackathonId)).thenReturn(Optional.of(mockHackathon));
        doAnswer(invocation -> invocation.getArgument(0)).when(hackathonRepository)
                .save(any(Hackathon.class));

        // when
        hackathonService.assignUserToHackathon(hackathonId, userId);

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

        when(hackathonRepository.findById(hackathonId)).thenReturn(Optional.of(mockHackathon));
        doAnswer(invocation -> invocation.getArgument(0)).when(hackathonRepository)
                .save(any(Hackathon.class));

        // when
        hackathonService.removeUserFromHackathonParticipants(hackathonId, userId);

        // then
        verify(hackathonRepository).findById(anyLong());
        verify(hackathonRepository).save(hackathonCaptor.capture());
        Hackathon captured = hackathonCaptor.getValue();

        assertThat(captured.getHackathonParticipantsIds().size()).isEqualTo(0);
    }

    private HackathonRequest buildHackathonRequest() {
        String name = "Hackathon";
        String desc = "Hackathon desc";
        String organizerInfo = "Organizer info";
        LocalDateTime eventStartDate = LocalDateTime.now();
        LocalDateTime eventEndDate = LocalDateTime.now().plusDays(1);

        return new HackathonRequest(name, desc,
                organizerInfo, eventStartDate, eventEndDate, 1L);
    }
}
