import {Injectable} from '@angular/core';
import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {catchError, Observable, throwError} from 'rxjs';
import { ToastrService} from "ngx-toastr";
import {LoginService} from "../services/login.service";

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {

  constructor(private loginService: LoginService, private toastr: ToastrService) {
  }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(request).pipe(catchError(err => {
      if (request.url.indexOf('redirect_uri') < 0) {
        if (err.status === 403 && this.loginService.isLoggedIn()) {
          location.reload();
        } else if (err.status === 401 && this.loginService.isLoggedIn()) {

          this.toastr.error('Your session has expired, please log again');
          this.loginService.logout();
        }
      }
      return throwError(err);
    }));
  }
}
