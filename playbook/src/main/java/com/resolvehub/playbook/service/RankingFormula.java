package com.resolvehub.playbook.service;

import org.springframework.stereotype.Component;

@Component
public class RankingFormula {
    private static final double W1_VECTOR = 0.25;
    private static final double W2_KEYWORD = 0.20;
    private static final double W3_SUCCESS_CONTEXT = 0.30;
    private static final double W4_RECENCY = 0.15;
    private static final double W5_EXPERTISE = 0.10;

    public double score(double vectorSimilarity, double keywordScore, double successRateContext, double recencyDecay, double authorExpertise) {
        return W1_VECTOR * vectorSimilarity
                + W2_KEYWORD * keywordScore
                + W3_SUCCESS_CONTEXT * successRateContext
                + W4_RECENCY * recencyDecay
                + W5_EXPERTISE * authorExpertise;
    }
}
