package br.com.grupopipa.gestaointegrada.core.dto;

import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderDTO {

    private Direction direction;
    private String property;

    public Order getOrder() {
        if (Direction.ASC.equals(direction)) {
            return Order.asc(property);
        }

        return Order.desc(property);
    }

}
