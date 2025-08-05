import { inject, Injectable } from "@angular/core";
import { environment } from "../../environments/environment.development";
import { HttpClient, HttpResponse } from "@angular/common/http";
import { ApiKeyResponse } from "../authentication/api-key-response.model";
import { Observable } from "rxjs";
import { Authority } from "./authority.model";

@Injectable({
  providedIn: "root",
})
export class AuthorityService {
  registerUrl = environment.apiUrl.registerAuthority;
  deleteUrl = environment.apiUrl.deleteAuthority;
  private readonly http = inject(HttpClient);

  register(authority: Authority): Observable<HttpResponse<ApiKeyResponse>> {
    return this.http.post<ApiKeyResponse>(this.registerUrl, authority, {
      observe: "response",
    });
  }

  deleteAuthority(authorityId: string): Observable<HttpResponse<Text>> {
    return this.http.delete<Text>(this.deleteUrl + "/" + authorityId, {
      observe: "response",
      responseType: "text" as "json",
    });
  }
}
