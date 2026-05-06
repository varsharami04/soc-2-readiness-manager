package com.campuspe.soc2_readiness_manager.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.campuspe.soc2_readiness_manager.config.JpaAuditingConfig;
import com.campuspe.soc2_readiness_manager.entity.ControlCategory;
import com.campuspe.soc2_readiness_manager.entity.PriorityLevel;
import com.campuspe.soc2_readiness_manager.entity.ReadinessItem;
import com.campuspe.soc2_readiness_manager.entity.ReadinessStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class ReadinessItemRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ReadinessItemRepository repository;

    private ReadinessItem activeItem;
    private ReadinessItem deletedItem;

    @BeforeEach
    void setUp() {
        activeItem = ReadinessItem.builder()
                .title("Active Item")
                .controlReference("CC-001")
                .description("Desc")
                .category(ControlCategory.valueOf("SECURITY"))
                .status(ReadinessStatus.valueOf("NOT_STARTED"))
                .priority(PriorityLevel.valueOf("HIGH"))
                .ownerName("Test Owner")
                .ownerEmail("test@example.com")
                .readinessScore(50)
                .dueDate(LocalDate.now().plusDays(10))
                .deleted(false)
                .build();
        
        deletedItem = ReadinessItem.builder()
                .title("Deleted Item")
                .controlReference("CC-002")
                .description("Desc")
                .category(ControlCategory.valueOf("SECURITY"))
                .status(ReadinessStatus.valueOf("NOT_STARTED"))
                .priority(PriorityLevel.valueOf("HIGH"))
                .ownerName("Test Owner")
                .ownerEmail("test@example.com")
                .readinessScore(50)
                .dueDate(LocalDate.now().plusDays(10))
                .deleted(true)
                .build();

        activeItem = entityManager.persistAndFlush(activeItem);
        deletedItem = entityManager.persistAndFlush(deletedItem);
    }

    @Test
    void findByIdAndDeletedFalse_WhenItemActive_ReturnsItem() {
        Optional<ReadinessItem> found = repository.findByIdAndDeletedFalse(activeItem.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(activeItem.getId());
    }

    @Test
    void findByIdAndDeletedFalse_WhenItemDeleted_ReturnsEmpty() {
        Optional<ReadinessItem> found = repository.findByIdAndDeletedFalse(deletedItem.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void findAllByDeletedFalse_ReturnsOnlyActiveItems() {
        List<ReadinessItem> items = repository.findAllByDeletedFalse();
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getId()).isEqualTo(activeItem.getId());
    }

    @Test
    void findAllByDeletedFalse_WithPageable_ReturnsOnlyActiveItemsPaged() {
        Page<ReadinessItem> page = repository.findAllByDeletedFalse(PageRequest.of(0, 10));
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getId()).isEqualTo(activeItem.getId());
    }

    @Test
    void findByControlReferenceAndDeletedFalse_WhenItemActive_ReturnsItem() {
        Optional<ReadinessItem> found = repository.findByControlReferenceAndDeletedFalse("CC-001");
        assertThat(found).isPresent();
        assertThat(found.get().getControlReference()).isEqualTo("CC-001");
    }

    @Test
    void findByControlReferenceAndDeletedFalse_WhenItemDeleted_ReturnsEmpty() {
        Optional<ReadinessItem> found = repository.findByControlReferenceAndDeletedFalse("CC-002");
        assertThat(found).isEmpty();
    }
}
