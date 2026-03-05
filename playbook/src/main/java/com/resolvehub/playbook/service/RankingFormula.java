package com.resolvehub.playbook.service;

import org.springframework.stereotype.Component;

@Component
public class RankingFormula {
    private static final double W_SUCCESS_CONTEXT = 0.55;
    private static final double W_RECENCY = 0.20;
    private static final double W_AUTHOR_EXPERTISE = 0.25;

    public double score(double successRateContext, double recencyDecay, double authorExpertise) {
        return W_SUCCESS_CONTEXT * successRateContext
                + W_RECENCY * recencyDecay
                + W_AUTHOR_EXPERTISE * authorExpertise;
    }
}
