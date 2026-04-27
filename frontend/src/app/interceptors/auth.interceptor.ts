import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  // Attach JWT to every request if we have one
  const token = auth.getToken();
  const authReq = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      // Only attempt refresh on 401, and only if we have a refresh token,
      // and don't loop on the /refresh or /signin calls themselves
      const isAuthUrl = req.url.includes('/signin') || req.url.includes('/refresh');
      if (error.status === 401 && auth.getRefreshToken() && !isAuthUrl) {
        return auth.refresh().pipe(
          switchMap(() => {
            // Retry the original request with the new token
            const retryReq = req.clone({
              setHeaders: { Authorization: `Bearer ${auth.getToken()}` }
            });
            return next(retryReq);
          }),
          catchError(refreshError => {
            // Refresh token is also expired — force re-login
            auth.clearTokens();
            router.navigate(['/login']);
            return throwError(() => refreshError);
          })
        );
      }
      return throwError(() => error);
    })
  );
};
