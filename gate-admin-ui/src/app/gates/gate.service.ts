import { HttpClient, HttpResponse } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { Gate } from "./gate.model";
import { MetaData } from './metadata.model';
import { environment } from "../../environments/environment";

@Injectable({
  providedIn: "root",
})
export class GateService {
  registerUrl = environment.apiUrl.registerGate;
  deleteUrl = environment.apiUrl.deleteGate;
  metaDataUrl = environment.apiUrl.getMetaData;

  private readonly http = inject(HttpClient);

  getMetaData(): Observable<MetaData> {
    return this.http.get<MetaData>(this.metaDataUrl);
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
