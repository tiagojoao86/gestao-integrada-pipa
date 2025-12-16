import { Injectable } from '@angular/core';
import { AbstractControl, FormGroup } from '@angular/forms';

@Injectable()
export class FormUtilsService {
  isControlInvalid(campo: string, form: FormGroup) {
    const fc: AbstractControl<unknown, unknown> | null = form.get(campo);

    if (fc !== null && fc.invalid && (fc.touched || fc.dirty)) {
      return true;
    }

    return false;
  }
}
