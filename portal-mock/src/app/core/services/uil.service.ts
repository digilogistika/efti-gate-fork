import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from "../../../environment/environment";
import { UilSearchModel } from "../models/uil-search.model";
import { UilResponse } from "../models/uil-response.model";
import { RequestIdModel } from "../models/RequestId.model";
import { IdentifiersResponse } from "../models/identifiers-response.model";
import {ApiKeyService} from "./api-key.service";

const baseUrl = environment.baseUrl;
const url = environment.apiUrl.uil;

@Injectable({
  providedIn: 'root'
})
export class UilService {
  constructor(
    private readonly http: HttpClient,
    private readonly apiKeyService: ApiKeyService
  ) {}

  postUilControl(searchParams: UilSearchModel): Observable<RequestIdModel> {
    const apiKeyError = this.apiKeyService.checkApiKey();
    if (apiKeyError) {
      return apiKeyError;
    }

    return this.http.post<IdentifiersResponse>(`${baseUrl}${url}`, searchParams);
  }

  getUilControl(requestId: string): Observable<UilResponse> {
    const apiKeyError = this.apiKeyService.checkApiKey();
    if (apiKeyError) {
      return apiKeyError;
    }

    return this.http.get<UilResponse>(`${baseUrl}${url}?requestId=${requestId}`);
  }

}
