import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-popup',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './popupwindow.html'
})
export class Popup {

  @Input()
  title = '';

  @Input()
  message = '';

  visible = true;

  close(): void {

    this.visible = false;

  }

}