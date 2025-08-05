export const environment = {
  production: false,
  gateId: "Borduria",
  apiUrl: {
    registerGate: "/api/admin/gate/register",
    registerPlatform: "/api/admin/platform/register",
    registerAuthority: "/api/admin/authority/register",
    deleteGate: "/api/admin/gate/delete",
    deletePlatform: "/api/admin/platform/delete",
    deleteAuthority: "/api/admin/authority/delete",
    health: "/actuator/health",
    getMetaData: "/api/admin/metadata",
  },
};
