package com.campuspe.soc2_readiness_manager.service;

import static com.campuspe.soc2_readiness_manager.config.CacheConfig.READINESS_ITEM_BY_ID_CACHE;
import static com.campuspe.soc2_readiness_manager.config.CacheConfig.READINESS_ITEMS_LIST_CACHE;
import static com.campuspe.soc2_readiness_manager.config.CacheConfig.READINESS_ITEMS_PAGE_CACHE;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.campuspe.soc2_readiness_manager.dto.ReadinessItemRequest;
import com.campuspe.soc2_readiness_manager.entity.ControlCategory;
import com.campuspe.soc2_readiness_manager.entity.PriorityLevel;
import com.campuspe.soc2_readiness_manager.entity.ReadinessItem;
import com.campuspe.soc2_readiness_manager.entity.ReadinessStatus;
import com.campuspe.soc2_readiness_manager.repository.ReadinessItemRepository;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.LockSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ReadinessItemServiceCachingTest.CachingTestConfig.class)
class ReadinessItemServiceCachingTest {

    private static final long SLOW_REPOSITORY_CALL_MILLIS = 120L;

    @Configuration
    @EnableCaching
    static class CachingTestConfig {

        @Bean
        ReadinessItemRepository readinessItemRepository() {
            return Mockito.mock(ReadinessItemRepository.class);
        }

        @Bean
        EmailService emailService() {
            return Mockito.mock(EmailService.class);
        }

        @Bean
        ReadinessItemService readinessItemService(ReadinessItemRepository repository, EmailService emailService) {
            return new ReadinessItemServiceImpl(repository, emailService);
        }

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager(
                    READINESS_ITEM_BY_ID_CACHE,
                    READINESS_ITEMS_PAGE_CACHE,
                    READINESS_ITEMS_LIST_CACHE);
        }
    }

    @org.springframework.beans.factory.annotation.Autowired
    private ReadinessItemService readinessItemService;

    @org.springframework.beans.factory.annotation.Autowired
    private ReadinessItemRepository readinessItemRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        reset(readinessItemRepository);
        cacheManager.getCacheNames().stream()
                .map(cacheManager::getCache)
                .forEach(this::clearCache);
    }

    @Test
    void getByIdCachesResponsesAndSecondCallIsFaster() {
        ReadinessItem item = sampleItem(1L, "CC-001");
        when(readinessItemRepository.findByIdAndDeletedFalse(1L)).thenAnswer(invocation -> {
            pauseRepositoryCall();
            return Optional.of(item);
        });

        long firstDurationNanos = timeGetById(1L);
        long secondDurationNanos = timeGetById(1L);

        assertTrue(
                secondDurationNanos < firstDurationNanos / 5,
                "Expected cached call to be substantially faster than the initial repository call");
        verify(readinessItemRepository, times(1)).findByIdAndDeletedFalse(1L);
    }

    @Test
    void createEvictsPagedAndListCaches() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("title").ascending());
        Page<ReadinessItem> page = new PageImpl<>(List.of(sampleItem(1L, "CC-001")), pageable, 1);

        when(readinessItemRepository.findAllByDeletedFalse(pageable)).thenReturn(page);
        when(readinessItemRepository.findAllByDeletedFalse()).thenReturn(List.of(sampleItem(1L, "CC-001")));
        when(readinessItemRepository.findByControlReferenceAndDeletedFalse("CC-NEW")).thenReturn(Optional.empty());
        when(readinessItemRepository.save(any(ReadinessItem.class))).thenAnswer(invocation -> {
            ReadinessItem saved = invocation.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        readinessItemService.getAll(pageable);
        readinessItemService.getAll(pageable);
        readinessItemService.getAll();
        readinessItemService.getAll();

        readinessItemService.create(sampleRequest("CC-NEW"));

        readinessItemService.getAll(pageable);
        readinessItemService.getAll();

        verify(readinessItemRepository, times(2)).findAllByDeletedFalse(pageable);
        verify(readinessItemRepository, times(2)).findAllByDeletedFalse();
    }

    @Test
    void updateEvictsItemAndPagedCaches() {
        ReadinessItem existing = sampleItem(1L, "CC-001");
        Pageable pageable = PageRequest.of(0, 10, Sort.by("title").ascending());
        Page<ReadinessItem> page = new PageImpl<>(List.of(existing), pageable, 1);

        when(readinessItemRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(existing));
        when(readinessItemRepository.findAllByDeletedFalse(pageable)).thenReturn(page);
        when(readinessItemRepository.findByControlReferenceAndDeletedFalse("CC-001")).thenReturn(Optional.of(existing));
        when(readinessItemRepository.save(any(ReadinessItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        readinessItemService.getById(1L);
        readinessItemService.getById(1L);
        readinessItemService.getAll(pageable);
        readinessItemService.getAll(pageable);

        readinessItemService.update(1L, sampleRequest("CC-001"));

        readinessItemService.getById(1L);
        readinessItemService.getAll(pageable);

        verify(readinessItemRepository, times(3)).findByIdAndDeletedFalse(1L);
        verify(readinessItemRepository, times(2)).findAllByDeletedFalse(pageable);
    }

    @Test
    void softDeleteEvictsItemAndPagedCaches() {
        ReadinessItem existing = sampleItem(1L, "CC-001");
        Pageable pageable = PageRequest.of(0, 10, Sort.by("title").ascending());
        Page<ReadinessItem> page = new PageImpl<>(List.of(existing), pageable, 1);

        when(readinessItemRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(existing));
        when(readinessItemRepository.findAllByDeletedFalse(pageable)).thenReturn(page);
        when(readinessItemRepository.save(any(ReadinessItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        readinessItemService.getById(1L);
        readinessItemService.getById(1L);
        readinessItemService.getAll(pageable);
        readinessItemService.getAll(pageable);

        readinessItemService.softDelete(1L);

        readinessItemService.getById(1L);
        readinessItemService.getAll(pageable);

        verify(readinessItemRepository, times(3)).findByIdAndDeletedFalse(1L);
        verify(readinessItemRepository, times(2)).findAllByDeletedFalse(pageable);
    }

    private long timeGetById(Long id) {
        long start = System.nanoTime();
        readinessItemService.getById(id);
        return System.nanoTime() - start;
    }

    private void clearCache(Cache cache) {
        if (cache != null) {
            cache.clear();
        }
    }

    private void pauseRepositoryCall() {
        LockSupport.parkNanos(Duration.ofMillis(SLOW_REPOSITORY_CALL_MILLIS).toNanos());
    }

    private ReadinessItem sampleItem(Long id, String controlReference) {
        return ReadinessItem.builder()
                .id(id)
                .title("Access review evidence")
                .controlReference(controlReference)
                .description("Validate access review evidence for production systems")
                .category(ControlCategory.SECURITY)
                .status(ReadinessStatus.IN_PROGRESS)
                .priority(PriorityLevel.HIGH)
                .ownerName("Alex Morgan")
                .ownerEmail("alex.morgan@example.com")
                .readinessScore(72)
                .dueDate(LocalDate.now().plusDays(14))
                .evidenceDetails("Quarterly access review pending sign-off")
                .riskSummary("Delayed sign-off will block audit readiness")
                .aiSummary("Focus on collecting owner approval evidence first")
                .deleted(false)
                .build();
    }

    private ReadinessItemRequest sampleRequest(String controlReference) {
        return ReadinessItemRequest.builder()
                .title("Access review evidence")
                .controlReference(controlReference)
                .description("Validate access review evidence for production systems")
                .category(ControlCategory.SECURITY)
                .status(ReadinessStatus.IN_PROGRESS)
                .priority(PriorityLevel.HIGH)
                .ownerName("Alex Morgan")
                .ownerEmail("alex.morgan@example.com")
                .readinessScore(72)
                .dueDate(LocalDate.now().plusDays(14))
                .evidenceDetails("Quarterly access review pending sign-off")
                .riskSummary("Delayed sign-off will block audit readiness")
                .aiSummary("Focus on collecting owner approval evidence first")
                .build();
    }
}
