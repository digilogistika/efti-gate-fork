// file: portal-mock/src/app/pdf-viewer/pdf-viewer.ts
import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SafeResourceUrl } from '@angular/platform-browser';

@Component({
  selector: 'app-pdf-viewer',
  standalone: true,
  imports: [CommonModule],
  template: `
    @if (pdfUrl) {
      <iframe [src]="pdfUrl" class="w-full h-[800px] border-0" title="PDF Viewer"></iframe>
    }
  `,
})
export class PdfViewer {
  @Input() pdfUrl: SafeResourceUrl | null = null;
}
