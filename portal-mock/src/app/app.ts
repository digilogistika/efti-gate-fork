import {Component} from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {Navbar} from './navbar/navbar';
import {HttpClient} from '@angular/common/http';
import {FooterComponent} from './footer/footer';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterOutlet,
    Navbar,
    FooterComponent
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
