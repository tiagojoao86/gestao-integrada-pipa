import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { DialogService } from './dialog.service';
import {
  DialogConfig,
  DialogButton,
  DialogType,
  DialogResult,
} from './dialog.model';
import { dialogServiceProvider } from './dialog.factory';
import { ButtonModule } from 'primeng/button';

@Component({
  selector: 'gi-dialog',
  imports: [CommonModule, ButtonModule],
  templateUrl: './dialog.component.html',
  styleUrl: './dialog.component.css',
  standalone: true,
  providers: [{ provide: DialogService, useFactory: dialogServiceProvider }],
})
export class DialogComponent implements OnInit, OnDestroy {
  dialogConfig: DialogConfig | null = null;
  buttons: DialogButton[] = [];
  isVisible = false;

  private subscription?: Subscription;
  private dialogService = inject(DialogService);

  ngOnInit(): void {
    this.subscription = this.dialogService.dialogSubject.subscribe((config) => {
      this.dialogConfig = config;
      this.isVisible = config !== null;
      if (config) {
        this.buildButtons(config);
      }
    });
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
  }

  private buildButtons(config: DialogConfig): void {
    this.buttons = [];

    switch (config.type) {
      case DialogType.OK:
        this.buttons.push({
          label: config.okLabel || 'OK',
          result: DialogResult.OK,
          cssClass: 'primario',
        });
        break;

      case DialogType.YES_NO:
        this.buttons.push({
          label: config.noLabel || 'Não',
          result: DialogResult.NO,
          cssClass: 'btn-secondary',
        });
        this.buttons.push({
          label: config.yesLabel || 'Sim',
          result: DialogResult.YES,
          cssClass: 'primario',
        });
        break;

      case DialogType.YES_NO_CANCEL:
        this.buttons.push({
          label: config.cancelLabel || 'Cancelar',
          result: DialogResult.CANCEL,
          cssClass: 'btn-secondary',
        });
        this.buttons.push({
          label: config.noLabel || 'Não',
          result: DialogResult.NO,
          cssClass: 'btn-secondary',
        });
        this.buttons.push({
          label: config.yesLabel || 'Sim',
          result: DialogResult.YES,
          cssClass: 'primario',
        });
        break;
    }
  }

  onButtonClick(result: DialogResult): void {
    this.dialogService.close(result);
  }

  onOverlayClick(): void {
    // Não fecha ao clicar no overlay - usuário deve escolher uma opção
    // Se quiser permitir fechar clicando fora, descomente a linha abaixo:
    // this.dialogService.dismiss();
  }
}
