export interface IdentifierResponse {
  eFTIGate: string
  requestId: string
  status: string
  errorCode: string
  errorDescription: string
  identifiers: GateIdentifiersResult[]
}

interface GateIdentifiersResult {
  gateIndicator: string
  status: string
  errorCode: string
  errorDescription: string
  consignments: Consignment[]
}

interface Consignment {
  platformId: string
  datasetId: string
  gateId: string
  carrierAcceptanceDatetime: string
  deliveryEventActualOccurrenceDatetime: string
  mainCarriageTransportMovement: MainCarriageTransportMovement[]
  usedTransportEquipment: UsedTransportEquipment[]
}

interface MainCarriageTransportMovement {
  id: string
  modeCode: string
  schemeAgencyId: string
  dangerousGoodsIndicator: boolean
  registrationCountryCode: string
}

interface UsedTransportEquipment {
  id: string
  sequenceNumber: number
  schemeAgencyId: string
  registrationCountry: string
  categoryCode: string
  carriedTransportEquipment: CarriedTransportEquipment[]
}

interface CarriedTransportEquipment {
  id: string
  sequenceNumber: number
  schemeAgencyId: string
}


export interface DatasetResponse {
  requestId: string
  status: string
  errorCode: string
  errorDescription: string
  data: string
}
