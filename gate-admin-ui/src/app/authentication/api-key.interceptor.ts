import { HttpEvent, HttpHandlerFn, HttpRequest } from "@angular/common/http";
import { Observable } from "rxjs";
import { inject } from "@angular/core";
import { AuthService } from "./auth.service";

export function apiKeyInterceptor(
  req: HttpRequest<unknown>,
  next: HttpHandlerFn,
): Observable<HttpEvent<unknown>> {
  const authService = inject(AuthService);
  const apiKey = authService.getApiKey();
  
  if (apiKey) {
    const reqWithApiKey = req.clone({
      headers: req.headers.set("X-API-Key", apiKey),
    });
    console.log("Sent request with headers:", reqWithApiKey.headers);
    return next(reqWithApiKey);
  }
  
  return next(req);
}
