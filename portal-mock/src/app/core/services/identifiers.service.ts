import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {Observable} from 'rxjs';
import { environment } from "../../../environment/environment";
import { RequestIdModel } from "../models/RequestId.model";
import { IdentifiersSearch } from "../models/identifiers-search.model";
import { IdentifiersResponse } from "../models/identifiers-response.model";
import {ApiKeyService} from "./api-key.service";

const baseUrl = environment.baseUrl;
const url = environment.apiUrl.identifiers;

@Injectable({
  providedIn: 'root'
})
export class IdentifiersService {
  constructor(
    private readonly http: HttpClient,
    private readonly apiKeyService: ApiKeyService
  ) {}

  postIdentifiersControl(searchParams: IdentifiersSearch): Observable<RequestIdModel> {
    const apiKeyError = this.apiKeyService.checkApiKey();
    if (apiKeyError) {
      return apiKeyError;
    }

    return this.http.post<IdentifiersResponse>(`${baseUrl}${url}`, searchParams);
  }

  getIdentifiersControl(requestId: string): Observable<IdentifiersResponse> {
    const apiKeyError = this.apiKeyService.checkApiKey();
    if (apiKeyError) {
      return apiKeyError;
    }

    return this.http.get<IdentifiersResponse>(`${baseUrl}${url}?requestId=${requestId}`);
  }
}
