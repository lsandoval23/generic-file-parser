package org.creati.sicloReservationsApi.service.parser;

import lombok.extern.slf4j.Slf4j;
import org.creati.sicloReservationsApi.exception.FileProcessingException;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class RowMapper {

    private final ValueConverter valueConverter;
    // Setter cache: class → (fieldName → Method)
    private final ConcurrentHashMap<Class<?>, Map<String, Method>> setterCache = new ConcurrentHashMap<>();

    public RowMapper(ValueConverter valueConverter) {
        this.valueConverter = valueConverter;
    }

    /**
     * Maps a raw row (keyed by DTO field name → raw value) onto a new DTO instance.
     */
    public <T> T map(Map<String, Object> rawRow, Class<T> dtoClass) {
        try {
            T dto = dtoClass.getDeclaredConstructor().newInstance();
            Map<String, Method> setters = setterCache.computeIfAbsent(dtoClass, this::buildSetterMap);

            for (Map.Entry<String, Object> entry : rawRow.entrySet()) {
                String fieldName = entry.getKey();
                if (fieldName == null || fieldName.isBlank()) {
                    log.warn("Skipping null/blank field name in row mapping for {}", dtoClass.getSimpleName());
                    continue;
                }
                Method setter = setters.get(fieldName);
                if (setter == null) {
                    log.warn("No setter found for field '{}' on {}", fieldName, dtoClass.getSimpleName());
                    continue;
                }
                Class<?> paramType = setter.getParameterTypes()[0];
                Object converted = valueConverter.convert(entry.getValue(), paramType);
                setter.invoke(dto, converted);
            }
            return dto;
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            log.error("Reflection error mapping row to {}: {}", dtoClass.getSimpleName(), e.getMessage());
            throw new FileProcessingException("Error mapping row to " + dtoClass.getSimpleName(), e);
        }
    }

    private Map<String, Method> buildSetterMap(Class<?> cls) {
        Map<String, Method> map = new ConcurrentHashMap<>();
        for (Method m : cls.getMethods()) {
            if (m.getName().startsWith("set") && m.getParameterCount() == 1 && m.getName().length() > 3) {
                String fieldName = Character.toLowerCase(m.getName().charAt(3)) + m.getName().substring(4);
                map.put(fieldName, m);
            }
        }
        return map;
    }
}
