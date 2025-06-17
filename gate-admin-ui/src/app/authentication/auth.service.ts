import { Injectable } from "@angular/core";
import { BehaviorSubject } from "rxjs";

@Injectable({
  providedIn: "root",
})
export class AuthService {
  private apiKeySubject = new BehaviorSubject<string>("");
  apiKey$ = this.apiKeySubject.asObservable();

  setApiKey(apiKey: string) {
    console.log("Setting api key to: ", apiKey);
    this.apiKeySubject.next(apiKey);
  }

  getApiKey(): string {
    return this.apiKeySubject.value;
  }
}
