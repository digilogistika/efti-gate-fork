import { ChangeDetectionStrategy, Component, Input, OnInit } from '@angular/core';
import {
  Amount, Measure, TradeAddress, LocalizedString, LogisticsSeal, TradeParty, SpatialDimension, Note,
  RadioactiveMaterial, RegulatoryExemption, ExemptionCalculation, ReferencedDocument, SpecifiedCondition,
  SpecifiedFuel, LogisticsPackage, SpecifiedRadioactiveIsotope, ReferencedLogisticsTransportEquipment,
  TransportDangerousGoods, SpecifiedPeriod, GeographicalCoordinate, UniversalCommunication,
  AttachedTransportEquipment, TransportRoute, TaxRegistration, CreditorFinancialAccount, SpecifiedLicence,
  TradeContract, LogisticsLocation, SpecifiedObservation, DocumentAuthentication, SpecifiedBinaryFile,
  Identifier, DateTime, CrossBorderRegulatoryProcedure, Measurement
} from '../core/types';

@Component({
  selector: 'app-display-field',
  standalone: true,
  imports: [],
  template: `
    <dl>
      @if (hasValue) {
        <div class="py-2 sm:grid sm:grid-cols-3 sm:gap-4">
          <dt class="text-sm font-medium text-gray-600">{{ label }}</dt>
          <dd class="mt-1 text-sm text-gray-900 sm:mt-0" [innerHTML]="formattedValue"></dd>
        </div>
      }
    </dl>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DisplayFieldComponent implements OnInit {
  @Input() label: string = '';
  @Input() value: any;

  protected formattedValue: string = '';

  ngOnInit(): void {
    this.formattedValue = this.format(this.value);
  }

  get hasValue(): boolean {
    if (this.value === null || this.value === undefined) {
      return false;
    }
    if (typeof this.value === 'string' && this.value.trim() === '') {
      return false;
    }
    return !(Array.isArray(this.value) && this.value.length === 0);
  }

  // =================================================================
  // Formatting Logic Migrated from Pipe
  // =================================================================

  private format(value: any): string {
    if (Array.isArray(value)) {
      return value.map(item => this.format(item)).join('; ');
    }

    if (typeof value === 'object' && value !== null) {
      // Order is important for type guards
      if ('value' in value && 'formatId' in value) return this.formatDateTime((value as DateTime).value);
      if ('value' in value && 'schemeAgencyId' in value) return this.formatIdentifier(value);
      if ('code' in value && Object.keys(value).length === 1) return value.code || '';
      if ('value' in value && 'currencyId' in value) return this.formatAmount(value);
      if ('value' in value && 'unitId' in value) return this.formatMeasure(value);
      if ('occurrenceLocation' in value || 'actualOccurrenceDateTime' in value) return this.formatTransportEvent(value);
      if ('exportCustomsOfficeLocation' in value || 'importCustomsOfficeLocation' in value) return this.formatRegulatoryProcedure(value);
      if ('postalAddress' in value || 'geographicalCoordinates' in value) return this.formatLogisticsLocation(value);
      if ('streetName' in value || 'cityName' in value || 'countryCode' in value) return this.formatTradeAddress(value);
      if ('value' in value && 'languageId' in value) return (value as LocalizedString).value || '';
      if ('sealingPartyRoleCode' in value) return this.formatSeal(value);
      if ('roleCode' in value || 'givenName' in value || 'familyName' in value || ('name' in value && 'id' in value)) return this.formatPartyOrPerson(value);
      if ('height' in value || 'width' in value || 'length' in value) return this.formatDimension(value);
      if ('conditionMeasure' in value || ('typeCode' in value && Array.isArray((value as any).typeCode))) return this.formatMeasurement(value);
      if ('applicableNote' in value && Object.keys(value).length === 1) return this.formatSpecifiedObservation(value);
      if ('fissileCriticalitySafetyIndexNumber' in value || 'applicableRadioactiveIsotope' in value) return this.formatRadioactiveMaterial(value);
      if ('reportableExemptionCalculation' in value || ('id' in value && 'typeCode' in value && !('value' in value))) return this.formatRegulatoryExemption(value);
      if ('contentText' in value && 'subjectCode' in value) return this.formatNote(value);
      if ('includedBinaryObject' in value) return this.formatSpecifiedBinaryFile(value);
      if ('referenceTypeCode' in value) return this.formatDocument(value);
      if ('statementCode' in value || 'statementText' in value) return this.formatStatedCondition(value);
      if ('typeCode' in value && 'id' in value) return `${(value as SpecifiedLicence).id?.value || ''} (${(value as SpecifiedLicence).typeCode?.join(', ') || ''})`;
      if ('typeCode' in value) return this.formatFuel(value);
      if ('itemQuantity' in value) return this.formatLogisticsPackage(value);
      if ('activityLevelMeasure' in value) return this.formatIsotope(value);
      if ('markingText' in value) return this.asArray((value as any).markingText).map((t: any) => t.value).join(' ') || '';
      if ('id' in value && !('modeCode' in value)) return this.formatReferencedEquipment(value);
      if ('undgid' in value) return this.formatDangerousGoodsSummary(value);
      if ('startDateTime' in value || 'endDateTime' in value) return this.formatPeriod(value);
      if ('latitude' in value || 'longitude' in value) return this.formatCoordinates(value);
      if ('completeNumber' in value || 'uri' in value) return this.formatUniversalCommunication(value);
      if ('unitQuantity' in value && 'categoryCode' in value) return this.formatAttachedEquipment(value);
      if ('description' in value && typeof value.description === 'string') return (value as TransportRoute).description || '';
      if ('id' in value && Object.keys(value).length === 1) return (value as TaxRegistration).id?.value || '';
      if ('name' in value && typeof value.name === 'string') return value.name;
      if ('proprietaryID' in value) return this.formatFinancialAccount(value);
      if ('issueDateTime' in value || 'durationMeasure' in value) return this.formatContract(value);
      if ('actualDateTime' in value) return this.formatAuthentication(value);
      if ('value' in value && typeof value.value === 'string' && /^\d{12}$/.test(value.value)) return this.formatDateTime(value.value);
      return this.asMuted('[Object]');
    }

    if (typeof value === 'boolean') {
      return value ? 'Yes' : 'No';
    }
    if (typeof value === 'string' && /^\d{12}$/.test(value)) {
      return this.formatDateTime(value);
    }

    return String(value);
  }

  private formatSpecifiedBinaryFile(file: SpecifiedBinaryFile): string {
    const parts = [];
    if (file.id?.value) parts.push(`ID: ${file.id.value}${file.id.schemeAgencyId ? ` (${file.id.schemeAgencyId})` : ''}`);
    if (file.includedBinaryObject?.length) parts.push(`(${file.includedBinaryObject.length} binary object(s))`);
    return parts.join(' ') || this.asMuted('Empty file reference');
  }

  private formatAmount(amount: Amount): string {
    if (amount.value === undefined || amount.value === null) return '';
    return `${amount.value.toFixed(2)} ${amount.currencyId || ''}`.trim();
  }

  private formatMeasure(measure: Measure): string {
    return measure.value !== null ? `${measure.value} ${measure.unitId || ''}`.trim() : '';
  }

  private formatDateTime(dateTimeString: string | undefined | null): string {
    if (!dateTimeString || dateTimeString.length < 12) return '';
    const y = dateTimeString.substring(0, 4), m = dateTimeString.substring(4, 6), d = dateTimeString.substring(6, 8);
    const h = dateTimeString.substring(8, 10), min = dateTimeString.substring(10, 12);
    return `${y}-${m}-${d} ${h}:${min}${dateTimeString.substring(12) ? ` ${dateTimeString.substring(12)}` : ''}`;
  }

  private formatTradeAddress(pa: TradeAddress): string {
    return [pa.departmentName, pa.postOfficeBox, pa.buildingNumber, pa.streetName?.join(' '), pa.additionalStreetName,
      pa.cityName?.join(' '), pa.postcode?.join(' '), pa.countrySubDivisionName?.join(' '), pa.countryCode]
      .filter(Boolean).join(', ');
  }

  private formatTradePartySummary(party: TradeParty): string {
    return `${this.asArray(party.name).join(' ')} ${party.id?.value ? `(${party.id.schemeAgencyId || 'ID'}: ${party.id.value})` : ''}`.trim() || 'Details unavailable';
  }

  private formatSeal(s: LogisticsSeal): string {
    return [`ID: ${s.id?.value || ''}${s.id?.schemeAgencyId ? ` [${s.id.schemeAgencyId}]` : ''}`,
      s.sealingPartyRoleCode ? `By: ${s.sealingPartyRoleCode}` : '',
      s.conditionCode?.length ? `Condition: ${s.conditionCode.join(', ')}` : '',
      s.issuingParty ? `Issuer: ${this.formatTradePartySummary(s.issuingParty)}` : '']
      .filter(Boolean).join(' | ');
  }

  private formatDimension(d: SpatialDimension): string {
    const measurements = `L:${this.formatMeasure(d.length!)} W:${this.formatMeasure(d.width!)} H:${this.formatMeasure(d.height!)}`;
    return d.description?.length ? `${d.description.join(', ')} (${measurements})` : measurements;
  }

  private formatNote(n: Note): string {
    return `(${n.subjectCode?.join(', ') || ''}): ${n.contentText?.map(c => c.value).join(' ') || ''}`;
  }

  private formatDocument(d: ReferencedDocument): string {
    return `${d.id?.value || ''} (Type: ${d.typeCode || ''})`;
  }

  private formatStatedCondition(c: SpecifiedCondition): string {
    const parts = [
      c.statementCode?.length ? `Code: ${c.statementCode.join(', ')}` : '',
      c.statementText?.length ? `"${c.statementText.map(t => t.value).join(' ')}"` : '',
      c.subjectTypeCode?.length ? `Subject: ${c.subjectTypeCode.join(', ')}` : '',
      c.actionCode?.length ? `Action: ${c.actionCode.join(', ')}` : '',
      c.valueMeasure?.length ? `Value: ${c.valueMeasure.map(m => this.formatMeasure(m)).join(', ')}` : '',
      c.actionDateTime?.length ? `Action Time: ${c.actionDateTime.map(dt => this.formatDateTime(dt.value)).join(', ')}` : ''
    ];
    if (c.calibratedMeasurement?.length) {
      const cal = c.calibratedMeasurement.map(cm => this.formatMeasure(cm.valueMeasure!)).filter(Boolean).join('; ');
      if (cal) parts.push(`Calibration: [${cal}]`);
    }
    return parts.filter(Boolean).join(' | ') || this.asMuted('Condition details empty');
  }

  private formatFuel(f: SpecifiedFuel): string {
    return [`Type: ${f.typeCode || ''}`,
      f.volumeMeasure?.length ? `Vol: ${f.volumeMeasure.map(m => this.formatMeasure(m)).join(', ')}` : '',
      f.weightMeasure?.length ? `Weight: ${f.weightMeasure.map(m => this.formatMeasure(m)).join(', ')}` : '',
      f.workingPressureMeasure?.length ? `Pressure: ${f.workingPressureMeasure.map(m => this.formatMeasure(m)).join(', ')}` : '']
      .filter(Boolean).join(' | ');
  }

  private formatLogisticsPackage(p: LogisticsPackage): string {
    return `${p.itemQuantity ?? 'N/A'} x ${p.typeCode?.join(', ') || 'Unknown Type'}`;
  }

  private formatIsotope(i: SpecifiedRadioactiveIsotope): string {
    return `${i.name?.map(n => n.value).join(' ') || ''}: ${i.activityLevelMeasure?.map(m => this.formatMeasure(m)).join(', ') || ''}`;
  }

  private formatReferencedEquipment(e: ReferencedLogisticsTransportEquipment): string {
    return `${e.id?.value || 'Unknown ID'}${e.id?.schemeAgencyId ? ` (${e.id.schemeAgencyId})` : ''}`;
  }

  private formatDangerousGoodsSummary(dg: TransportDangerousGoods): string {
    return dg.undgid || 'Unknown UN ID';
  }

  private formatPeriod(p: SpecifiedPeriod): string {
    return [p.startDateTime?.value ? `Start: ${this.formatDateTime(p.startDateTime.value)}` : '',
      p.endDateTime?.value ? `End: ${this.formatDateTime(p.endDateTime.value)}` : '']
      .filter(Boolean).join(' | ');
  }

  private formatCoordinates(c: GeographicalCoordinate): string {
    return `Lat: ${c.latitude?.value ?? ''}, Lon: ${c.longitude?.value ?? ''}`;
  }

  private formatUniversalCommunication(comm: UniversalCommunication): string {
    return [comm.completeNumber,
      comm.uri?.value ? `<a href="${comm.uri.value}" target="_blank" class="text-blue-600 hover:underline text-xs ml-1">(URI)</a>` : '']
      .filter(Boolean).join(' ');
  }

  private formatAttachedEquipment(e: AttachedTransportEquipment): string {
    return [`ID: ${e.id?.value || ''}`,
      e.categoryCode?.length ? `Category: ${e.categoryCode.join(', ')}` : '',
      e.unitQuantity ? `Qty: ${e.unitQuantity}` : '']
      .filter(Boolean).join(' | ');
  }

  private formatFinancialAccount(a: CreditorFinancialAccount): string {
    return `${a.proprietaryID?.value || ''} (Scheme: ${a.proprietaryID?.schemeAgencyId || ''})`;
  }

  private formatRegulatoryProcedure(proc: CrossBorderRegulatoryProcedure): string {
    return [proc.typeCode?.length ? `Type: ${proc.typeCode.join(', ')}` : '',
      proc.exportCustomsOfficeLocation ? `Export Office: ${this.formatLogisticsLocation(proc.exportCustomsOfficeLocation)}` : '',
      proc.importCustomsOfficeLocation ? `Import Office: ${this.formatLogisticsLocation(proc.importCustomsOfficeLocation)}` : '']
      .filter(Boolean).join(' | ') || this.asMuted('Details unavailable');
  }

  private formatContract(c: TradeContract): string {
    const locations = c.signedLocation?.map(l => this.asArray(l.name) || '').filter(Boolean).join(', ');
    return [c.issueDateTime?.value ? `Issued: ${this.formatDateTime(c.issueDateTime.value)}` : '',
      locations ? `Signed at: ${locations}` : '']
      .filter(Boolean).join(' | ');
  }

  private formatAuthentication(a: DocumentAuthentication): string {
    return `${a.statementCode || 'Authenticated'} on ${this.formatDateTime(a.actualDateTime?.value)}`;
  }

  private formatRadioactiveMaterial(m: RadioactiveMaterial): string {
    const isotopes = m.applicableRadioactiveIsotope?.map(iso => this.formatIsotope(iso)).join('; ');
    return [m.fissileCriticalitySafetyIndexNumber ? `Criticality Index: ${m.fissileCriticalitySafetyIndexNumber}` : '',
      m.specialFormInformation?.length ? `Form Info: ${m.specialFormInformation.map(i => i.value).join(', ')}` : '',
      m.radioactivePackageTransportIndexCode?.length ? `Transport Index Code: ${m.radioactivePackageTransportIndexCode.join(', ')}` : '',
      isotopes ? `Isotopes: [${isotopes}]` : '']
      .filter(Boolean).join(' | ') || this.asMuted('Radioactive material details present');
  }

  private formatRegulatoryExemption(ex: RegulatoryExemption): string {
    const calcs = ex.reportableExemptionCalculation?.map(c => this.formatExemptionCalculation(c)).join('; ');
    return [ex.id?.value ? `ID: ${ex.id.value}` : '',
      ex.typeCode?.length ? `Type: ${ex.typeCode.join(', ')}` : '',
      calcs ? `Calculations: [${calcs}]` : '']
      .filter(Boolean).join(' | ') || this.asMuted('Exemption details present');
  }

  private formatExemptionCalculation(calc: ExemptionCalculation): string {
    return [calc.hazardCategoryCode?.length ? `Hazard Category: ${calc.hazardCategoryCode.join(', ')}` : '',
      calc.reportableQuantity?.length ? `Reportable Qty: ${calc.reportableQuantity.join(', ')}` : '']
      .filter(Boolean).join(', ') || this.asMuted('Calculation details empty');
  }

  private formatLogisticsLocation(loc: LogisticsLocation): string {
    return [loc.id?.value ? this.formatIdentifier(loc.id as Identifier) : '',
      loc.name?.length ? loc.name.join(', ') : '',
      loc.postalAddress ? `Address: ${this.formatTradeAddress(loc.postalAddress)}` : '',
      loc.geographicalCoordinates ? `Coords: ${this.formatCoordinates(loc.geographicalCoordinates)}` : '']
      .filter(Boolean).join(' | ') || this.asMuted('Location details unavailable');
  }

  private formatSpecifiedObservation(obs: SpecifiedObservation): string {
    return obs.applicableNote?.map(note => this.formatNote(note)).join('; ') || this.asMuted('No observation notes');
  }

  private formatMeasurement(m: Measurement): string {
    return [m.typeCode?.length ? `Type: ${m.typeCode.join(', ')}` : '',
      m.conditionMeasure?.length ? `Value: ${m.conditionMeasure.map(measure => this.formatMeasure(measure)).join('; ')}` : '']
      .filter(Boolean).join(' | ') || this.asMuted('Measurement details empty');
  }

  private formatIdentifier(id: Identifier): string {
    return id.value ? (id.schemeAgencyId ? `${id.value} (${id.schemeAgencyId})` : id.value) : '';
  }

  private formatPartyOrPerson(p: any): string {
    let name = '';
    if (p.name) name = this.asArray(p.name).join(' ');
    else if (p.givenName || p.familyName) name = [p.givenName, ...(this.asArray(p.familyName))].filter(Boolean).join(' ');

    return [name, p.id?.value ? `(${this.formatIdentifier(p.id)})` : '',
      p.birthDateTime?.value ? `DOB: ${this.formatDateTime(p.birthDateTime.value)}` : '']
      .filter(Boolean).join(' ') || this.asMuted('Details unavailable');
  }

  private formatTransportEvent(event: any): string {
    return [event.typeCode ? `Type: ${event.typeCode}` : '',
      event.actualOccurrenceDateTime?.value ? this.formatDateTime(event.actualOccurrenceDateTime.value) : '']
      .filter(Boolean).join(' | ') || this.asMuted('Event details unavailable');
  }

  private asMuted(text: string): string {
    return `<span class="text-gray-500 italic">${text}</span>`;
  }

  private asArray<T>(value: T | T[] | undefined | null): T[] {
    return value === null || value === undefined ? [] : (Array.isArray(value) ? value : [value]);
  }
}
