package com.alterjoc.radar.server.dao.impl;

import java.lang.reflect.Field;
import java.util.List;

import javax.persistence.Query;

import org.jboss.capedwarf.server.api.dao.impl.AbstractTimestampedDAO;
import org.jboss.capedwarf.server.api.domain.TimestampedEntity;

/**
 * Hack around the result list being null.
 * Remove this once we upgrade CapeDwarf Green.
 * TODO -----------------^
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class HackDAO<T extends TimestampedEntity> extends AbstractTimestampedDAO<T> {
    protected Long getCount(Query query) {
        List list;
        Class<? extends Query> clazz = query.getClass();
        if (clazz.getName().contains("ProxyingQuery")) {
            try {
                Field field = clazz.getDeclaredField("delegate");
                field.setAccessible(true);
                Query qd = (Query) field.get(query);
                list = qd.getResultList();
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        } else {
            list = query.getResultList();
        }
        return (list == null || list.isEmpty()) ? 0L : list.size();
    }
}
