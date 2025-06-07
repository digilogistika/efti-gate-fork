export const environment = {
  production: false,
  logLevel: 3,
  baseUrl: "http://localhost:8090",
  apiUrl: {
    identifiers: "/api/v1/control/identifiers",
    uil: "/api/v1/control/uil",
    note: "/api/v1/control/uil/follow-up",
    authUserVerify: "/api/public/authority-user/verify",
    createAuthUser: "/api/admin/authority-user/create",
  },
};
