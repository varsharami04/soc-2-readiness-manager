package com.campuspe.soc2_readiness_manager.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.campuspe.soc2_readiness_manager.dto.ReadinessItemRequest;
import com.campuspe.soc2_readiness_manager.entity.ControlCategory;
import com.campuspe.soc2_readiness_manager.entity.PriorityLevel;
import com.campuspe.soc2_readiness_manager.entity.ReadinessItem;
import com.campuspe.soc2_readiness_manager.entity.ReadinessStatus;
import com.campuspe.soc2_readiness_manager.exception.BusinessValidationException;
import com.campuspe.soc2_readiness_manager.exception.DuplicateControlReferenceException;
import com.campuspe.soc2_readiness_manager.exception.InvalidDueDateException;
import com.campuspe.soc2_readiness_manager.exception.InvalidReadinessScoreException;
import com.campuspe.soc2_readiness_manager.exception.ResourceNotFoundException;
import com.campuspe.soc2_readiness_manager.repository.ReadinessItemRepository;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class ReadinessItemServiceImplTest {

    @Mock
    private ReadinessItemRepository repository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ReadinessItemServiceImpl service;

    private ReadinessItemRequest validRequest;
    private ReadinessItem validItem;

    @BeforeEach
    void setUp() {
        validRequest = new ReadinessItemRequest();
        validRequest.setTitle("Test Title");
        validRequest.setControlReference("CC-001");
        validRequest.setDescription("Test Description");
        validRequest.setCategory(ControlCategory.SECURITY);
        validRequest.setStatus(ReadinessStatus.NOT_STARTED);
        validRequest.setPriority(PriorityLevel.HIGH);
        validRequest.setOwnerName("John Doe");
        validRequest.setOwnerEmail("john.doe@example.com");
        validRequest.setReadinessScore(50);
        validRequest.setDueDate(LocalDate.now().plusDays(10));

        validItem = ReadinessItem.builder()
                .id(1L)
                .title("Test Title")
                .controlReference("CC-001")
                .description("Test Description")
                .category(ControlCategory.SECURITY)
                .status(ReadinessStatus.NOT_STARTED)
                .priority(PriorityLevel.HIGH)
                .ownerName("John Doe")
                .ownerEmail("john.doe@example.com")
                .readinessScore(50)
                .dueDate(LocalDate.now().plusDays(10))
                .deleted(false)
                .build();
    }

    @Test
    void create_Success() {
        when(repository.findByControlReferenceAndDeletedFalse("CC-001")).thenReturn(Optional.empty());
        when(repository.save(any(ReadinessItem.class))).thenReturn(validItem);

        ReadinessItem result = service.create(validRequest);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(repository, times(1)).save(any(ReadinessItem.class));
        verify(emailService, times(1)).sendItemCreatedNotification(validItem);
    }

    @Test
    void create_ThrowsBusinessValidationException() {
        validRequest.setTitle(null);

        BusinessValidationException exception = assertThrows(BusinessValidationException.class, () -> {
            service.create(validRequest);
        });

        assertTrue(exception.getViolations().contains("Title is required"));
        verify(repository, never()).save(any());
        verify(emailService, never()).sendItemCreatedNotification(any());
    }

    @Test
    void create_ThrowsInvalidReadinessScoreException() {
        validRequest.setReadinessScore(150);

        assertThrows(InvalidReadinessScoreException.class, () -> {
            service.create(validRequest);
        });

        verify(repository, never()).save(any());
    }

    @Test
    void create_ThrowsInvalidDueDateException() {
        validRequest.setDueDate(LocalDate.now().minusDays(1));

        assertThrows(InvalidDueDateException.class, () -> {
            service.create(validRequest);
        });

        verify(repository, never()).save(any());
    }

    @Test
    void create_ThrowsDuplicateControlReferenceException() {
        when(repository.findByControlReferenceAndDeletedFalse("CC-001")).thenReturn(Optional.of(validItem));

        assertThrows(DuplicateControlReferenceException.class, () -> {
            service.create(validRequest);
        });

        verify(repository, never()).save(any());
    }

    @Test
    void getById_Success() {
        when(repository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(validItem));

        ReadinessItem result = service.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getById_ThrowsResourceNotFoundException() {
        when(repository.findByIdAndDeletedFalse(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            service.getById(99L);
        });
    }

    @Test
    void getAll_Success() {
        List<ReadinessItem> items = Arrays.asList(validItem);
        when(repository.findAllByDeletedFalse()).thenReturn(items);

        List<ReadinessItem> result = service.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
    }
    
    @Test
    void getAllPageable_Success() {
        Page<ReadinessItem> page = new PageImpl<>(Arrays.asList(validItem));
        when(repository.findAllByDeletedFalse(any(PageRequest.class))).thenReturn(page);

        Page<ReadinessItem> result = service.getAll(PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void update_Success() {
        when(repository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(validItem));
        
        ReadinessItem updatedItem = ReadinessItem.builder().id(1L).title("Updated Title").build();
        when(repository.save(any(ReadinessItem.class))).thenReturn(updatedItem);

        validRequest.setTitle("Updated Title");
        
        ReadinessItem result = service.update(1L, validRequest);

        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        verify(repository, times(1)).save(validItem);
    }

    @Test
    void softDelete_Success() {
        when(repository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(validItem));

        service.softDelete(1L);

        assertTrue(validItem.isDeleted());
        verify(repository, times(1)).save(validItem);
    }
}
