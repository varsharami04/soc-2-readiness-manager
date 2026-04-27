package com.campuspe.soc2_readiness_manager.service;

import com.campuspe.soc2_readiness_manager.dto.ReadinessItemRequest;
import com.campuspe.soc2_readiness_manager.entity.ReadinessItem;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReadinessItemService {

    ReadinessItem create(ReadinessItemRequest request);

    ReadinessItem getById(Long id);

    List<ReadinessItem> getAll();

    Page<ReadinessItem> getAll(Pageable pageable);

    ReadinessItem update(Long id, ReadinessItemRequest request);

    void softDelete(Long id);
}
