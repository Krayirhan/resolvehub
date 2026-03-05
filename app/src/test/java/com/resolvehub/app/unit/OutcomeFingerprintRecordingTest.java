package com.resolvehub.app.unit;

import com.resolvehub.common.model.OutcomeType;
import com.resolvehub.common.util.EnvironmentFingerprintUtil;
import com.resolvehub.playbook.domain.SolutionEntity;
import com.resolvehub.playbook.domain.SolutionOutcomeEntity;
import com.resolvehub.playbook.dto.RecordOutcomeRequest;
import com.resolvehub.playbook.repository.PlaybookRepository;
import com.resolvehub.playbook.repository.PlaybookStepRepository;
import com.resolvehub.playbook.repository.SolutionOutcomeRepository;
import com.resolvehub.playbook.repository.SolutionRepository;
import com.resolvehub.playbook.repository.SolutionVoteRepository;
import com.resolvehub.playbook.service.SolutionService;
import com.resolvehub.problemgraph.repository.ProblemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutcomeFingerprintRecordingTest {
    @Mock
    private SolutionRepository solutionRepository;
    @Mock
    private SolutionVoteRepository solutionVoteRepository;
    @Mock
    private SolutionOutcomeRepository solutionOutcomeRepository;
    @Mock
    private PlaybookRepository playbookRepository;
    @Mock
    private PlaybookStepRepository playbookStepRepository;
    @Mock
    private ProblemRepository problemRepository;

    @Test
    void shouldGenerateEnvironmentFingerprintWhenClientDoesNotProvideIt() {
        SolutionService service = new SolutionService(
                solutionRepository,
                solutionVoteRepository,
                solutionOutcomeRepository,
                playbookRepository,
                playbookStepRepository,
                problemRepository,
                null,
                null
        );

        SolutionEntity solution = new SolutionEntity();
        when(solutionRepository.findById(42L)).thenReturn(Optional.of(solution));
        when(solutionOutcomeRepository.save(any(SolutionOutcomeEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, String> environment = Map.of("os", "linux", "java", "21", "db", "postgres");
        RecordOutcomeRequest request = new RecordOutcomeRequest(OutcomeType.WORKED, environment, null, "Works now");

        service.recordOutcome(42L, 99L, request);

        ArgumentCaptor<SolutionOutcomeEntity> captor = ArgumentCaptor.forClass(SolutionOutcomeEntity.class);
        verify(solutionOutcomeRepository).save(captor.capture());
        String expected = EnvironmentFingerprintUtil.fingerprint(environment);
        assertEquals(expected, captor.getValue().getEnvironmentFingerprint());
    }
}
