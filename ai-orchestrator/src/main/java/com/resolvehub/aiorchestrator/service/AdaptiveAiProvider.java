package com.resolvehub.aiorchestrator.service;

import com.resolvehub.aiorchestrator.config.AiProviderProperties;
import com.resolvehub.aiorchestrator.dto.ProblemTriageResult;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Primary
public class AdaptiveAiProvider implements AiProvider {
    private final AiProvider delegate;

    public AdaptiveAiProvider(AiProviderProperties properties, OpenAiCompatibleProvider openAi, StubAiProvider stubAiProvider) {
        boolean useRealProvider = properties.isEnabled()
                && properties.getBaseUrl() != null && !properties.getBaseUrl().isBlank()
                && properties.getApiKey() != null && !properties.getApiKey().isBlank();
        this.delegate = useRealProvider ? openAi : stubAiProvider;
    }

    @Override
    public ProblemTriageResult triageProblem(String title, String description) {
        return delegate.triageProblem(title, description);
    }

    @Override
    public List<Double> embed(String content) {
        return delegate.embed(content);
    }

    @Override
    public String providerName() {
        return delegate.providerName();
    }
}
