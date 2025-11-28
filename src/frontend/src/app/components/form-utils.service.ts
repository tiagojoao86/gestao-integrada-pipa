import { Injectable } from '@angular/core';
import { AbstractControl, FormGroup } from '@angular/forms';

@Injectable()
export class FormUtilsService {
  isControlInvalid(campo: string, form: FormGroup) {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const fc: AbstractControl<any, any> | null = form.get(campo);

    if (fc !== null && fc.invalid && (fc.touched || fc.dirty)) {
      return true;
    }

    return false;
  }
}
