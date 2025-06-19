import { inject, Injectable } from "@angular/core";
import { environment } from "../../environments/environment";
import { HttpClient, HttpResponse } from "@angular/common/http";
import { Platform } from "./platform.model";
import { Observable } from "rxjs";
import { ApiKeyResponse } from "../authentication/api-key-response.model";

@Injectable({
  providedIn: "root",
})
export class PlatformService {
  registerUrl = environment.apiUrl.registerPlatform;

  private readonly http = inject(HttpClient);

  registerPlatform(
    platform: Platform,
  ): Observable<HttpResponse<ApiKeyResponse>> {
    return this.http.post<ApiKeyResponse>(this.registerUrl, platform, {
      observe: "response",
    });
  }
}
