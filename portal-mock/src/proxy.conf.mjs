import {isDevMode} from "@angular/core";

export default [
  {
    context: [
      '/api',
      '/v3'
    ],
    target: isDevMode() ? 'http://localhost:8090' : "",
    secure: false
  }
];
