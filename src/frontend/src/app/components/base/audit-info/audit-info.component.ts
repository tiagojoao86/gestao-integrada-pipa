import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';

export interface AuditInfoData {
  createdBy: string | null;
  createdAt: Date | null;
  updatedBy: string | null;
  updatedAt: Date | null;
  deletedBy: string | null;
  deletedAt: Date | null;
}

@Component({
  selector: 'gi-audit-info',
  standalone: true,
  imports: [CommonModule, CardModule, ButtonModule],
  templateUrl: './audit-info.component.html',
  styleUrls: ['./audit-info.component.css'],
})
export class AuditInfoComponent {
  @Input() auditData: AuditInfoData | null = null;
  @Output() closeEvent = new EventEmitter<void>();

  onClose(): void {
    this.closeEvent.emit();
  }

  formatDate(date: Date | null | undefined): string {
    if (!date) return '-';
    return new Date(date).toLocaleString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  }

  getCloseLabel(): string {
    return $localize`Fechar informações de auditoria`;
  }
}
