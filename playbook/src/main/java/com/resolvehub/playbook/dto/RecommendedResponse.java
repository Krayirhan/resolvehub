package com.resolvehub.playbook.dto;

import java.util.List;

public record RecommendedResponse(
        List<SimilarProblemCard> similarProblems,
        List<RecommendedPlaybookCard> topPlaybooks,
        List<RecommendedSolutionCard> topSolutions
) {
    public record SimilarProblemCard(
            Long problemId,
            String title,
            double score,
            String reason
    ) {
    }

    public record RecommendedPlaybookCard(
            Long playbookId,
            String title,
            Long sourceSolutionId,
            RankingBreakdown ranking
    ) {
    }

    public record RecommendedSolutionCard(
            Long solutionId,
            String summary,
            RankingBreakdown ranking
    ) {
    }

    public record RankingBreakdown(
            double totalScore,
            double successRateContext,
            double recencyDecay,
            double authorExpertise,
            String explanation
    ) {
    }
}
