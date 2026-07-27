import {
  Component,
  EventEmitter,
  Input,
  Output
} from '@angular/core';

@Component({
  selector: 'app-confirmation-modal',
  standalone: true,
  templateUrl: './confirmation-modal.html',
  styleUrl: './confirmation-modal.css'
})
export class ConfirmationModalComponent {

  @Input() show = false;

  @Input() title = 'Confirmation';

  @Input() message = '';

  @Output()
  cancelled = new EventEmitter<void>();

  @Output()
  confirmed = new EventEmitter<void>();

}