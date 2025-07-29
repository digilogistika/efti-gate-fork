import { Component, Input, OnChanges, SimpleChanges, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { getDocument, GlobalWorkerOptions } from 'pdfjs-dist';

@Component({
  selector: 'app-pdfjs-viewer',
  standalone: true,
  imports: [CommonModule],
  template: '',
})
export class Pdfjs implements OnChanges {
  @Input() pdfData: Blob | null = null;

  constructor(private elementRef: ElementRef<HTMLElement>) {
    GlobalWorkerOptions.workerSrc = '/assets/pdf.worker.mjs';
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['pdfData'] && this.pdfData) {
      this.renderPdf(this.pdfData);
    }
  }

  private async renderPdf(pdfData: Blob): Promise<void> {
    const container = this.elementRef.nativeElement;
    if (!container) return;

    container.innerHTML = '';

    const pdfArrayBuffer = await pdfData.arrayBuffer();
    const loadingTask = getDocument(pdfArrayBuffer);
    const pdf = await loadingTask.promise;
    const page = await pdf.getPage(1);
    const viewport = page.getViewport({ scale: 1.5 });
    const canvas = document.createElement('canvas');
    const context = canvas.getContext('2d');
    if (!context) return;

    canvas.height = viewport.height;
    canvas.width = viewport.width;
    canvas.className = 'max-w-full h-auto block';

    container.appendChild(canvas);

    const renderContext = {
      canvasContext: context,
      viewport: viewport,
      canvas: canvas
    };
    await page.render(renderContext).promise;
  }
}
