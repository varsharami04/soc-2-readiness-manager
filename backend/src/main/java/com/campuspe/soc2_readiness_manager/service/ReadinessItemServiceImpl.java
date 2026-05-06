package com.campuspe.soc2_readiness_manager.service;

import static com.campuspe.soc2_readiness_manager.config.CacheConfig.READINESS_ITEM_BY_ID_CACHE;
import static com.campuspe.soc2_readiness_manager.config.CacheConfig.READINESS_ITEMS_LIST_CACHE;
import static com.campuspe.soc2_readiness_manager.config.CacheConfig.READINESS_ITEMS_PAGE_CACHE;

import com.campuspe.soc2_readiness_manager.dto.ReadinessItemRequest;
import com.campuspe.soc2_readiness_manager.entity.ReadinessItem;
import com.campuspe.soc2_readiness_manager.exception.BusinessValidationException;
import com.campuspe.soc2_readiness_manager.exception.DuplicateControlReferenceException;
import com.campuspe.soc2_readiness_manager.exception.InvalidDueDateException;
import com.campuspe.soc2_readiness_manager.exception.InvalidReadinessScoreException;
import com.campuspe.soc2_readiness_manager.exception.ResourceNotFoundException;
import com.campuspe.soc2_readiness_manager.repository.ReadinessItemRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReadinessItemServiceImpl implements ReadinessItemService {

    private static final Logger log = LoggerFactory.getLogger(ReadinessItemServiceImpl.class);

    private static final int SCORE_MIN = 0;
    private static final int SCORE_MAX = 100;

    private final ReadinessItemRepository repository;
    private final EmailService emailService;

    public ReadinessItemServiceImpl(ReadinessItemRepository repository, EmailService emailService) {
        this.repository = repository;
        this.emailService = emailService;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = READINESS_ITEMS_PAGE_CACHE, allEntries = true),
            @CacheEvict(value = READINESS_ITEMS_LIST_CACHE, allEntries = true)
    })
    public ReadinessItem create(ReadinessItemRequest request) {
        sanitise(request);
        validateBusinessRules(request, null);

        ReadinessItem item = ReadinessItem.builder()
                .title(request.getTitle())
                .controlReference(request.getControlReference())
                .description(request.getDescription())
                .category(request.getCategory())
                .status(request.getStatus())
                .priority(request.getPriority())
                .ownerName(request.getOwnerName())
                .ownerEmail(request.getOwnerEmail())
                .readinessScore(request.getReadinessScore())
                .dueDate(request.getDueDate())
                .evidenceDetails(request.getEvidenceDetails())
                .riskSummary(request.getRiskSummary())
                .aiSummary(request.getAiSummary())
                .deleted(false)
                .build();

        ReadinessItem saved = repository.save(item);
        log.info("Created readiness item id={} controlRef={}", saved.getId(), saved.getControlReference());
        
        emailService.sendItemCreatedNotification(saved);
        
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = READINESS_ITEM_BY_ID_CACHE, key = "#id")
    public ReadinessItem getById(Long id) {
        return repository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReadinessItem", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = READINESS_ITEMS_LIST_CACHE, key = "'all'")
    public List<ReadinessItem> getAll() {
        return repository.findAllByDeletedFalse();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = READINESS_ITEMS_PAGE_CACHE,
            key = "#pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort.toString()")
    public Page<ReadinessItem> getAll(Pageable pageable) {
        return repository.findAllByDeletedFalse(pageable);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = READINESS_ITEM_BY_ID_CACHE, key = "#id"),
            @CacheEvict(value = READINESS_ITEMS_PAGE_CACHE, allEntries = true),
            @CacheEvict(value = READINESS_ITEMS_LIST_CACHE, allEntries = true)
    })
    public ReadinessItem update(Long id, ReadinessItemRequest request) {
        ReadinessItem existing = getById(id);

        sanitise(request);
        validateBusinessRules(request, existing);

        existing.setTitle(request.getTitle());
        existing.setControlReference(request.getControlReference());
        existing.setDescription(request.getDescription());
        existing.setCategory(request.getCategory());
        existing.setStatus(request.getStatus());
        existing.setPriority(request.getPriority());
        existing.setOwnerName(request.getOwnerName());
        existing.setOwnerEmail(request.getOwnerEmail());
        existing.setReadinessScore(request.getReadinessScore());
        existing.setDueDate(request.getDueDate());
        existing.setEvidenceDetails(request.getEvidenceDetails());
        existing.setRiskSummary(request.getRiskSummary());
        existing.setAiSummary(request.getAiSummary());

        ReadinessItem saved = repository.save(existing);
        log.info("Updated readiness item id={}", saved.getId());
        return saved;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = READINESS_ITEM_BY_ID_CACHE, key = "#id"),
            @CacheEvict(value = READINESS_ITEMS_PAGE_CACHE, allEntries = true),
            @CacheEvict(value = READINESS_ITEMS_LIST_CACHE, allEntries = true)
    })
    public void softDelete(Long id) {
        ReadinessItem existing = getById(id);
        existing.setDeleted(true);
        repository.save(existing);
        log.info("Soft-deleted readiness item id={}", id);
    }

    private void sanitise(ReadinessItemRequest request) {
        request.setTitle(trimOrNull(request.getTitle()));
        request.setControlReference(trimOrNull(request.getControlReference()));
        request.setDescription(trimOrNull(request.getDescription()));
        request.setOwnerName(trimOrNull(request.getOwnerName()));
        request.setOwnerEmail(trimOrNull(request.getOwnerEmail()));
        request.setEvidenceDetails(trimOrNull(request.getEvidenceDetails()));
        request.setRiskSummary(trimOrNull(request.getRiskSummary()));
        request.setAiSummary(trimOrNull(request.getAiSummary()));
    }

    private String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void validateBusinessRules(ReadinessItemRequest request, ReadinessItem existing) {
        List<String> violations = new ArrayList<>();

        if (request.getTitle() == null) {
            violations.add("Title is required");
        }
        if (request.getControlReference() == null) {
            violations.add("Control reference is required");
        }
        if (request.getDescription() == null) {
            violations.add("Description is required");
        }
        if (request.getCategory() == null) {
            violations.add("Category is required");
        }
        if (request.getStatus() == null) {
            violations.add("Status is required");
        }
        if (request.getPriority() == null) {
            violations.add("Priority is required");
        }
        if (request.getOwnerName() == null) {
            violations.add("Owner name is required");
        }
        if (request.getOwnerEmail() == null) {
            violations.add("Owner email is required");
        }
        if (request.getReadinessScore() == null) {
            violations.add("Readiness score is required");
        }
        if (request.getDueDate() == null) {
            violations.add("Due date is required");
        }

        if (!violations.isEmpty()) {
            throw new BusinessValidationException(violations);
        }

        if (request.getReadinessScore() < SCORE_MIN || request.getReadinessScore() > SCORE_MAX) {
            throw new InvalidReadinessScoreException(request.getReadinessScore());
        }

        if (existing == null && request.getDueDate().isBefore(LocalDate.now())) {
            throw new InvalidDueDateException(request.getDueDate());
        }

        Optional<ReadinessItem> duplicate = repository
                .findByControlReferenceAndDeletedFalse(request.getControlReference());

        if (duplicate.isPresent()) {
            boolean isDuplicate = (existing == null)
                    || !duplicate.get().getId().equals(existing.getId());

            if (isDuplicate) {
                throw new DuplicateControlReferenceException(request.getControlReference());
            }
        }
    }
}
