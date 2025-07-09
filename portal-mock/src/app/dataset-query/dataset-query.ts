import {Component, ElementRef, ViewChild} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {Location} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {memberStateSubsets} from '../core/subsets';
import {DatasetResponse} from '../core/types';
import {HttpClient} from '@angular/common/http';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import { XmlNode } from '../xml-viewer/xml-node';
import { XmlViewer } from '../xml-viewer/xml-viwer';

@Component({
  selector: 'app-dataset-query',
  imports: [
    ReactiveFormsModule,
    TranslatePipe,
    XmlViewer,
  ],
  templateUrl: './dataset-query.html',
})
export class DatasetQuery {
  @ViewChild('myDialog') dialog!: ElementRef<HTMLDialogElement>;
  @ViewChild('subsetDetails') subsetDetails!: ElementRef<HTMLDetailsElement>
  protected datasetQueryForm: FormGroup;
  protected readonly selectedSubsets = new Set<string>();
  protected isLoading: boolean = false;
  protected datasetQueryResponse: DatasetResponse | null = null;
  protected datasetQueryErrorMessage: string | null = null;
  protected followUpForm: FormGroup;
  protected showSuccessMessageForFollowUp: boolean = false;
  protected parsedXmlData: XmlNode[] | null = null;

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly fb: FormBuilder,
    private readonly http: HttpClient,
    private readonly location: Location,
    private readonly translate: TranslateService
  ) {
    const gateId = this.activatedRoute.snapshot.paramMap.get('gateId');
    const platformId = this.activatedRoute.snapshot.paramMap.get('platformId');
    const datasetId = this.activatedRoute.snapshot.paramMap.get('datasetId');

    this.datasetQueryForm = this.fb.group({
      gateId: [{value: gateId, disabled: true}, [Validators.required]],
      platformId: [{value: platformId, disabled: true}, [Validators.required]],
      datasetId: [{value: datasetId, disabled: true}, [Validators.required]],
      subsetIds: [new Set<string>(), [Validators.required, Validators.minLength(1)]]
    });

    this.followUpForm = this.fb.group({
      message: ['', [Validators.required]],
    });
  }

  onSubmit() {
    if (!this.datasetQueryForm.valid) {
      return
    }
    // request the identifiers endpoint for results
    const formValues = this.datasetQueryForm.getRawValue();

    let identifiersQuery: string = `/api/v1/dataset/${formValues.gateId}/${formValues.platformId}/${formValues.datasetId}`;
    identifiersQuery += "?subsets=" + Array.from(formValues.subsetIds).join(",");

    this.isLoading = true;
    this.http.get<DatasetResponse>(identifiersQuery)
      .subscribe({
        next: (v) => {
          this.datasetQueryResponse = v;
          this.isLoading = false;
          this.subsetDetails.nativeElement.open = false;
          this.datasetQueryErrorMessage = null; // Clear any previous error message

          if (v.data) {
            this.parseAndSetXml(v.data);
          }
        },
        error: (error) => {
          console.error('Error fetching dataset:', error);
          this.isLoading = false;
          this.datasetQueryErrorMessage = this.translate.instant('datasetQuery.errorFetchingDataset');
        }
      });
  }

  getAvailableSubsets() {
    // TODO in the real world the police of one country is only interested of the subsets of their country.
    // For now we return all subsets and let the user pick form them.
    return memberStateSubsets;
  }

  onUILEditToggleChange(event: any) {
    if (event.target.checked) {
      // Enable the fields
      this.datasetQueryForm.get('gateId')?.enable();
      this.datasetQueryForm.get('platformId')?.enable();
      this.datasetQueryForm.get('datasetId')?.enable();
    } else {
      // Disable the fields
      this.datasetQueryForm.get('gateId')?.disable();
      this.datasetQueryForm.get('platformId')?.disable();
      this.datasetQueryForm.get('datasetId')?.disable();
    }
  }

  onSubsetsChange(subset: string, event: any) {
    if (event.target.checked) {
      this.selectedSubsets.add(subset)
    } else {
      this.selectedSubsets.delete(subset);
    }
    this.datasetQueryForm.setValue({
      gateId: this.datasetQueryForm.get('gateId')?.value,
      platformId: this.datasetQueryForm.get('platformId')?.value,
      datasetId: this.datasetQueryForm.get('datasetId')?.value,
      subsetIds: this.selectedSubsets
    });
  }

  private parseAndSetXml(base64Xml: string): void {
    try {
      const xmlString = atob(base64Xml);
      const parser = new DOMParser();
      const xmlDoc = parser.parseFromString(xmlString, "application/xml");

      if (xmlDoc.getElementsByTagName("parsererror").length > 0) {
        console.error("Error parsing XML.");
        this.parsedXmlData = null;
        return;
      }

      const rootNode = this.convertNodeToJson(xmlDoc.documentElement);
      this.parsedXmlData = rootNode.children;

    } catch (e) {
      console.error("Failed to decode or parse XML", e);
      this.parsedXmlData = null;
    }
  }

  private convertNodeToJson(node: Element): XmlNode {
    const children = Array.from(node.children).map(child => this.convertNodeToJson(child));
    const attributes = Array.from(node.attributes).map(attr => ({ key: attr.name, value: attr.value }));

    const textValue = Array.from(node.childNodes)
      .filter(child => child.nodeType === Node.TEXT_NODE && child.textContent?.trim())
      .map(child => child.textContent?.trim())
      .join(' ');

    return {
      name: node.localName,
      attributes: attributes,
      children: children,
      value: textValue || undefined,
      isExpanded: true
    };
  }

  openDialog() {
    this.dialog.nativeElement.showModal(); // opens dialog as modal
  }

  closeDialog() {
    this.dialog.nativeElement.close(); // closes dialog
  }

  onFollowUpSubmit() {
    if (!this.followUpForm.valid || !this.datasetQueryResponse?.requestId) {
      return;
    }

    const formValues = this.followUpForm.value;

    const message = formValues.message;
    const requestId = this.datasetQueryResponse.requestId;

    this.http.post("/api/v1/follow-up", {
      requestId: requestId,
      message: message
    }).subscribe(() => {
      this.followUpForm.reset();
      this.showSuccessMessageForFollowUp = true;

      // Clear the success message after 2 seconds
      setTimeout(() => {
        this.showSuccessMessageForFollowUp = false;
      }, 2000);
    });
  }

  navigateBack() {
    this.location.back();
  }
}
