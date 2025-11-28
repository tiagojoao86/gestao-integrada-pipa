package br.com.grupopipa.gestaointegrada.core.enums;

import org.springframework.data.domain.Sort.Direction;

import lombok.Getter;

@Getter
public enum OrdemDirecao {
    ASC(Direction.ASC),
    DESC(Direction.DESC);

    Direction direcao;

    OrdemDirecao(Direction direcao) {
        this.direcao = direcao;
    }
}
