import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environment/environment';
import { AuthorityUserModel} from "../models/authority-user.model";

const baseUrl = environment.baseUrl;
const url = environment.apiUrl.createAuthUser;

@Injectable({
  providedIn: 'root'
})
export class CreateUserService {

  constructor(
    private readonly http: HttpClient,
  ) {}

  create(userModel: AuthorityUserModel): Observable<undefined> {
    return this.http.post<undefined>(`${baseUrl}${url}`, userModel);
  }
}
