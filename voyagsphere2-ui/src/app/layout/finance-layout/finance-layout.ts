import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Sidebar } from '../../pages/finance-officer/sidebar/sidebar';

@Component({
  selector: 'app-finance-layout',
  standalone: true,
  imports: [RouterOutlet, Sidebar],
  templateUrl: './finance-layout.html',
  styleUrls: ['./finance-layout.css']
})
export class FinanceLayout {}
