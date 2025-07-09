export interface XmlNode {
  name: string;
  value?: string;
  attributes: { key: string; value: string }[];
  children: XmlNode[];
  isExpanded: boolean;
}

