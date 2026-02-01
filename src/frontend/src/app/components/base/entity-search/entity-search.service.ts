import {
  Injectable,
  ApplicationRef,
  ComponentRef,
  createComponent,
  EnvironmentInjector,
  inject,
} from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { take } from 'rxjs/operators';
import { EntitySearchConfig, EntitySearchResult } from './entity-search.model';
import { EntitySearchComponent } from './entity-search.component';

@Injectable({
  providedIn: 'root',
})
export class EntitySearchService {
  private appRef = inject(ApplicationRef);
  private injector = inject(EnvironmentInjector);

  /**
   * Abre a modal de busca de entidade
   * @param config Configuração da busca
   * @returns Observable que emite a entidade selecionada ou cancelled: true se cancelado
   */
  search<T>(config: EntitySearchConfig<T>): Observable<EntitySearchResult<T>> {
    const resultSubject = new Subject<EntitySearchResult<T>>();

    const componentRef = createComponent(EntitySearchComponent<T>, {
      environmentInjector: this.injector,
    });

    const instance = componentRef.instance;
    instance.config = config;
    instance.isVisible = true;

    instance.entitySelected.pipe(take(1)).subscribe((entity) => {
      resultSubject.next({ entity: entity as T, cancelled: false });
      resultSubject.complete();
      this.destroyComponent(componentRef);
    });

    instance.searchCancelled.pipe(take(1)).subscribe(() => {
      resultSubject.next({
        entity: undefined as unknown as T,
        cancelled: true,
      });
      resultSubject.complete();
      this.destroyComponent(componentRef);
    });

    this.appRef.attachView(componentRef.hostView);
    const domElem = componentRef.location.nativeElement as HTMLElement;
    document.body.appendChild(domElem);

    return resultSubject.asObservable();
  }

  private destroyComponent<T>(
    componentRef: ComponentRef<EntitySearchComponent<T>>
  ): void {
    this.appRef.detachView(componentRef.hostView);
    componentRef.destroy();
  }
}
