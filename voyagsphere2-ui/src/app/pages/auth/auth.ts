import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth';
import { LoginDTO, CreateUserDTO } from '../../core/models/travel.model';
import DOMPurify from 'dompurify';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './auth.html',
  styleUrl: './auth.css'
})
export class AuthComponent {

  view: 'LOGIN' | 'REGISTER' = 'LOGIN';

  loginForm: LoginDTO = {
    email: '',
    password: ''
  };

  registerForm: CreateUserDTO = {
    firstName:'',
    lastName:'',
    email: '',
    role: 'CUSTOMER',
    phoneNo: 0,
    password: ''
  };
  errorMessage: string='';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  login() {

  this.loginForm.email = DOMPurify
    .sanitize(this.loginForm.email)
    .trim();

  this.loginForm.password = DOMPurify
    .sanitize(this.loginForm.password);

  if (!this.loginForm.email || !this.loginForm.password) {
    alert('Please enter email and password');
    return;
  }

  this.authService.login(this.loginForm).subscribe({
    next: (response) => {

      this.authService.setToken(response.token);

      localStorage.setItem(
        'user',
        JSON.stringify(response.user)
      );

      switch (response.user.role) {

        case 'TRAVEL_AGENT':
          this.router.navigate(['/agent']);
          break;

        case 'ADMIN':
          this.router.navigate(['/admin']);
          break;

        case 'COMPLIANCE_OFFICER':
          this.router.navigate(['/compliance']);
          break;

        case 'FINANCE_OFFICER':
          this.router.navigate(['/finance']);
          break;

        default:
          this.router.navigate(['/search']);
      }
    },
    error: (err) => {
      console.error('Login failed:', err);
      alert('Login failed. Please check your credentials.');
    }
  });
}

  register() {

  this.registerForm.firstName = DOMPurify.sanitize(this.registerForm.firstName);
  this.registerForm.lastName = DOMPurify.sanitize(this.registerForm.lastName);
  this.registerForm.email = DOMPurify.sanitize(this.registerForm.email);
  this.registerForm.password = DOMPurify.sanitize(this.registerForm.password);

  if (
    !this.registerForm.firstName ||
    !this.registerForm.lastName ||
    !this.registerForm.email ||
    !this.registerForm.password ||
    !this.registerForm.phoneNo
  ) {
    alert('Please fill in all required fields');
    return;
  }

  this.authService.register(this.registerForm).subscribe({
    next: () => {
      alert('Registration Successful');
      this.view = 'LOGIN';
    },
    error: (err) => {
      console.error('Registration failed:', err);
      this.errorMessage = err.error?.message || 'Registration failed.';
      
    }
  });
}
}