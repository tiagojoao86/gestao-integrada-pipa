import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { LoadingInterceptor } from './components/base/loading/loading-interceptor';
import { LoadingComponent } from './components/base/loading/loading.component';

@Component({
    selector: 'gi-app-root',
    standalone: true,
    imports: [RouterOutlet, LoadingComponent],
    templateUrl: './app.component.html',
    styleUrl: './app.component.css',
    providers: [
      { provide: HTTP_INTERCEPTORS, useClass: LoadingInterceptor, multi: true },
    ]
})
export class AppComponent {
  title = 'frontend';
}
