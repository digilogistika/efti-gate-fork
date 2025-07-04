import {isDevMode} from "@angular/core";

export default [
  {
    context: [
      '/api',
    ],
    target: isDevMode() ? 'http://localhost:8090' : "",
    secure: false
  }
];
