package ru.t1.synthetichumancorestarter.audit.service;

import ru.t1.synthetichumancorestarter.audit.model.AuditEvent;

public interface AuditService {

    void audit(AuditEvent event);

}
