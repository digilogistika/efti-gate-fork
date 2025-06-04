package eu.efti.authorityapp.service;

import eu.efti.authorityapp.entity.AuthorityConfigEntity;
import eu.efti.authorityapp.repository.AuthorityConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfigService {
    
    private final AuthorityConfigRepository authorityConfigRepository;
    
    @Transactional
    public void saveApiKey(String apiKey) {
        log.info("Saving API key to database");
        
        Optional<AuthorityConfigEntity> existingConfig = authorityConfigRepository.getConfig();
        
        if (existingConfig.isPresent()) {
            authorityConfigRepository.delete(existingConfig.get());
            log.info("Deleted existing API key entry");
        }
        
        AuthorityConfigEntity config = AuthorityConfigEntity.builder()
                .gateApiKey(apiKey)
                .build();
        authorityConfigRepository.save(config);
        log.info("Saved new API key to database");
    }
    
    public String getApiKey() {
        return authorityConfigRepository.getConfig()
                .map(AuthorityConfigEntity::getGateApiKey).orElseThrow();
    }
    
    public boolean hasApiKey() {
        return authorityConfigRepository.hasConfig();
    }
    
    @Transactional
    public void clearApiKey() {
        log.info("Clearing API key from database");
        authorityConfigRepository.deleteAll();
    }
}