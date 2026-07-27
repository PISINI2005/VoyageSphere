import {
  Component,
  ChangeDetectorRef
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Role } from '../../../../core/enums/admin-enums';
import { CreateUserDTO } from '../../../../core/models/admin.model';
import { UserService } from '../../../../core/services/user';


@Component({
  selector: 'app-add-user',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './add-user.html'
})
export class AddUser {

  loading = false;

  showPopup = false;

  popupTitle = '';

  popupMessage = '';

  user: CreateUserDTO = {
    firstName:'',
    lastName:'',

    email: '',

    role: Role.TRAVEL_AGENT,

    phoneNo: 0

  };

  readonly roles = [

    Role.ADMIN,

    Role.TRAVEL_AGENT,

    Role.FINANCE_OFFICER,

    Role.COMPLIANCE_OFFICER

  ];

  constructor(
    private readonly userService: UserService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef
  ) {}

  addUser(): void {

    console.log(
      '[AddUser] Form Submit Triggered'
    );

    this.showPopup = false;

    this.popupTitle = '';

    this.popupMessage = '';

   if (
  !this.user.firstName?.trim() ||
  !this.user.lastName?.trim() ||
  !this.user.email?.trim()
) {

  this.popupTitle = 'Validation Error';
  this.popupMessage = 'Please fill all required fields.';
  this.showPopup = true;
  this.cdr.detectChanges();
  return;
}

    this.loading = true;

    console.log(
      '[AddUser] Sending Payload:',
      JSON.stringify(
        this.user,
        null,
        2
      )
    );

    this.userService
      .createUser(this.user)
      .subscribe({

        next: (response) => {

          console.log(
            '[AddUser] Success Response:',
            response
          );

          this.loading = false;

          this.popupTitle = 'Success';

          this.popupMessage =
            'User created successfully';

          this.showPopup = true;

          this.cdr.detectChanges();

          console.log(
            '[AddUser] Popup Opened'
          );

        },

        error: (error) => {

          console.error(
            '[AddUser] HTTP Error:',
            error
          );

          console.error(
            '[AddUser] Backend Response:',
            error?.error
          );

          this.loading = false;

          this.popupTitle = 'Error';

          this.popupMessage =
            error?.error?.message ??
            'Operation Failed';

          this.showPopup = true;

          this.cdr.detectChanges();

          console.log(
            '[AddUser] Error Popup Opened'
          );

        },

        complete: () => {

          console.log(
            '[AddUser] Request Completed'
          );

          this.loading = false;

          this.cdr.detectChanges();

        }

      });

  }

  closePopup(): void {

    console.log(
      '[AddUser] Popup Closed'
    );

    this.showPopup = false;

    this.cdr.detectChanges();

    if (
      this.popupTitle === 'Success'
    ) {

      this.router.navigate([
        '/admin/user/view'
      ]);

    }

  }

}