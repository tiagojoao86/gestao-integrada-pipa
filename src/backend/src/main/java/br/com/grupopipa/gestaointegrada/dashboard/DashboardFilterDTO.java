package br.com.grupopipa.gestaointegrada.dashboard;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class DashboardFilterDTO {

    private LocalDate dataInicio;
    private LocalDate dataFim;
}
