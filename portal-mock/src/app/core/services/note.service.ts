import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from "../../../environment/environment";
import {NoteRequestModel} from "../models/note-request.model";
import {ApiKeyService} from "./api-key.service";

const baseUrl = environment.baseUrl;
const url = environment.apiUrl.note;

@Injectable({
  providedIn: 'root'
})
export class NoteService {

  constructor(
    private readonly http: HttpClient,
    private readonly apiKeyService: ApiKeyService
  ) {}

  postNote(note: NoteRequestModel): Observable<string> {
    const apiKeyError = this.apiKeyService.checkApiKey();
    if (apiKeyError) {
      return apiKeyError;
    }

    return this.http.post<string>(`${baseUrl}${url}`, note);
  }
}
