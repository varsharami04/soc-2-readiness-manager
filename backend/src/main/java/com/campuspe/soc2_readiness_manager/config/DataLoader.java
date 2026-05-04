package com.campuspe.soc2_readiness_manager.config;

import com.campuspe.soc2_readiness_manager.entity.ControlCategory;
import com.campuspe.soc2_readiness_manager.entity.PriorityLevel;
import com.campuspe.soc2_readiness_manager.entity.ReadinessItem;
import com.campuspe.soc2_readiness_manager.entity.ReadinessStatus;
import com.campuspe.soc2_readiness_manager.repository.ReadinessItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final ReadinessItemRepository readinessItemRepository;

    @Override
    public void run(String... args) throws Exception {
        if (readinessItemRepository.count() == 0) {
            log.info("Database is empty. Seeding demo data...");
            seedData();
            log.info("Demo data seeded successfully.");
        } else {
            log.info("Database already contains data. Skipping seeding.");
        }
    }

    private void seedData() {
        List<ReadinessItem> items = new ArrayList<>();
        Random random = new Random(42);

        ControlCategory[] categories = ControlCategory.values();
        ReadinessStatus[] statuses = ReadinessStatus.values();
        PriorityLevel[] priorities = PriorityLevel.values();

        for (int i = 1; i <= 30; i++) {
            ControlCategory category = categories[random.nextInt(categories.length)];
            ReadinessStatus status = statuses[random.nextInt(statuses.length)];
            PriorityLevel priority = priorities[random.nextInt(priorities.length)];
            
            int readinessScore = random.nextInt(101); // 0 to 100
            
            // Random due date between 30 days ago and 90 days in the future
            long daysOffset = random.nextInt(121) - 30; 
            LocalDate dueDate = LocalDate.now().plusDays(daysOffset);

            ReadinessItem item = ReadinessItem.builder()
                    .title("Demo Control Item " + i)
                    .controlReference("CTRL-" + String.format("%03d", i))
                    .description("This is an automatically generated demo readiness item for demonstration purposes. Focus: " + category.name())
                    .category(category)
                    .status(status)
                    .priority(priority)
                    .ownerName("Demo Owner " + i)
                    .ownerEmail("owner" + i + "@example.com")
                    .readinessScore(readinessScore)
                    .dueDate(dueDate)
                    .evidenceDetails(status == ReadinessStatus.COMPLIANT ? "Evidence linked and verified." : "Pending evidence.")
                    .riskSummary("Risk level associated is " + priority.name())
                    .aiSummary("AI analysis indicates control is currently " + status.name() + " with score " + readinessScore)
                    .build();

            items.add(item);
        }

        readinessItemRepository.saveAll(items);
    }
}
