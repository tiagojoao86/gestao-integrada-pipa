import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'gi-entity-field',
  standalone: true,
  templateUrl: './entity-field.component.html',
  styleUrl: './entity-field.component.css',
})
export class EntityFieldComponent {
  @Input() label = '';
  @Input() entityLabel: string | null = null;
  @Output() entitySearch = new EventEmitter<void>();
  @Output() remove = new EventEmitter<void>();
}
