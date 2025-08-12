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
  unitCode?: string;
}

export interface Identifier {
  value?: string;
  schemeId?: string;
}

export interface LocalizedString {
  value?: string;
  languageID?: string;
}

// =================================================================
//
// Complex Types from eu.efti.v1.consignment.common
//
// =================================================================

export interface PostalAddress {
  streetName?: string[];
  additionalStreetName?: string[];
  buildingNumber?: string[];
  cityName?: string[];
  postcode?: string[];
  countrySubDivisionName?: string[];
  countryCode?: { value?: string };
}

export interface GeographicalCoordinates {
  latitude?: Measure;
  longitude?: Measure;
}

export interface DefinedContactDetails {
  personName?: string[];
  telephone?: { completeNumber?: string }[];
  emailAddress?: { completeNumber?: string }[];
}

export interface TradeParty {
  id?: Identifier;
  name?: string[];
  postalAddress?: PostalAddress;
  roleCode?: string[];
  definedContactDetails?: DefinedContactDetails[];
}

export interface LogisticsLocation {
  id?: Identifier;
  name?: string[];
  geographicalCoordinates?: GeographicalCoordinates;
  postalAddress?: PostalAddress;
}

export interface AttachedBinaryFile {
  id?: Identifier;
  includedBinaryObject?: string; // Base64 encoded
}

export interface AssociatedDocument {
  id?: Identifier;
  uRI?: Identifier;
  typeCode?: string[];
  subtypeCode?: string[];
  referenceTypeCode?: string[];
  issuer?: TradeParty;
  issueLocation?: LogisticsLocation;
  formattedIssueDateTime?: DateTime;
  attachedBinaryFile?: AttachedBinaryFile;
}

export interface ReferencedDocument {
  id?: Identifier;
  typeCode?: string;
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
  name?: string[];
  registrationCountry?: TradeCountry;
}

export interface LogisticsTransportMovement {
  id?: Identifier;
  stageCode?: string;
  modeCode?: string[];
  usedTransportMeans?: UsedTransportMeans;
  departureEvent?: TransportEvent;
  arrivalEvent?: TransportEvent;
}

export interface TransportCargo {
  identificationText?: { value: string }[];
  typeCode?: string[];
}

export interface TransportEvent {
  actualOccurrenceDateTime?: DateTime;
  estimatedOccurrenceDateTime?: DateTime;
  occurrenceLocation?: LogisticsLocation;
  typeCode?: string[];
}


/**
 * Mirrors the eu.efti.v1.consignment.common.TradePrice Java class.
 */
export interface TradePrice {
  basisQuantity?: number;
  categoryTypeCode?: string;
  unitAmount?: Amount[];
}

/**
 * Mirrors the eu.efti.v1.consignment.common.ExemptionCalculation Java class.
 */
export interface ExemptionCalculation {
  hazardCategoryCode?: string[];
  reportableQuantity?: number[];
}

/**
 * Mirrors the eu.efti.v1.consignment.common.RegulatoryExemption Java class.
 */
export interface RegulatoryExemption {
  id?: Identifier;
  reportableExemptionCalculation?: ExemptionCalculation[];
  typeCode?: string[];
}

/**
 * Mirrors the eu.efti.v1.consignment.common.Measurement Java class.
 */
export interface Measurement {
  conditionMeasure?: Measure[];
  typeCode?: string[];
}

/**
 * Mirrors the eu.efti.v1.consignment.common.SpecifiedFuel Java class.
 */
export interface SpecifiedFuel {
  typeCode?: string;
  volumeMeasure?: Measure[];
  weightMeasure?: Measure[];
  workingPressureMeasure?: Measure[];
}

/**
 * Mirrors the eu.efti.v1.consignment.common.SpecifiedRadioactiveIsotope Java class.
 */
export interface SpecifiedRadioactiveIsotope {
  activityLevelMeasure?: Measure[];
  name?: LocalizedString[];
}

/**
 * Mirrors the eu.efti.v1.consignment.common.RadioactiveMaterial Java class.
 */
export interface RadioactiveMaterial {
  applicableRadioactiveIsotope?: SpecifiedRadioactiveIsotope[];
  fissileCriticalitySafetyIndexNumber?: number;
  radioactivePackageTransportIndexCode?: string[];
  specialFormInformation?: LocalizedString[];
}

/**
 * Mirrors the eu.efti.v1.consignment.common.CalibratedMeasurement Java class.
 */
export interface CalibratedMeasurement {
  valueMeasure?: Measure;
}

/**
 * Mirrors the eu.efti.v1.consignment.common.SpecifiedCondition Java class.
 */
export interface SpecifiedCondition {
  actionCode?: string[];
  actionDateTime?: DateTime[];
  calibratedMeasurement?: CalibratedMeasurement[];
  statementCode?: string[];
  statementText?: LocalizedString[];
  subjectTypeCode?: string[];
  valueMeasure?: Measure[];
}

/**
 * Mirrors the eu.efti.v1.consignment.common.Note Java class.
 */
export interface Note {
  contentText?: LocalizedString[];
  subjectCode?: string[];
}

/**
 * Mirrors the eu.efti.v1.consignment.common.ReferencedLogisticsTransportEquipment Java class.
 */
export interface ReferencedLogisticsTransportEquipment {
  id?: Identifier;
}

/**
 * Mirrors the eu.efti.v1.consignment.common.SpatialDimension Java class.
 */
export interface SpatialDimension {
  description?: string[];
  height?: Measure;
  length?: Measure;
  width?: Measure;
}

/**
 * Mirrors the eu.efti.v1.consignment.common.LogisticsSeal Java class.
 */
export interface LogisticsSeal {
  conditionCode?: string[];
  id?: Identifier;
  issuingParty?: TradeParty;
  sealingPartyRoleCode?: string;
}

/**
 * Mirrors the eu.efti.v1.consignment.common.AssociatedTransportEquipment Java class.
 */
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
  reportableQuantity?: number[];
  sequenceNumber?: number;
  stowagePositionID?: Identifier;
  usedCapacityCode?: string[];
  verifiedGrossWeight?: Measure[];
  weightVerificationMethodCode?: string[];
  weightVerifierParty?: TradeParty[];
}

/**
 * Mirrors the eu.efti.v1.consignment.common.TradeCountry Java class.
 */
export interface TradeCountry {
  code?: string;
}

/**
 * Represents charges for logistics services.
 * Mirrors the eu.efti.v1.consignment.common.LogisticsServiceCharge Java class.
 */
export interface LogisticsServiceCharge {
  appliedAmount?: Amount[];
  calculationBasisCode?: string;
  calculationBasisPrice?: TradePrice;
  id?: string;
  payingPartyRoleCode?: string;
  paymentArrangementCode?: string;
}

/**
 * Represents information about the transport of dangerous goods.
 * Mirrors the eu.efti.v1.consignment.common.TransportDangerousGoods Java class.
 */
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
  limitedQuantityCode?: string[];
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

/**
 * Defines an item within a supply chain consignment.
 * Mirrors the eu.efti.v1.consignment.common.SupplyChainConsignmentItem Java class.
 */
export interface SupplyChainConsignmentItem {
  associatedTransportEquipment?: ReferencedLogisticsTransportEquipment[];
  dimensions?: SpatialDimension;
  goodsUnitQuantity?: number[];
  grossVolume?: Measure[];
  transportDangerousGoods?: TransportDangerousGoods[];
}

/**
 * Holds the results of a logistics risk analysis.
 * Mirrors the eu.efti.v1.consignment.common.LogisticsRiskAnalysisResult Java class.
 */
export interface LogisticsRiskAnalysisResult {
  consignmentRiskRelatedCode?: string[];
  description?: LocalizedString[];
  informationText?: LocalizedString[];
  levelCode?: string;
  screeningMethodCode?: string[];
  securityExemptionCode?: string[];
}

/**
 * Represents waste material being transported.
 * Mirrors the eu.efti.v1.consignment.common.TransportationWasteMaterial Java class.
 */
export interface TransportationWasteMaterial {
  id?: Identifier;
  weight?: Measure[];
}

/**
 * Details procedures for crossing borders, such as customs.
 * Mirrors the eu.efti.v1.consignment.common.CrossBorderRegulatoryProcedure Java class.
 */
export interface CrossBorderRegulatoryProcedure {
  exportCustomsOfficeLocation?: LogisticsLocation;
  importCustomsOfficeLocation?: LogisticsLocation;
  transitCustomsOfficeLocation?: LogisticsLocation[];
  typeCode?: string[];
}

/**
 * Describes a specific service related to the transport.
 * Mirrors the eu.efti.v1.consignment.common.TransportService Java class.
 */
export interface TransportService {
  conditionTypeCode?: string[];
  description?: string;
}

/**
 * Provides details about the transport equipment used (e.g., containers).
 * Mirrors the eu.efti.v1.consignment.common.LogisticsTransportEquipment Java class.
 */
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
  reportableQuantity?: number[];
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

/**
 * Main eFTI Data Set Type.
 * Mirrors the eu.efti.v1.consignment.common.SupplyChainConsignment Java class.
 */
export interface SupplyChainConsignment {
  applicableServiceCharge?: LogisticsServiceCharge[];
  associatedDocument?: AssociatedDocument[];
  associatedParty?: TradeParty[];
  cODAmount?: Amount;
  cargoInsuranceInstructions?: string[];
  carrier?: TradeParty;
  carrierAcceptanceDateTime?: DateTime;
  carrierAcceptanceLocation?: LogisticsLocation;
  connectingCarrier?: TradeParty[];
  consignee?: TradeParty;
  consigneeReceiptLocation?: LogisticsLocation;
  consignor?: TradeParty;
  consignorProvidedInformationText?: string[];
  contractTermsText?: string[];
  dangerousGoods?: TransportDangerousGoods;
  declaredValueForCarriageAmount?: Amount;
  deliveryEvent?: TransportEvent;
  freightForwarder?: TradeParty;
  grossVolume?: Measure[];
  grossWeight?: Measure[];
  includedConsignmentItem?: SupplyChainConsignmentItem[];
  information?: string[];
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
