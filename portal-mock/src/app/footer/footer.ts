import { Component, Input } from '@angular/core';
import { NgOptimizedImage } from '@angular/common';

@Component({
  selector: 'app-footer',
  templateUrl: './footer.html',
  standalone: true,
  imports: [
    NgOptimizedImage
  ]
})
export class FooterComponent {
  @Input() version: string = '';
}
