import {Component, ElementRef, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {Location, NgClass} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {memberStateSubsets} from '../core/subsets';
import {DatasetResponse} from '../core/types';
import {HttpClient} from '@angular/common/http';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import {Subscription} from 'rxjs';
import { Pdfjs } from '../pdf-viewer/pdfjs';

@Component({
  selector: 'app-dataset-query',
  imports: [
    ReactiveFormsModule,
    TranslatePipe,
    Pdfjs,
    NgClass,
  ],
  templateUrl: './dataset-query.html',
})
export class DatasetQuery implements OnInit, OnDestroy {
  @ViewChild('myDialog') dialog!: ElementRef<HTMLDialogElement>;
  @ViewChild('subsetDetails') subsetDetails!: ElementRef<HTMLDetailsElement>
  protected datasetQueryForm: FormGroup;
  protected readonly selectedSubsets = new Set<string>();
  protected isLoading: boolean = false;
  protected datasetQueryResponse: DatasetResponse | null = null;
  protected datasetQueryErrorMessage: string | null = null;
  protected followUpForm: FormGroup;
  protected showSuccessMessageForFollowUp: boolean = false;
  protected availableSubsets: any[] = [];
  protected isEnglishLanguage: boolean = true;
  private langChangeSubscription!: Subscription;
  protected isUilEditMode: boolean = false;
  protected pdfData: Blob | null = null;

  @ViewChild('subsetInfoDialog') subsetInfoDialog!: ElementRef<HTMLDialogElement>;
  protected selectedSubsetInfo: any | null = null;

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly fb: FormBuilder,
    private readonly http: HttpClient,
    private readonly location: Location,
    private readonly translate: TranslateService,
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

  ngOnInit(): void {
    this.filterSubsetsByLanguage();
    this.langChangeSubscription = this.translate.onLangChange.subscribe((_event: any) => {
      this.filterSubsetsByLanguage();
    });
  }

  ngOnDestroy(): void {
    if (this.langChangeSubscription) {
      this.langChangeSubscription.unsubscribe();
    }
  }

  onSubmit() {
    if (!this.datasetQueryForm.valid) {
      return;
    }
    this.pdfData = null;
    this.datasetQueryResponse = null;
    this.datasetQueryErrorMessage = null;
    this.isLoading = true;

    const formValues = this.datasetQueryForm.getRawValue();
    const identifiersQuery = `/api/v1/dataset/${formValues.gateId}/${formValues.platformId}/${formValues.datasetId}?subsets=${Array.from(formValues.subsetIds).join(',')}`;

    this.http.get<DatasetResponse>(identifiersQuery)
      .subscribe({
        next: (v) => {
          this.isLoading = false;
          this.datasetQueryResponse = v;

          if (this.subsetDetails) {
            this.subsetDetails.nativeElement.open = false;
          }

          if (v.errorCode || !v.data) {
            this.datasetQueryErrorMessage = v.errorDescription || this.translate.instant('datasetQuery.errorFetchingDataset');
            return;
          }

          if (v.pdfData) {
            fetch(`data:application/pdf;base64,${v.pdfData}`)
              .then(res => res.blob())
              .then(blob => {
                this.pdfData = blob;
              })
              .catch(e => {
                console.error('Error decoding Base64 PDF data:', e);
                this.datasetQueryErrorMessage = this.translate.instant('datasetQuery.errorDecodingPdf');
              });
          }
        },
        error: (error) => {
          console.error('Error fetching dataset:', error);
          this.isLoading = false;
          this.datasetQueryErrorMessage = this.translate.instant('datasetQuery.errorFetchingDataset');
        }
      });
  }

  private filterSubsetsByLanguage(): void {
    this.selectedSubsets.clear();
    this.datasetQueryForm.get('subsetIds')?.setValue(this.selectedSubsets);

    const currentLang = this.translate.currentLang;
    this.isEnglishLanguage = currentLang === 'en';

    if (this.isEnglishLanguage) {
      this.availableSubsets = memberStateSubsets;
    } else {
      let countryCode = '';
      switch (currentLang) {
        case 'et': countryCode = 'EE'; break;
        case 'lv': countryCode = 'LV'; break;
        case 'lt': countryCode = 'LT'; break;
        case 'pl': countryCode = 'PL'; break;
        default:
          this.availableSubsets = memberStateSubsets;
          return;
      }
      const filtered = memberStateSubsets.find(ms => ms.isoCode === countryCode);
      this.availableSubsets = filtered ? [filtered] : [];
    }
  }

  onUILEditToggleChange(event: any) {
    this.isUilEditMode = event.target.checked;
    this.datasetQueryForm.get('gateId')?.enable();
    this.datasetQueryForm.get('platformId')?.enable();
    this.datasetQueryForm.get('datasetId')?.enable();
  }

  onSubsetsChange(subset: string, event: any) {
    if (event.target.checked) {
      this.selectedSubsets.add(subset)
    } else {
      this.selectedSubsets.delete(subset);
    }
    this.datasetQueryForm.get('subsetIds')?.setValue(this.selectedSubsets);
  }

  openDialog() {
    this.dialog.nativeElement.showModal();
  }

  closeDialog() {
    this.dialog.nativeElement.close();
  }

  onFollowUpSubmit() {
    if (!this.followUpForm.valid || !this.datasetQueryResponse?.requestId) {
      return;
    }

    const formValues = this.followUpForm.value;
    const requestId = this.datasetQueryResponse.requestId;

    this.http.post("/api/v1/follow-up", {
      requestId: requestId,
      message: formValues.message
    }).subscribe(() => {
      this.followUpForm.reset();
      this.showSuccessMessageForFollowUp = true;
      setTimeout(() => {
        this.showSuccessMessageForFollowUp = false;
      }, 2000);
    });
  }

  navigateBack() {
    this.location.back();
  }

  openSubsetInfoDialog(subset: any) {
    this.selectedSubsetInfo = subset;
    this.subsetInfoDialog.nativeElement.showModal();
  }

  closeSubsetInfoDialog() {
    this.subsetInfoDialog.nativeElement.close();
    this.selectedSubsetInfo = null;
  }

  onDialogClick(event: MouseEvent) {
    if (event.target === this.subsetInfoDialog.nativeElement) {
      this.closeSubsetInfoDialog();
    }
  }
}
