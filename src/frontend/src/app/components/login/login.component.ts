import { Component, inject, OnInit } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { CommonModule } from '@angular/common';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { ButtonModule } from 'primeng/button';
import { AuthService } from '../base/auth/auth-service';
import { IftaLabelModule } from 'primeng/iftalabel';
import { FormUtilsService } from '../form-utils.service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'gi-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    InputTextModule,
    PasswordModule,
    ButtonModule,
    IftaLabelModule,
  ],
  providers: [FormUtilsService],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent implements OnInit {
  form: FormGroup = new FormGroup({});
  formUtils = inject(FormUtilsService);
  showError = false;
  errorMessage = '';

  private authService = inject(AuthService);

  ngOnInit(): void {
    const fb = new FormBuilder().nonNullable;
    this.form.addControl('login', fb.control(null, [Validators.required]));
    this.form.addControl('senha', fb.control(null, [Validators.required]));
  }

  login() {
    const login = this.form.value.login;
    const senha = this.form.value.senha;

    if (login && senha) {
      this.authService.login(login, senha).subscribe({
        next: () => {
          console.log('Login bem-sucedido');
        },
        error: (error: HttpErrorResponse) => {
          this.showError = true;
          if (error.status === 401) {
            this.errorMessage = $localize`Login ou senha inválidos`;
          } else {
            this.errorMessage = $localize`Erro inesperado, tente novamente mais tarde`;
          }
        },
      });
    }
  }

  closeMessage() {
    this.showError = false;
  }
}
