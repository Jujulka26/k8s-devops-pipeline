import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly API = '/api/auth';

  constructor(private http: HttpClient) {}

  login(email: string, password: string): Observable<{ token: string; refreshToken: string; name: string }> {
    return this.http
      .post<{ token: string; refreshToken: string; name: string }>(`${this.API}/signin`, { email, password })
      .pipe(
        tap(res => {
          localStorage.setItem('token', res.token);
          localStorage.setItem('refreshToken', res.refreshToken);
          localStorage.setItem('displayName', res.name);
        })
      );
  }

  register(name: string, email: string, password: string): Observable<string> {
    return this.http
      .post(`${this.API}/signup`, { name, email, password }, { responseType: 'text' })
      .pipe(tap(() => localStorage.setItem('displayName', name)));
  }

  refresh(): Observable<{ token: string }> {
    return this.http
      .post<{ token: string }>(`${this.API}/refresh`, { refreshToken: this.getRefreshToken() })
      .pipe(tap(res => localStorage.setItem('token', res.token)));
  }

  logout(): Observable<string> {
    return this.http
      .post(`${this.API}/logout`, { refreshToken: this.getRefreshToken() }, { responseType: 'text' })
      .pipe(tap(() => this.clearTokens()));
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  getRefreshToken(): string | null {
    return localStorage.getItem('refreshToken');
  }

  getDisplayName(): string {
    return localStorage.getItem('displayName') ?? 'User';
  }

  clearTokens(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('displayName');
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }
}
