package br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendaregra;

import java.util.UUID;

import lombok.Getter;

@Getter
public class ConflitoPar {

    private final UUID regraIdA;
    private final UUID regraIdB;

    public ConflitoPar(UUID regraIdA, UUID regraIdB) {
        this.regraIdA = regraIdA;
        this.regraIdB = regraIdB;
    }
}
