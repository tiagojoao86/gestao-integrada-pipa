package br.com.grupopipa.gestaointegrada.core.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import br.com.grupopipa.gestaointegrada.core.dto.FilterItemDTO;
import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Service
public class Specifications<T extends BaseEntity> {

    public Specification<T> withItem(FilterItemDTO item, Class<T> klazz) {
        return (root, query, criteriaBuilder) -> {
            if (ObjectUtils.isEmpty(item)) {
                return null;
            }
            return buildPredicate(item, criteriaBuilder, root, klazz);
        };
    }

    private Predicate buildPredicate(FilterItemDTO item, CriteriaBuilder criteriaBuilder, Root<T> root,
            Class<T> klazz) {
        switch (item.getOperator()) {
            case EQ:
                return buildPredicateEq(item, criteriaBuilder, root, klazz);
            case NEQ:
                return buildPredicateNeq(item, criteriaBuilder, root, klazz);
            case GT:
            case LT:
            case GE:
            case LE:
                return buildRelationalPredicate(item, criteriaBuilder, root, klazz);
            case CONTAINS:
                return buildPredicateContains(item, criteriaBuilder, root, klazz);
            case NOT_CONTAINS:
                return buildPredicateNotContains(item, criteriaBuilder, root, klazz);
            case IN:
                return buildPredicateIn(item, criteriaBuilder, root, klazz);
            case NOT_IN:
                return buildPredicateNotIn(item, criteriaBuilder, root, klazz);
            case BT:
                return buildPredicateBetween(item, criteriaBuilder, root, klazz);
            default:
                return buildPredicateEq(item, criteriaBuilder, root, klazz);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Predicate buildPredicateEq(FilterItemDTO item, CriteriaBuilder criteriaBuilder, Root<T> root,
            Class<T> klazz) {
        String property = item.getProperty();
        Class<?> targetType = getFieldType(klazz, property);
        Object value = convertValue(property, item.getValues().get(0), klazz, targetType);

        if (targetType.equals(LocalDateTime.class)) {            
            Expression<Comparable> propertyExpression = root.get(property);
            Comparable startOfDay = getStartOfDay(value);
            Comparable endOfDay = getEndOfDay(value);
            return criteriaBuilder.between(propertyExpression, startOfDay, endOfDay);
        }

        return criteriaBuilder.equal(root.get(property), value);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Predicate buildPredicateNeq(FilterItemDTO item, CriteriaBuilder criteriaBuilder, Root<T> root,
            Class<T> klazz) {
        String property = item.getProperty();
        Class<?> targetType = getFieldType(klazz, property);
        Object value = convertValue(property, item.getValues().get(0), klazz, targetType);

        if (targetType.equals(LocalDateTime.class)) {
            Expression<Comparable> propertyExpression = root.get(property);
            Comparable startOfDay = getStartOfDay(value);
            Comparable endOfDay = getEndOfDay(value);
            return criteriaBuilder.not(criteriaBuilder.between(propertyExpression, startOfDay, endOfDay));
        }

        return criteriaBuilder.notEqual(root.get(property), value);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Predicate buildRelationalPredicate(FilterItemDTO item, CriteriaBuilder criteriaBuilder, Root<T> root, Class<T> klazz) {
        String property = item.getProperty();
        Class<?> targetType = getFieldType(klazz, property);
        Comparable value = (Comparable) convertValue(property, item.getValues().get(0), klazz, targetType);
        Expression<Comparable> propertyExpression = root.get(property);

        switch (item.getOperator()) {
            case GT:
                if (targetType.equals(LocalDateTime.class)) {
                    Comparable endOfDay = getEndOfDay(value);
                    return criteriaBuilder.greaterThan(propertyExpression, endOfDay);
                }
                return criteriaBuilder.greaterThan(propertyExpression, value);
            case LT:
                if (targetType.equals(LocalDateTime.class)) {
                    Comparable startOfDay = getStartOfDay(value);
                    return criteriaBuilder.lessThan(propertyExpression, startOfDay);
                }
                return criteriaBuilder.lessThan(propertyExpression, value);
            case GE:
                if (targetType.equals(LocalDateTime.class)) {
                    Comparable startOfDay = getStartOfDay(value);
                    return criteriaBuilder.greaterThanOrEqualTo(propertyExpression, startOfDay);
                }
                return criteriaBuilder.greaterThanOrEqualTo(propertyExpression, value);
            case LE:
                if (targetType.equals(LocalDateTime.class)) {
                    Comparable endOfDay = getEndOfDay(value);
                    return criteriaBuilder.lessThanOrEqualTo(propertyExpression, endOfDay);
                }
                return criteriaBuilder.lessThanOrEqualTo(propertyExpression, value);
            default:
                throw new IllegalArgumentException("Operador relacional inválido: " + item.getOperator());
        }
    }

    private Predicate buildPredicateContains(FilterItemDTO item, CriteriaBuilder criteriaBuilder, Root<T> root,
            Class<T> klazz) {
        String property = item.getProperty();
        Class<?> targetType = getFieldType(klazz, property);
        Object value = convertValue(property, item.getValues().get(0), klazz, targetType);

        return criteriaBuilder.like(criteriaBuilder.lower(root.get(property).as(String.class)),
                "%" + value.toString().toLowerCase() + "%");
    }

    private Predicate buildPredicateNotContains(FilterItemDTO item, CriteriaBuilder criteriaBuilder, Root<T> root,
            Class<T> klazz) {
        String property = item.getProperty();
        Class<?> targetType = getFieldType(klazz, property);
        Object value = convertValue(property, item.getValues().get(0), klazz, targetType);

        return criteriaBuilder.notLike(criteriaBuilder.lower(root.get(property).as(String.class)),
                "%" + value.toString().toLowerCase() + "%");
    }

    private Predicate buildPredicateIn(FilterItemDTO item, CriteriaBuilder criteriaBuilder, Root<T> root,
            Class<T> klazz) {
        Class<?> targetType = getFieldType(klazz, item.getProperty());
        List<?> convertedValues = item.getValues().stream()
                .map(val -> convertValue(item.getProperty(), val, klazz, targetType))
                .toList();
        return root.get(item.getProperty()).in(convertedValues);
    }

    private Predicate buildPredicateNotIn(FilterItemDTO item, CriteriaBuilder criteriaBuilder, Root<T> root,
            Class<T> klazz) {
        Class<?> targetType = getFieldType(klazz, item.getProperty());
        List<?> convertedValues = item.getValues().stream()
                .map(val -> convertValue(item.getProperty(), val, klazz, targetType)).toList();
        return criteriaBuilder.not(root.get(item.getProperty()).in(convertedValues));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Predicate buildPredicateBetween(FilterItemDTO item, CriteriaBuilder criteriaBuilder, Root<T> root,
            Class<T> klazz) {
        if (ObjectUtils.isEmpty(item.getValues()) || item.getValues().size() < 2) {
            return null;
        }

        String property = item.getProperty();
        Class<?> targetType = getFieldType(klazz, property);
        Expression<Comparable> propertyExpression = root.get(property);

        Comparable value1 = (Comparable) convertValue(property, item.getValues().get(0), klazz, targetType);
        Comparable value2 = (Comparable) convertValue(property, item.getValues().get(1), klazz, targetType);

        if (targetType.equals(LocalDateTime.class)) {
            Comparable startOfDay = getStartOfDay(value1);
            Comparable endOfDay = getEndOfDay(value2);
            return criteriaBuilder.between(propertyExpression, startOfDay, endOfDay);
        }

        return criteriaBuilder.between(propertyExpression, value1, value2); // Para LocalDate e outros tipos, o between é direto.
    }

    private LocalDateTime getStartOfDay(Object value) {
        if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).toLocalDate().atStartOfDay();
        }
        if (value instanceof LocalDate) {
            return ((LocalDate) value).atStartOfDay();
        }
        throw new IllegalArgumentException("Tipo de data inesperado para getStartOfDay: " + value.getClass().getName());
    }

    private LocalDateTime getEndOfDay(Object value) {
        if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).toLocalDate().atTime(LocalTime.MAX);
        }
        if (value instanceof LocalDate) {
            return ((LocalDate) value).atTime(LocalTime.MAX);
        }
        throw new IllegalArgumentException("Tipo de data inesperado para getEndOfDay: " + value.getClass().getName());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Object convertValue(String property, Object value, Class<T> klazz, Class<?> targetType) {
        if (value == null) {
            return null;
        }

        String stringValue = value.toString();
        try {
            if (targetType.equals(Integer.class) || targetType.equals(int.class)) {
                return Integer.parseInt(stringValue);
            } else if (targetType.equals(Long.class) || targetType.equals(long.class)) {
                return Long.parseLong(stringValue);
            } else if (targetType.equals(Double.class) || targetType.equals(double.class)) {
                return Double.parseDouble(stringValue);
            } else if (targetType.equals(Boolean.class) || targetType.equals(boolean.class)) {
                return Boolean.parseBoolean(stringValue);
            } else if (targetType.equals(LocalDate.class)) {
                return LocalDate.parse(stringValue, DateTimeFormatter.ISO_LOCAL_DATE);
            } else if (targetType.equals(LocalDateTime.class)) {
                return LocalDateTime.parse(stringValue, DateTimeFormatter.ISO_DATE_TIME);
            } else if (targetType.equals(String.class)) {
                return stringValue;
            } else if (targetType.equals(UUID.class)) {
                return UUID.fromString(stringValue);
            } else if (targetType.isEnum()) {
                return Enum.valueOf((Class<Enum>) targetType, stringValue);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Erro ao converter valor: " + value + " para " + targetType.getSimpleName(), e);
        }

        return value;
    }

    private Class<?> getFieldType(Class<?> klazz, String fieldName) {
        Class<?> currentClass = klazz;
        while (currentClass != null && currentClass != Object.class) {
            try {
                return currentClass.getDeclaredField(fieldName).getType();
            } catch (NoSuchFieldException e) {
                currentClass = currentClass.getSuperclass();
            }
        }
        throw new RuntimeException("Campo não encontrado: " + fieldName + " na entidade " + klazz.getSimpleName() + " ou em suas superclasses.");
    }

}
