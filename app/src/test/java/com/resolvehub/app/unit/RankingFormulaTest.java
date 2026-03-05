package com.resolvehub.app.unit;

import com.resolvehub.playbook.service.RankingFormula;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RankingFormulaTest {
    private final RankingFormula rankingFormula = new RankingFormula();

    @Test
    void shouldPrioritizeSuccessRateContextStrongly() {
        double highContext = rankingFormula.score(1.0, 0.5, 0.5);
        double lowContext = rankingFormula.score(0.0, 0.5, 0.5);
        assertTrue(highContext > lowContext);
    }

    @Test
    void shouldProduceHigherScoreForGenerallyBetterCandidate() {
        double strong = rankingFormula.score(0.9, 0.9, 0.8);
        double weak = rankingFormula.score(0.1, 0.3, 0.2);
        assertTrue(strong > weak);
    }
}
