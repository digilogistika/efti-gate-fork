import { HttpClient, HttpResponse } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { Gate } from "./gate.model";
import { environment } from "../../environments/environment";

@Injectable({
  providedIn: "root",
})
export class GateService {
  registerUrl = environment.apiUrl.registerGate;
  deleteUrl = environment.apiUrl.deleteGate;
  gatesUrl = environment.apiUrl.getGates;

  private readonly http = inject(HttpClient);

  getGates(): Observable<Gate[]> {
    return this.http.get<Gate[]>(this.gatesUrl);
  }

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
