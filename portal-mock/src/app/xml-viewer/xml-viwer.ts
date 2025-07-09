import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import {XmlNode} from './xml-node';

@Component({
  selector: 'app-xml-viewer',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './xml-viewer.html',
})
export class XmlViewer {
  @Input() nodes: XmlNode[] = [];
  @Input() level: number = 0;

  toggle(node: XmlNode): void {
    if (this.hasContentToExpand(node)) {
      node.isExpanded = !node.isExpanded;
    }
  }

  hasContentToExpand(node: XmlNode): boolean {
    return (node.attributes && node.attributes.length > 0) || (node.children && node.children.length > 0);
  }

  formatKey(key: string): string {
    if (!key) return '';
    const result = key.replace(/([A-Z])/g, ' $1');
    return result.charAt(0).toUpperCase() + result.slice(1).trim();
  }
}
