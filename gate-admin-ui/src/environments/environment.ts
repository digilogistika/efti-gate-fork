export const environment = {
  production: true,
  gateId: process.env["GATE_ID"],
  baseUrl: process.env["BASE_URL"],
  apiUrl: {
    registerGate: "/api/admin/gate/register",
    registerPlatform: "/api/admin/platform/register",
    registerAuthority: "/api/admin/authority/register",
    deleteGate: "/api/admin/gate/delete",
  },
};
