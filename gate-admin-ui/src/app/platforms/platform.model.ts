export interface Header {
  key: string;
  value: string;
}

export interface Platform {
  platformId: string;
  requestBaseUrl: string;
  headers: Header[];
}
