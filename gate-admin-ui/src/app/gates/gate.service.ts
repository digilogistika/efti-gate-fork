import { HttpClient, HttpRequest, HttpResponse } from "@angular/common/http";
import { Host, inject, Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { Gate } from "./gate.model";
import { environment } from "../../environments/environment";

@Injectable({
  providedIn: "root",
})
export class GateService {
  baseUrl = environment.baseUrl;
  registerUrl = this.baseUrl + environment.apiUrl.registerGate;
  deleteUrl = this.baseUrl + environment.apiUrl.deleteGate;

  private readonly http = inject(HttpClient);

  registerGate(gate: Gate): Observable<HttpResponse<Text>> {
    return this.http.post<Text>(this.registerUrl, gate, {
      observe: "response",
      responseType: "text" as "json",
    });
  }

  deleteGate(gateId: string): Observable<HttpResponse<Text>> {
    return this.http.delete<Text>(this.deleteUrl + "/" + gateId, {
      observe: "response",
      responseType: "text" as "json",
    });
  }
}
