// src/app/pdfjs-viewer/pdfjs-viewer.component.ts
import { Component, Input, OnChanges, SimpleChanges, ViewContainerRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import * as pdfjsLib from 'pdfjs-dist';

@Component({
  selector: 'app-pdfjs-viewer',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div #pdfContainer class="pdf-container"></div>
  `,
  styles: [`
    .pdf-container ::ng-deep canvas {
      max-width: 100%;
      height: auto;
      display: block;
    }
  `]
})
export class Pdfjs implements OnChanges {
  @Input() pdfData: Blob | null = null;

  constructor(private viewContainerRef: ViewContainerRef) {
    // Set the worker source for PDF.js
    pdfjsLib.GlobalWorkerOptions.workerSrc = '/assets/pdf.worker.mjs';
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['pdfData'] && this.pdfData) {
      this.renderPdf(this.pdfData);
    }
  }

  private async renderPdf(pdfData: Blob): Promise<void> {
    const container = this.viewContainerRef.element.nativeElement.querySelector('.pdf-container');
    if (!container) return;

    // Clear previous content
    container.innerHTML = '';

    const pdfArrayBuffer = await pdfData.arrayBuffer();
    const loadingTask = pdfjsLib.getDocument(pdfArrayBuffer);

    const pdf = await loadingTask.promise;

    // Since the PDF is always one page, get the first page directly.
    const page = await pdf.getPage(1);
    const viewport = page.getViewport({ scale: 1.5 });

    const canvas = document.createElement('canvas');
    const context = canvas.getContext('2d');
    if (!context) return;

    canvas.height = viewport.height;
    canvas.width = viewport.width;

    container.appendChild(canvas);

    const renderContext = {
      canvasContext: context,
      viewport: viewport
    };
    await page.render(renderContext).promise;
  }
}
