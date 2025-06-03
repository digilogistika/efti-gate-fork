import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiKeyService } from '../services/api-key.service';

@Injectable()
export class ApiKeyInterceptor implements HttpInterceptor {

  constructor(private apiKeyService: ApiKeyService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const apiKey = this.apiKeyService.getApiKey();

    if (req.url.includes('/api/') && apiKey) {
      const apiReq = req.clone({
        setHeaders: {
          'X-API-Key': apiKey
        }
      });
      return next.handle(apiReq);
    }

    return next.handle(req);
  }
}
