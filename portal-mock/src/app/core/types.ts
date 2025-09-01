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

// All measure types from the XSD can be simplified to the base Measure type
// for frontend purposes, unless specific unit validation is needed.
export type WeightUnitMeasure = Measure;
export type VolumeUnitMeasure = Measure;
export type LinearUnitMeasure = Measure;
export type DurationUnitMeasure = Measure;
export type UnitMeasure = Measure;
export type TemperatureUnitMeasure = Measure;


export interface Identifier {
  value?: string;
  schemeAgencyId?: string;
}

// All identifier types can be simplified to the base Identifier type.
export type Identifier17 = Identifier;
export type Identifier35 = Identifier;
export type Identifier70 = Identifier;
export type Identifier512 = Identifier;
export type Identifier7 = Identifier;


export interface LocalizedString {
  value?: string;
  languageId?: string;
}

// All localized string types
export type LocalizedString35 = LocalizedString;
export type LocalizedString70 = LocalizedString;
export type LocalizedString128 = LocalizedString;
export type LocalizedString256 = LocalizedString;
export type LocalizedString512 = LocalizedString;


// Simple string-based types from XSD
export type LimitedString17 = string;
export type LimitedString35 = string;
export type LimitedString70 = string;
export type LimitedString256 = string;

// Simple number-based types from XSD
export type Integer8 = number;
export type Integer16 = number;
export type Decimal16d6 = number;

// Binary types
export type Base64Binary = string;

// Code types from the 'codes.xsd' can be represented as strings
export type TransportEquipmentCategoryCode = string;
export type TransportModeCode = string;
export type CountryCode = string;

// =================================================================
//
// Complex Types from eu.efti.v1.consignment.common
//
// =================================================================

export interface AssociatedTransportEquipment {
  affixedSeal?: LogisticsSeal[];
  categoryCode?: TransportEquipmentCategoryCode[];
  goodsItemUnitQuantity?: Integer8[];
  grossGoodsVolumeMeasure?: VolumeUnitMeasure[];
  grossGoodsWeightMeasure?: WeightUnitMeasure[];
  grossWeight?: Measure;
  id?: Identifier17;
  loadedDangerousGoods?: TransportDangerousGoods[];
  netGoodsVolumeMeasure?: VolumeUnitMeasure[];
  netGoodsWeightMeasure?: WeightUnitMeasure[];
  reportableQuantity?: Integer8[];
  sequenceNumber?: Integer16;
  stowagePositionID?: Identifier17;
  usedCapacityCode?: string;
  verifiedGrossWeight?: WeightUnitMeasure[];
  weightVerificationMethodCode?: string[];
  weightVerifierParty?: TradeParty[];
}

export interface AttachedTransportEquipment {
  categoryCode?: TransportEquipmentCategoryCode[];
  id?: Identifier17;
  unitQuantity?: Integer8;
}

export interface AuthoritativeSignatoryPerson {
  id?: Identifier17;
  name?: LimitedString35;
}

export interface CalibratedMeasurement {
  valueMeasure?: Measure;
}

export interface ContactPerson {
  familyName?: LimitedString35[];
  givenName?: LimitedString35;
}

export interface CreditorFinancialAccount {
  proprietaryID?: Identifier17;
}

export interface CrossBorderRegulatoryProcedure {
  exportCustomsOfficeLocation?: LogisticsLocation;
  importCustomsOfficeLocation?: LogisticsLocation;
  transitCustomsOfficeLocation?: LogisticsLocation[];
  typeCode?: string[];
}

export interface DocumentAuthentication {
  actualDateTime?: DateTime;
  statementCode?: string;
}

export interface DocumentClause {
  contentText?: LimitedString256[];
}

export interface ExemptionCalculation {
  hazardCategoryCode?: string[];
  reportableQuantity?: Integer8[];
}

export interface GeographicalCoordinate {
  latitude?: Measure;
  longitude?: Measure;
}

export interface LogisticsLocation {
  geographicalCoordinates?: GeographicalCoordinate;
  id?: Identifier17;
  name?: LimitedString256[];
  postalAddress?: TradeAddress;
}

export interface LogisticsPackage {
  information?: LimitedString35[];
  itemQuantity?: Integer8;
  referencedDocument?: ReferencedDocument[];
  shippingMarks?: LogisticsShippingMarks[];
  statedCondition?: SpecifiedCondition[];
  typeCode?: string[];
  typeText?: LocalizedString35[];
}

export interface LogisticsRiskAnalysisResult {
  consignmentRiskRelatedCode?: string[];
  description?: LocalizedString128[];
  informationText?: LocalizedString128[];
  levelCode?: string;
  screeningMethodCode?: string[];
  securityExemptionCode?: string[];
}

export interface LogisticsSeal {
  conditionCode?: string[];
  id?: Identifier70;
  issuingParty?: TradeParty;
  sealingPartyRoleCode?: string;
}

export interface LogisticsServiceCharge {
  appliedAmount?: Amount[];
  calculationBasisCode?: string;
  calculationBasisPrice?: TradePrice;
  id?: string;
  payingPartyRoleCode?: string;
  paymentArrangementCode?: string;
}

export interface LogisticsShippingMarks {
  markingText?: LocalizedString70[];
}

export interface LogisticsTransportEquipment {
  affixedSeal?: LogisticsSeal[];
  carriedTransportEquipment?: AssociatedTransportEquipment[];
  categoryCode?: TransportEquipmentCategoryCode;
  fullEmptyCode?: string;
  grossGoodsVolumeMeasure?: VolumeUnitMeasure[];
  grossGoodsWeightMeasure?: WeightUnitMeasure[];
  grossVolume?: VolumeUnitMeasure;
  id?: Identifier17;
  loadedDangerousGoods?: TransportDangerousGoods[];
  netGoodsVolumeMeasure?: VolumeUnitMeasure[];
  netGoodsWeightMeasure?: WeightUnitMeasure[];
  operatingParty?: TradeParty;
  ownerParty?: TradeParty[];
  registrationCountry?: TradeCountry;
  reportableQuantity?: Integer8[];
  sealQuantity?: Integer8;
  sequenceNumber?: Integer16;
  sizeTypeCode?: string;
  stowagePosition?: Identifier17;
}

export interface LogisticsTransportMeans {
  attachedOperationalEquipment?: AttachedTransportEquipment[];
  id?: Identifier17;
  name?: LimitedString70;
  owner?: TradeParty;
  registrationCountry?: TradeCountry;
  specifiedSpatialDimension?: SpatialDimension[];
  typeCode?: string;
}

export interface LogisticsTransportMovement {
  arrivalEvent?: TransportEvent[];
  borderCrossingEvent?: TransportEvent[];
  callEvent?: TransportEvent[];
  dangerousGoodsIndicator?: boolean;
  departureEvent?: TransportEvent[];
  event?: TransportEvent[];
  id?: Identifier17;
  itineraryRoute?: TransportRoute[];
  loadingEvent?: TransportEvent[];
  master?: TransportPerson;
  modeCode?: TransportModeCode;
  sequenceNumber?: Integer16[];
  unloadingEvent?: TransportEvent[];
  usedTransportMeans?: LogisticsTransportMeans;
}

export interface Measurement {
  conditionMeasure?: Measure[];
  typeCode?: string[];
}

export interface Note {
  contentText?: LocalizedString256[];
  subjectCode?: string[];
}

export interface RadioactiveMaterial {
  applicableRadioactiveIsotope?: SpecifiedRadioactiveIsotope[];
  fissileCriticalitySafetyIndexNumber?: Decimal16d6;
  radioactivePackageTransportIndexCode?: string[];
  specialFormInformation?: LocalizedString70[];
}

export interface ReferencedDocument {
  attachedBinaryFile?: SpecifiedBinaryFile[];
  attachedBinaryObject?: Base64Binary[];
  contractualClause?: DocumentClause[];
  formattedIssueDateTime?: DateTime;
  id?: Identifier35;
  issueLocation?: LogisticsLocation;
  issuer?: TradeParty;
  referenceTypeCode?: string;
  subtypeCode?: string[];
  typeCode?: string;
  uri?: Identifier512;
}

export interface ReferencedLogisticsTransportEquipment {
  id?: Identifier17;
}

export interface RegulatoryExemption {
  id?: Identifier17;
  reportableExemptionCalculation?: ExemptionCalculation[];
  typeCode?: string[];
}

export interface RepresentativePerson {
  name?: LimitedString35;
}

export interface SpatialDimension {
  description?: LimitedString35[];
  height?: LinearUnitMeasure;
  length?: LinearUnitMeasure;
  width?: LinearUnitMeasure;
}

export interface SpecifiedBinaryFile {
  id?: Identifier17;
  includedBinaryObject?: Base64Binary[];
}

export interface SpecifiedCondition {
  actionCode?: string[];
  actionDateTime?: DateTime[];
  calibratedMeasurement?: CalibratedMeasurement[];
  statementCode?: string[];
  statementText?: LocalizedString70[];
  subjectTypeCode?: string[];
  valueMeasure?: Measure[];
}

export interface SpecifiedFuel {
  typeCode?: string;
  volumeMeasure?: VolumeUnitMeasure[];
  weightMeasure?: WeightUnitMeasure[];
  workingPressureMeasure?: UnitMeasure[];
}

export interface SpecifiedLicence {
  id?: Identifier17;
  typeCode?: string[];
}

export interface SpecifiedLocation {
  name?: LimitedString35[];
}

export interface SpecifiedObservation {
  applicableNote?: Note[];
}

export interface SpecifiedPeriod {
  durationMeasure?: DurationUnitMeasure[];
  endDateTime?: DateTime;
  maximumDurationMeasure?: DurationUnitMeasure;
  startDateTime?: DateTime;
}

export interface SpecifiedRadioactiveIsotope {
  activityLevelMeasure?: UnitMeasure[];
  name?: LocalizedString35[];
}

export interface SupplyChainConsignmentItem {
  associatedTransportEquipment?: ReferencedLogisticsTransportEquipment[];
  dimensions?: SpatialDimension;
  goodsUnitQuantity?: Integer8[];
  grossVolume?: VolumeUnitMeasure[];
  transportDangerousGoods?: TransportDangerousGoods[];
}

export interface TaxRegistration {
  id?: Identifier35;
}

export interface TradeAddress {
  additionalStreetName?: LimitedString35;
  buildingNumber?: LimitedString17;
  cityName?: LimitedString35[];
  countryCode?: CountryCode;
  countrySubDivisionName?: LimitedString35[];
  departmentName?: LimitedString35;
  postOfficeBox?: LimitedString17;
  postcode?: string[];
  streetName?: LimitedString35[];
}

export interface TradeContact {
  emailAddress?: UniversalCommunication;
  fax?: UniversalCommunication[];
  personName?: LimitedString35[];
  telephone?: UniversalCommunication[];
}

export interface TradeContract {
  durationMeasure?: DurationUnitMeasure[];
  issueDateTime?: DateTime;
  signedLocation?: SpecifiedLocation[];
}

export interface TradeCountry {
  code?: CountryCode;
}

export interface TradeParty {
  agreedContract?: TradeContract[];
  applicableLicence?: SpecifiedLicence[];
  authoritativeSignatoryPerson?: AuthoritativeSignatoryPerson[];
  confirmedDocumentAuthentication?: DocumentAuthentication[];
  definedContactDetails?: TradeContact[];
  id?: Identifier17;
  name?: LimitedString70[];
  ownedCreditorFinancialAccount?: CreditorFinancialAccount;
  postalAddress?: TradeAddress;
  representativePerson?: RepresentativePerson;
  roleCode?: string[];
  specifiedContactPerson?: ContactPerson[];
  taxRegistration?: TaxRegistration[];
}

export interface TradePrice {
  basisQuantity?: Integer8;
  categoryTypeCode?: string;
  unitAmount?: Amount[];
}

export interface TransportCargo {
  identificationText?: LocalizedString512[];
  operationalCategoryCode?: string;
  statisticalClassificationCode?: string;
  typeCode?: string;
}

export interface TransportDangerousGoods {
  applicableRegulatoryExemption?: RegulatoryExemption[];
  controlTemperature?: Measurement;
  dangerousGoodsLogisticsPackage?: LogisticsPackage[];
  densityMeasure?: UnitMeasure[];
  emergencyTemperature?: Measurement;
  explosiveCargoNetWeight?: WeightUnitMeasure;
  grossVolume?: VolumeUnitMeasure;
  grossWeight?: WeightUnitMeasure;
  hazardCategoryCode?: string;
  hazardClassificationID?: Identifier7;
  hazardTypeCode?: string[];
  includedFuel?: SpecifiedFuel[];
  information?: LocalizedString256[];
  labelTypeCode?: string[];
  limitedQuantityCode?: string[];
  meltingPointTemperatureMeasure?: TemperatureUnitMeasure[];
  netWeight?: WeightUnitMeasure;
  packagingDangerLevelCode?: string;
  previousCargoInformation?: LocalizedString256[];
  properShippingName?: LocalizedString128[];
  radioactiveMaterial?: RadioactiveMaterial;
  regulatoryAuthorityName?: LimitedString35;
  relatedDocument?: ReferencedDocument[];
  reportableQuantity?: Integer8;
  specialProvisionID?: Identifier17;
  statedCondition?: SpecifiedCondition[];
  supplementaryInformation?: LocalizedString128[];
  technicalName?: LocalizedString128[];
  technicalNameNote?: Note[];
  tunnelRestrictionCode?: string;
  undgid?: string;
}

export interface TransportEvent {
  actualOccurrenceDateTime?: DateTime;
  additionalSecurityMeasures?: Note[];
  certifyingParty?: TradeParty[];
  estimatedOccurrenceDateTime?: DateTime;
  occurrenceLocation?: LogisticsLocation;
  relatedObservation?: SpecifiedObservation[];
  requestedOccurrenceDateTime?: DateTime;
  scheduledOccurrenceDateTime?: DateTime;
  scheduledOccurrencePeriod?: SpecifiedPeriod;
  typeCode?: string;
}

export interface TransportInstructions {
  description?: LimitedString256[];
}

export interface TransportPerson {
  birthDateTime?: DateTime;
  familyName?: LimitedString35[];
  givenName?: LimitedString35[];
  id?: Identifier17;
}

export interface TransportRoute {
  description?: LimitedString256;
}

export interface TransportService {
  conditionTypeCode?: string[];
  description?: LimitedString256;
}

export interface TransportationWasteMaterial {
  id?: Identifier17;
  weight?: WeightUnitMeasure[];
}

export interface UniversalCommunication {
  completeNumber?: LimitedString35;
  uri?: Identifier35;
}

// =================================================================
//
// Main eFTI Data Set Type
//
// =================================================================

export interface SupplyChainConsignment {
  applicableServiceCharge?: LogisticsServiceCharge[];
  associatedDocument?: ReferencedDocument[];
  associatedParty?: TradeParty[];
  codamount?: Amount;
  cargoInsuranceInstructions?: LimitedString256[];
  carrier?: TradeParty;
  carrierAcceptanceDateTime?: DateTime;
  carrierAcceptanceLocation?: LogisticsLocation;
  connectingCarrier?: TradeParty[];
  consignee?: TradeParty;
  consigneeReceiptLocation?: LogisticsLocation;
  consignor?: TradeParty;
  consignorProvidedBorderClearanceInstructions?: TransportInstructions[];
  consignorProvidedInformationText?: LimitedString256[];
  contractTermsText?: LimitedString256[];
  dangerousGoods?: TransportDangerousGoods;
  declaredValueForCarriageAmount?: Amount;
  deliveryEvent?: TransportEvent;
  deliveryInformation?: LimitedString256;
  freightForwarder?: TradeParty;
  grossVolume?: VolumeUnitMeasure[];
  grossWeight?: WeightUnitMeasure[];
  includedConsignmentItem?: SupplyChainConsignmentItem[];
  information?: LimitedString256[];
  logisticsRiskAnalysisResult?: LogisticsRiskAnalysisResult[];
  mainCarriageTransportMovement?: LogisticsTransportMovement[];
  natureIdentificationCargo?: TransportCargo;
  netWeight?: WeightUnitMeasure[];
  notifiedWasteMaterial?: TransportationWasteMaterial[];
  numberOfPackages?: Integer8;
  onCarriageTransportMovement?: LogisticsTransportMovement[];
  paymentArrangementCode?: string;
  preCarriageTransportMovement?: LogisticsTransportMovement[];
  regulatoryProcedure?: CrossBorderRegulatoryProcedure[];
  specifiedTransportMovement?: LogisticsTransportMovement[];
  transportContractDocument?: ReferencedDocument;
  transportEquipmentQuantity?: Integer8[];
  transportEvent?: TransportEvent[];
  transportPackage?: LogisticsPackage[];
  transportService?: TransportService[];
  transshipmentLocation?: LogisticsLocation[];
  transshipmentPermittedIndicator?: boolean;
  usedTransportEquipment?: LogisticsTransportEquipment[];
}
