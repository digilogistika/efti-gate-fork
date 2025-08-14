// =================================================================
//
// Top Level Response Interfaces
//
// =================================================================

export interface IdentifierResponse {
  eFTIGate: string;
  requestId: string;
  status: string;
  errorCode: string;
  errorDescription: string;
  identifiers: GateIdentifiersResult[];
}

export interface DatasetResponse {
  requestId: string;
  status: string;
  errorCode: string;
  errorDescription: string;
  data: string;
  pdfData?: string;
  eftiData?: SupplyChainConsignment;
}

// =================================================================
//
// Nested Result Interfaces
//
// =================================================================

interface GateIdentifiersResult {
  gateIndicator: string;
  status: string;
  errorCode: string;
  errorDescription: string;
  consignments: Consignment[];
}

interface Consignment {
  platformId: string;
  datasetId: string;
  gateId: string;
  carrierAcceptanceDatetime: string;
  deliveryEventActualOccurrenceDatetime: string;
  mainCarriageTransportMovement: MainCarriageTransportMovement[];
  usedTransportEquipment: UsedTransportEquipment[];
}

interface MainCarriageTransportMovement {
  id: string;
  modeCode: string;
  schemeAgencyId: string;
  dangerousGoodsIndicator: boolean;
  registrationCountryCode: string;
}

interface UsedTransportEquipment {
  id: string;
  sequenceNumber: number;
  schemeAgencyId: string;
  registrationCountry: string;
  categoryCode: string;
  carriedTransportEquipment: CarriedTransportEquipment[];
}

interface CarriedTransportEquipment {
  id: string;
  sequenceNumber: number;
  schemeAgencyId: string;
}

// =================================================================
//
// Base Primitive and Simple Types
//
// =================================================================

export interface Amount {
  value?: number;
  currencyId?: string;
}

export interface DateTime {
  value?: string;
  formatId?: string;
}

export interface Measure {
  value?: number;
  unitId?: string;
}

export interface Identifier {
  value?: string;
  schemeAgencyId?: string;
}

export interface LocalizedString {
  value?: string;
  languageId?: string;
}

// =================================================================
//
// Complex Types from eu.efti.v1.consignment.common
//
// =================================================================

export interface PostalAddress {
  streetName?: string[];
  additionalStreetName?: string;
  buildingNumber?: string;
  cityName?: string[];
  postcode?: string[];
  countrySubDivisionName?: string[];
  countryCode?: string;
  departmentName?: string;
  postOfficeBox?: string;
}

export interface GeographicalCoordinates {
  latitude?: Measure;
  longitude?: Measure;
}

export interface DefinedContactDetails {
  personName?: string[];
  telephone?: { completeNumber?: string; uri?: string }[];
  emailAddress?: { completeNumber?: string; uri?: string };
  fax?: any[];
}

export interface TradeParty {
  id?: Identifier;
  name?: string[];
  postalAddress?: PostalAddress;
  roleCode?: string[];
  definedContactDetails?: DefinedContactDetails[];
  agreedContract?: any[];
  applicableLicence?: any[];
  authoritativeSignatoryPerson?: any[];
  confirmedDocumentAuthentication?: any[];
  ownedCreditorFinancialAccount?: any;
  representativePerson?: any;
  specifiedContactPerson?: any[];
  taxRegistration?: any[];
}

export interface LogisticsLocation {
  id?: Identifier;
  name?: string[];
  geographicalCoordinates?: GeographicalCoordinates;
  postalAddress?: PostalAddress;
}

export interface AttachedBinaryFile {
  id?: Identifier;
  includedBinaryObject?: string[]; // Base64 encoded
}

export interface AssociatedDocument {
  id?: Identifier;
  uri?: Identifier;
  typeCode?: string;
  subtypeCode?: string[];
  referenceTypeCode?: string;
  issuer?: TradeParty;
  issueLocation?: LogisticsLocation;
  formattedIssueDateTime?: DateTime;
  attachedBinaryFile?: AttachedBinaryFile[];
  attachedBinaryObject?: any[];
  contractualClause?: any[];
}

export interface ReferencedDocument {
  id?: Identifier;
  uri?: Identifier;
  typeCode?: string;
  attachedBinaryFile?: any[];
  attachedBinaryObject?: any[];
  contractualClause?: any[];
  formattedIssueDateTime?: DateTime;
  issueLocation?: LogisticsLocation;
  issuer?: TradeParty;
  referenceTypeCode?: string;
  subtypeCode?: any[];
}

export interface LogisticsPackage {
  id?: Identifier;
  itemQuantity?: number;
  typeText?: { value: string }[];
  typeCode?: string[];
}

export interface UsedTransportMeans {
  id?: Identifier;
  typeCode?: string;
  name?: string;
  registrationCountry?: TradeCountry;
  attachedOperationalEquipment?: any[];
  owner?: any;
  specifiedSpatialDimension?: any[];
}

export interface LogisticsTransportMovement {
  id?: Identifier;
  stageCode?: string;
  modeCode?: string;
  usedTransportMeans?: UsedTransportMeans;
  departureEvent?: TransportEvent[];
  arrivalEvent?: TransportEvent[];
  borderCrossingEvent?: any[];
  callEvent?: any[];
  dangerousGoodsIndicator?: boolean;
  event?: any[];
  itineraryRoute?: any[];
  loadingEvent?: any[];
  master?: any;
  sequenceNumber?: any[];
  unloadingEvent?: any[];
}

export interface TransportCargo {
  identificationText?: LocalizedString[];
  typeCode?: string;
  operationalCategoryCode?: any;
  statisticalClassificationCode?: any;
}

export interface TransportEvent {
  actualOccurrenceDateTime?: DateTime;
  estimatedOccurrenceDateTime?: DateTime;
  occurrenceLocation?: LogisticsLocation;
  typeCode?: string;
  additionalSecurityMeasures?: any[];
  certifyingParty?: any[];
  relatedObservation?: any[];
  requestedOccurrenceDateTime?: DateTime;
  scheduledOccurrenceDateTime?: DateTime;
  scheduledOccurrencePeriod?: any;
}

export interface TradePrice {
  basisQuantity?: number;
  categoryTypeCode?: string;
  unitAmount?: Amount[];
}

export interface ExemptionCalculation {
  hazardCategoryCode?: string[];
  reportableQuantity?: number[];
}

export interface RegulatoryExemption {
  id?: Identifier;
  reportableExemptionCalculation?: ExemptionCalculation[];
  typeCode?: string[];
}

export interface Measurement {
  conditionMeasure?: Measure[];
  typeCode?: string[];
}

export interface SpecifiedFuel {
  typeCode?: string;
  volumeMeasure?: Measure[];
  weightMeasure?: Measure[];
  workingPressureMeasure?: Measure[];
}

export interface SpecifiedRadioactiveIsotope {
  activityLevelMeasure?: Measure[];
  name?: LocalizedString[];
}

export interface RadioactiveMaterial {
  applicableRadioactiveIsotope?: SpecifiedRadioactiveIsotope[];
  fissileCriticalitySafetyIndexNumber?: number;
  radioactivePackageTransportIndexCode?: string[];
  specialFormInformation?: LocalizedString[];
}

export interface CalibratedMeasurement {
  valueMeasure?: Measure;
}

export interface SpecifiedCondition {
  actionCode?: string[];
  actionDateTime?: DateTime[];
  calibratedMeasurement?: CalibratedMeasurement[];
  statementCode?: string[];
  statementText?: LocalizedString[];
  subjectTypeCode?: string[];
  valueMeasure?: Measure[];
}

export interface Note {
  contentText?: LocalizedString[];
  subjectCode?: string[];
}

export interface ReferencedLogisticsTransportEquipment {
  id?: Identifier;
}

export interface SpatialDimension {
  description?: any[];
  height?: Measure;
  length?: Measure;
  width?: Measure;
}

export interface LogisticsSeal {
  conditionCode?: any[];
  id?: Identifier;
  issuingParty?: TradeParty;
  sealingPartyRoleCode?: string;
}

export interface AssociatedTransportEquipment {
  affixedSeal?: LogisticsSeal[];
  categoryCode?: string[];
  goodsItemUnitQuantity?: number[];
  grossGoodsVolumeMeasure?: Measure[];
  grossGoodsWeightMeasure?: Measure[];
  grossWeight?: Measure;
  id?: Identifier;
  loadedDangerousGoods?: TransportDangerousGoods[];
  netGoodsVolumeMeasure?: Measure[];
  netGoodsWeightMeasure?: Measure[];
  reportableQuantity?: any[];
  sequenceNumber?: number;
  stowagePositionID?: Identifier;
  usedCapacityCode?: string;
  verifiedGrossWeight?: Measure[];
  weightVerificationMethodCode?: string[];
  weightVerifierParty?: TradeParty[];
}

export interface TradeCountry {
  code?: string;
}

export interface LogisticsServiceCharge {
  appliedAmount?: Amount[];
  calculationBasisCode?: string;
  calculationBasisPrice?: TradePrice;
  id?: string;
  payingPartyRoleCode?: string;
  paymentArrangementCode?: string;
}

export interface TransportDangerousGoods {
  applicableRegulatoryExemption?: RegulatoryExemption[];
  controlTemperature?: Measurement;
  dangerousGoodsLogisticsPackage?: LogisticsPackage[];
  densityMeasure?: Measure[];
  emergencyTemperature?: Measurement;
  explosiveCargoNetWeight?: Measure;
  grossVolume?: Measure;
  grossWeight?: Measure;
  hazardCategoryCode?: string;
  hazardClassificationID?: Identifier;
  hazardTypeCode?: string[];
  includedFuel?: SpecifiedFuel[];
  information?: LocalizedString[];
  labelTypeCode?: string[];
  limitedQuantityCode?: string;
  meltingPointTemperatureMeasure?: Measure[];
  netWeight?: Measure;
  packagingDangerLevelCode?: string;
  previousCargoInformation?: LocalizedString[];
  properShippingName?: LocalizedString[];
  radioactiveMaterial?: RadioactiveMaterial;
  regulatoryAuthorityName?: string;
  relatedDocument?: ReferencedDocument[];
  reportableQuantity?: number;
  specialProvisionID?: Identifier;
  statedCondition?: SpecifiedCondition[];
  supplementaryInformation?: LocalizedString[];
  technicalName?: LocalizedString[];
  technicalNameNote?: Note[];
  tunnelRestrictionCode?: string;
  undgid?: string;
}

export interface SupplyChainConsignmentItem {
  associatedTransportEquipment?: ReferencedLogisticsTransportEquipment[];
  dimensions?: SpatialDimension;
  goodsUnitQuantity?: number[];
  grossVolume?: Measure[];
  transportDangerousGoods?: TransportDangerousGoods[];
}

export interface LogisticsRiskAnalysisResult {
  consignmentRiskRelatedCode?: string[];
  description?: LocalizedString[];
  informationText?: LocalizedString[];
  levelCode?: string;
  screeningMethodCode?: string[];
  securityExemptionCode?: string[];
}

export interface TransportationWasteMaterial {
  id?: Identifier;
  weight?: Measure[];
}

export interface CrossBorderRegulatoryProcedure {
  exportCustomsOfficeLocation?: LogisticsLocation;
  importCustomsOfficeLocation?: LogisticsLocation;
  transitCustomsOfficeLocation?: LogisticsLocation[];
  typeCode?: string[];
}

export interface TransportService {
  conditionTypeCode?: string[];
  description?: string;
}

export interface LogisticsTransportEquipment {
  affixedSeal?: LogisticsSeal[];
  carriedTransportEquipment?: AssociatedTransportEquipment[];
  categoryCode?: string;
  fullEmptyCode?: string;
  grossGoodsVolumeMeasure?: Measure[];
  grossGoodsWeightMeasure?: Measure[];
  grossVolume?: Measure;
  id?: Identifier;
  loadedDangerousGoods?: TransportDangerousGoods[];
  netGoodsVolumeMeasure?: Measure[];
  netGoodsWeightMeasure?: Measure[];
  operatingParty?: TradeParty;
  ownerParty?: TradeParty[];
  registrationCountry?: TradeCountry;
  reportableQuantity?: any[];
  sealQuantity?: number;
  sequenceNumber?: number;
  sizeTypeCode?: string;
  stowagePosition?: Identifier;
}

// =================================================================
//
// Main eFTI Data Set Type
//
// =================================================================

export interface SupplyChainConsignment {
  applicableServiceCharge?: LogisticsServiceCharge[];
  associatedDocument?: AssociatedDocument[];
  associatedParty?: TradeParty[];
  codamount?: Amount;
  cargoInsuranceInstructions?: string[];
  carrier?: TradeParty;
  carrierAcceptanceDateTime?: DateTime;
  carrierAcceptanceLocation?: LogisticsLocation;
  connectingCarrier?: TradeParty[];
  consignee?: TradeParty;
  consigneeReceiptLocation?: LogisticsLocation;
  consignor?: TradeParty;
  consignorProvidedInformationText?: string[];
  consignorProvidedBorderClearanceInstructions?: any[];
  contractTermsText?: string[];
  dangerousGoods?: TransportDangerousGoods;
  declaredValueForCarriageAmount?: Amount;
  deliveryEvent?: TransportEvent;
  deliveryInformation?: any;
  freightForwarder?: TradeParty;
  grossVolume?: Measure[];
  grossWeight?: Measure[];
  includedConsignmentItem?: SupplyChainConsignmentItem[];
  information?: any[];
  logisticsRiskAnalysisResult?: LogisticsRiskAnalysisResult[];
  mainCarriageTransportMovement?: LogisticsTransportMovement[];
  natureIdentificationCargo?: TransportCargo;
  netWeight?: Measure[];
  notifiedWasteMaterial?: TransportationWasteMaterial[];
  numberOfPackages?: number;
  onCarriageTransportMovement?: LogisticsTransportMovement[];
  paymentArrangementCode?: string;
  preCarriageTransportMovement?: LogisticsTransportMovement[];
  regulatoryProcedure?: CrossBorderRegulatoryProcedure[];
  specifiedTransportMovement?: LogisticsTransportMovement[];
  transportContractDocument?: ReferencedDocument;
  transportEquipmentQuantity?: number[];
  transportEvent?: TransportEvent[];
  transportPackage?: LogisticsPackage[];
  transportService?: TransportService[];
  transshipmentLocation?: LogisticsLocation[];
  transshipmentPermittedIndicator?: boolean;
  usedTransportEquipment?: LogisticsTransportEquipment[];
}
