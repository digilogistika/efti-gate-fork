import {Component} from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {Navbar} from './navbar/navbar';
import {HttpClient} from '@angular/common/http';

@Component({
  selector: 'app-root',
  imports: [
    RouterOutlet,
    Navbar
  ],
  templateUrl: './app.html',
})
export class App {
  protected title = 'portal-mock';
  protected version: string = ""

  constructor(private readonly http: HttpClient) {
    http.get<any>("/v3/api-docs").subscribe(apiDocs => {
      this.version = apiDocs.info.version
    })
  }
}
