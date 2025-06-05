import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from "../../../environment/environment";
import {NoteRequestModel} from "../models/note-request.model";

const baseUrl = environment.baseUrl;
const url = environment.apiUrl.note;

@Injectable({
  providedIn: 'root'
})
export class NoteService {

  constructor(
    private readonly http: HttpClient,
  ) {}

  postNote(note: NoteRequestModel): Observable<string> {
    return this.http.post<string>(`${baseUrl}${url}`, note);
  }
}
