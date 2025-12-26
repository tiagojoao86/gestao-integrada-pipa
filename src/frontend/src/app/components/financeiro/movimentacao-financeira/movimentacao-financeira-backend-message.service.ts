import { Injectable } from '@angular/core';
import { AbstractBackendMessageService } from '../../base/services/backend-messsages/abstract-backend-message.service';

@Injectable({
  providedIn: 'root',
})
export class MovimentacaoFinanceiraBackendMessages extends AbstractBackendMessageService {
  protected entityMessages(): Record<string, string> {
    return {
      'movimentacaoFinanceira.titulos': $localize`:@@movimentacaoFinanceira.titulos:Pelo menos um título é obrigatório`,
      'movimentacaoFinanceira.contaBancaria': $localize`:@@movimentacaoFinanceira.contaBancaria:Conta bancária é obrigatória`,
      'movimentacaoFinanceira.tipo': $localize`:@@movimentacaoFinanceira.tipo:Tipo de movimentação é obrigatório`,
      'movimentacaoFinanceira.formaPagamento': $localize`:@@movimentacaoFinanceira.formaPagamento:Forma de pagamento é obrigatória`,
      'movimentacaoFinanceira.valor': $localize`:@@movimentacaoFinanceira.valor:Valor deve ser maior que zero`,
      'movimentacaoFinanceira.valor.valorMovimentoMaiorTitulo': $localize`:@@movimentacaoFinanceira.valor.valorMovimentoMaiorTitulo:Valor da movimentação excede o saldo do título`,
      'movimentacaoFinanceira.data': $localize`:@@movimentacaoFinanceira.data:Data é obrigatória`,
      'movimentacaoFinanceira.unidadeNegocio': $localize`:@@movimentacaoFinanceira.unidadeNegocio:Unidade de negócio é obrigatória`,
      'movimentacaoFinanceira.titulo.status': $localize`:@@movimentacaoFinanceira.titulo.status:Não é possível criar movimentação para título com o status atual`,
    };
  }
}
