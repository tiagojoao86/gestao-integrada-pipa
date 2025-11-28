import { Component, OnInit } from '@angular/core';
import { CommonModule, AsyncPipe } from '@angular/common';
import { LoadingService } from './loading.service';
import { Observable } from 'rxjs';

@Component({
  selector: 'gi-loading',
  standalone: true,
  imports: [CommonModule, AsyncPipe],
  template: `
  <div *ngIf="loading$ | async" class="gi-loading-overlay">
    <div class="gi-loading-spinner"></div>
  </div>
  `,
  styles: [
    `
    .gi-loading-overlay {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0,0,0,0.5);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 9999;
    }

    .gi-loading-spinner {
      width: 64px;
      height: 64px;
      border-radius: 50%;
      border: 8px solid rgba(255,255,255,0.2);
      border-top-color: var(--primary-color, #ffffff);
      animation: gi-spin 1s linear infinite;
    }

    @keyframes gi-spin {
      to { transform: rotate(360deg); }
    }
    `,
  ],
})
export class LoadingComponent implements OnInit {
  loading$!: Observable<boolean>;
  constructor(private loadingService: LoadingService) {}

  ngOnInit(): void {
    this.loading$ = this.loadingService.loading$;
  }
}
