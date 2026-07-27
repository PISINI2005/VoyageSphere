import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-toast-notification',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './toast-notification.html',
  styleUrl: './toast-notification.css'
})
export class ToastNotificationComponent {

  @Input() type:
    'success' |
    'error' |
    'warning' |
    'info' = 'success';

  @Input() title = '';

  @Input() message = '';

  @Input() visible = false;

}
