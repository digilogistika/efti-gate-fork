import { Component } from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {memberStateSubsets} from '../core/subsets';
import {DatasetResponse, IdentifierResponse} from '../core/types';
import {HttpClient} from '@angular/common/http';
import xmlFormatter from 'xml-formatter';
import * as Prism from 'prismjs';

@Component({
  selector: 'app-dataset-query',
  imports: [
    ReactiveFormsModule
  ],
  templateUrl: './dataset-query.html',
})
export class DatasetQuery {
  protected datasetQueryForm: FormGroup;
  protected readonly selectedSubsets = new Set<string>();
  protected isLoading: boolean = false;
  protected datasetQueryResponse: DatasetResponse | null = null;

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly fb: FormBuilder,
    private readonly http: HttpClient
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
      .subscribe(v => {
        this.datasetQueryResponse = v;
        this.isLoading = false;
      })
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

  getFormattedXml(base64Xml: string): string {
    const rawXml = atob(base64Xml);

    const formatted = xmlFormatter(rawXml, {
      indentation: '  ', // 2 spaces
      collapseContent: true,
    });

    return formatted
  }
}
