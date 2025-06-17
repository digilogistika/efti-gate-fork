import {inject, Injectable} from '@angular/core';
import {environment} from '../../environments/environment';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {Platform} from './platform.model';
import {Observable} from 'rxjs';
import {PlatformRegisterResponse} from './platform-register-response.model';


@Injectable({
  providedIn: "root",
})
export class PlatformService {
  baseUrl = environment.baseUrl;
  registerUrl = this.baseUrl + environment.apiUrl.registerPlatform;

  private readonly http = inject(HttpClient);

  registerPlatform(platform: Platform): Observable<HttpResponse<PlatformRegisterResponse>> {
    return this.http.post<PlatformRegisterResponse>(this.registerUrl, platform, {
      observe: "response",
    })
  }
}
