export const environment = {
  production: true,
  gateId: "OWNER",
  apiUrl: {
    registerGate: "/api/admin/gate/register",
    registerPlatform: "/api/admin/platform/register",
    registerAuthority: "/api/admin/authority/register",
    deleteGate: "/api/admin/gate/delete",
    health: "/actuator/health",
    getGates: "/api/admin/gates",
  },
};
