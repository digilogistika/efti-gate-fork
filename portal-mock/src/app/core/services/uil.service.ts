import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from "../../../environment/environment";
import { UilSearchModel } from "../models/uil-search.model";
import { UilResponse } from "../models/uil-response.model";
import { RequestIdModel } from "../models/RequestId.model";
import { IdentifiersResponse } from "../models/identifiers-response.model";

const baseUrl = environment.baseUrl;
const url = environment.apiUrl.uil;

@Injectable({
  providedIn: 'root'
})
export class UilService {
  constructor(
    private readonly http: HttpClient,
  ) {}

  postUilControl(searchParams: UilSearchModel): Observable<RequestIdModel> {
    return this.http.post<IdentifiersResponse>(`${baseUrl}${url}`, searchParams);
  }

  getUilControl(requestId: string): Observable<UilResponse> {
    return this.http.get<UilResponse>(`${baseUrl}${url}?requestId=${requestId}`);
  }

}
