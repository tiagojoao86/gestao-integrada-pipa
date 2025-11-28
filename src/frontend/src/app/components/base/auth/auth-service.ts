import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, catchError, tap } from 'rxjs';
import { Response } from '../model/response';
import { AuthorityDTO } from '../model/authority-dto';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private tokenSubject = new BehaviorSubject<string | null>(this.getToken());
  private username = new BehaviorSubject<string | null>(this.getToken());
  private name = new BehaviorSubject<string | null>(this.getToken());
  private authorities = new BehaviorSubject<AuthorityDTO[] | null>(null);
  token$ = this.tokenSubject.asObservable();

  private http: HttpClient = inject(HttpClient);
  private router: Router = inject(Router);  

  login(username: string, password: string) {
    return this.http
      .post<Response>('/api/authenticate', { username, password })
      .pipe(
        tap((response) => {
          this.setToken(response.body.accessToken);
          this.setUsername(response.body.username);
          this.setNome(response.body.nome);
          this.setUserAuthorities(response.body.authorities);
          this.router.navigate(['/']);
        }),
        catchError((e) => {
          throw e;
        })
      );
  }

  logout() {
    this.clearToken();
    this.router.navigate(['/login']);
  }

  refreshToken() {
    return this.http
      .post<Response>(
        '/api/authenticate/refresh',
        {},
        { withCredentials: true }
      )
      .pipe(
        tap((response) => {
          this.setToken(response.body.accessToken);
          this.setUsername(response.body.username);
          this.setNome(response.body.nome);
          this.setUserAuthorities(response.body.authorities);
        }),
        catchError(() => {
          this.logout();
          throw new Error('Session expired');
        })
      );
  }

  getUsername(): string | null {
    return sessionStorage.getItem('username');
  }

  getNome(): string | null {
    return sessionStorage.getItem('nome');
  }

  setUsername(username: string) {
    sessionStorage.setItem('username', username);
    this.username.next(username);
  }

  setNome(name: string) {
    sessionStorage.setItem('nome', name);
    this.name.next(name);
  }

  getToken(): string | null {
    return sessionStorage.getItem('accessToken');
  }

  setToken(token: string) {
    sessionStorage.setItem('accessToken', token);
    this.tokenSubject.next(token);
  }

  clearToken() {
    sessionStorage.removeItem('accessToken');
    this.tokenSubject.next(null);
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  setUserAuthorities(authorities: AuthorityDTO[]) {
    sessionStorage.setItem('authorities', JSON.stringify(authorities));
    this.authorities.next(authorities);
  }

  getUserAuthorities(): AuthorityDTO[] {
    const authorities = sessionStorage.getItem('authorities');
    if (authorities) {
      return JSON.parse(authorities);
    }
    return [];
  }

  hasAuthorityToModulo(moduleKey: string): boolean {
    const authorities = this.getUserAuthorities();
    return authorities.some((auth) => auth.chave === moduleKey);
  }

  hasAuthorityToGrupo(grupo: string): boolean {
    const authorities = this.getUserAuthorities();
    return authorities.some((auth) => auth.grupo === grupo);
  }

  hasAuthorityListarToModulo(moduleKey: string): boolean {
    const authorities = this.getUserAuthorities();
    const list = authorities.filter((auth) => auth.chave === moduleKey);

    if (list.length > 0) {
      return list[0].permissoes?.includes('LISTAR') || false;
    }

    return false;
  }

  hasAuthorityEditarToModulo(moduleKey: string): boolean {
    const authorities = this.getUserAuthorities();
    const list = authorities.filter((auth) => auth.chave === moduleKey);

    if (list.length > 0) {
      return list[0].permissoes?.includes('EDITAR') || false;
    }

    return false;
  }

  hasAuthorityVisualizarToModulo(moduleKey: string): boolean {
    const authorities = this.getUserAuthorities();
    const list = authorities.filter((auth) => auth.chave === moduleKey);

    if (list.length > 0) {
      return list[0].permissoes?.includes('VISUALIZAR') || false;
    }

    return false;
  }

  hasAuthorityDeletarToModulo(moduleKey: string): boolean {
    const authorities = this.getUserAuthorities();
    const list = authorities.filter((auth) => auth.chave === moduleKey);

    if (list.length > 0) {
      return list[0].permissoes?.includes('DELETAR') || false;
    }

    return false;
  }

}
