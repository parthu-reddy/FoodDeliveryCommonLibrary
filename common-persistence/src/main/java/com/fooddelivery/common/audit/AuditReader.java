package com.fooddelivery.common.audit;

import jakarta.persistence.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import java.util.*;

/** Read-only history; deliberately exposes no save, update or delete. */
@Component
public class AuditReader {
    @PersistenceContext private EntityManager entityManager;
    @Transactional(readOnly=true)
    public Slice<AuditEvent> history(String subjectType,UUID subjectId,int page,int size) {
        if(subjectType==null || !subjectType.matches("[A-Z_]{1,40}") || subjectId==null || page<0 || page>100000 || size<1 || size>100) {
            throw new IllegalArgumentException("Supply a subject type, subject id and a valid page size");
        }
        var rows=entityManager.createQuery("select a from AuditEvent a where a.subjectType=:type and a.subjectId=:id order by a.occurredAt desc, a.id desc",AuditEvent.class)
            .setParameter("type",subjectType).setParameter("id",subjectId).setFirstResult(Math.multiplyExact(page,size)).setMaxResults(size+1).getResultList();
        boolean hasNext=rows.size()>size;
        return new SliceImpl<>(hasNext?rows.subList(0,size):rows,PageRequest.of(page,size),hasNext);
    }
}
