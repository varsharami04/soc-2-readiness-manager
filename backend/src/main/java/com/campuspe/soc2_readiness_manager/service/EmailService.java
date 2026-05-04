package com.campuspe.soc2_readiness_manager.service;

import com.campuspe.soc2_readiness_manager.entity.ReadinessItem;

public interface EmailService {

    void sendItemCreatedNotification(ReadinessItem item);

    void sendOverdueNotification(ReadinessItem item);
}
